package com.lilsort.swordofthemorning.retention;

import com.lilsort.swordofthemorning.item.ModItems;
import com.lilsort.swordofthemorning.mixin.ChunkMapAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class RetentionManager {
    public static final String NBT_OWNER_UUID = "OwnerUUID";
    public static final String NBT_OWNER_NAME = "OwnerName";
    public static final String TAG_BEARER = "swordofthemorning_bearer";

    // Preserved items for dead players awaiting respawn
    private static final Map<UUID, List<ItemStack>> DEATH_RETAINED_ITEMS = new ConcurrentHashMap<>();

    public static boolean isAnySword(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ModItems.SWORD_OF_THE_MORNING) || stack.is(ModItems.SWORD_OF_THE_MORNING_CLASSIC);
    }

    public static boolean isSwordOwnedBy(ItemStack stack, Player player) {
        if (!isAnySword(stack)) return false;
        if (stack.hasTag() && stack.getTag().contains(NBT_OWNER_UUID)) {
            return stack.getTag().getString(NBT_OWNER_UUID).equals(player.getStringUUID());
        }
        return true; // Untagged legacy sword fallback
    }

    public static void preserveForRespawn(UUID playerUuid, ItemStack stack) {
        if (stack.isEmpty()) return;
        DEATH_RETAINED_ITEMS.computeIfAbsent(playerUuid, u -> new ArrayList<>()).add(stack);
    }

    public static void restoreOnRespawn(ServerPlayer player) {
        UUID uuid = player.getUUID();
        List<ItemStack> preserved = DEATH_RETAINED_ITEMS.remove(uuid);

        if (preserved != null && !preserved.isEmpty()) {
            for (ItemStack stack : preserved) {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
            }
            player.getInventory().setChanged();
        }
    }

    public static boolean hasSword(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (isAnySword(stack)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasSwordType(Player player, boolean isClassic) {
        var targetItem = isClassic ? ModItems.SWORD_OF_THE_MORNING_CLASSIC : ModItems.SWORD_OF_THE_MORNING;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.is(targetItem)) {
                return true;
            }
        }
        return false;
    }

    public static int clearSwordsMatching(Container container, Predicate<ItemStack> predicate) {
        if (container == null) return 0;
        int count = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (predicate.test(stack)) {
                container.setItem(i, ItemStack.EMPTY);
                count++;
            }
        }
        if (count > 0) {
            container.setChanged();
        }
        return count;
    }

    public static void giveSword(Player player, boolean isClassic) {
        // Clear any existing Dawn swords from player's inventory first so there's never duplicate 3d+2d clutter
        clearSwordsMatching(player.getInventory(), RetentionManager::isAnySword);

        ItemStack sword = new ItemStack(isClassic ? ModItems.SWORD_OF_THE_MORNING_CLASSIC : ModItems.SWORD_OF_THE_MORNING);
        // Stamp owner binding in NBT
        sword.getOrCreateTag().putString(NBT_OWNER_UUID, player.getStringUUID());
        sword.getOrCreateTag().putString(NBT_OWNER_NAME, player.getGameProfile().getName());

        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, sword);
        } else if (!player.getInventory().add(sword)) {
            player.drop(sword, false);
        }
        player.getInventory().setChanged();
        setBearer(player, true);
    }

    public static boolean isBearer(Player player) {
        return player.getTags().contains(TAG_BEARER);
    }

    public static void setBearer(Player player, boolean bearer) {
        if (bearer) {
            player.addTag(TAG_BEARER);
        } else {
            player.removeTag(TAG_BEARER);
        }
    }

    public static void onIntentionalDrop(Player player, ItemStack stack) {
        if (isAnySword(stack)) {
            setBearer(player, false);
        }
    }

    /**
     * Purges only the sword(s) bound to a specific owner player across the entire server.
     * Prevents duping/hiding by removing the sword from other players, chests, ground items, etc.
     */
    public static int purgePlayerSwords(MinecraftServer server, ServerPlayer owner) {
        String ownerUuid = owner.getStringUUID();
        Predicate<ItemStack> filter = stack -> {
            if (!isAnySword(stack)) return false;
            if (stack.hasTag() && stack.getTag().contains(NBT_OWNER_UUID)) {
                return stack.getTag().getString(NBT_OWNER_UUID).equals(ownerUuid);
            }
            return false;
        };

        int totalRemoved = purgeMatchingSwords(server, filter);

        // Also clean up any untagged legacy sword directly in owner's inventory
        totalRemoved += clearSwordsMatching(owner.getInventory(), RetentionManager::isAnySword);

        setBearer(owner, false);
        DEATH_RETAINED_ITEMS.remove(owner.getUUID());

        return totalRemoved;
    }

    /**
     * Global wipe of all swords on the server (admin emergency tool).
     */
    public static int purgeAllSwords(MinecraftServer server) {
        int totalRemoved = purgeMatchingSwords(server, RetentionManager::isAnySword);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            setBearer(player, false);
        }
        DEATH_RETAINED_ITEMS.clear();

        return totalRemoved;
    }

    private static int purgeMatchingSwords(MinecraftServer server, Predicate<ItemStack> predicate) {
        int totalRemoved = 0;

        // 1. Players
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            totalRemoved += clearSwordsMatching(player.getInventory(), predicate);
            totalRemoved += clearSwordsMatching(player.getEnderChestInventory(), predicate);

            if (player.containerMenu != null) {
                ItemStack carried = player.containerMenu.getCarried();
                if (predicate.test(carried)) {
                    player.containerMenu.setCarried(ItemStack.EMPTY);
                    totalRemoved++;
                }
                for (Slot slot : player.containerMenu.slots) {
                    if (predicate.test(slot.getItem())) {
                        slot.set(ItemStack.EMPTY);
                        totalRemoved++;
                    }
                }
                player.containerMenu.broadcastChanges();
            }
        }

        // 2. All loaded levels (entities and block containers)
        for (ServerLevel level : server.getAllLevels()) {
            // Entities
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof ItemEntity itemEntity) {
                    if (predicate.test(itemEntity.getItem())) {
                        itemEntity.discard();
                        totalRemoved++;
                    }
                } else if (entity instanceof ItemFrame itemFrame) {
                    if (predicate.test(itemFrame.getItem())) {
                        itemFrame.setItem(ItemStack.EMPTY);
                        totalRemoved++;
                    }
                } else if (entity instanceof Container container) {
                    totalRemoved += clearSwordsMatching(container, predicate);
                } else if (entity instanceof ArmorStand armorStand) {
                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        ItemStack stack = armorStand.getItemBySlot(slot);
                        if (predicate.test(stack)) {
                            armorStand.setItemSlot(slot, ItemStack.EMPTY);
                            totalRemoved++;
                        }
                    }
                } else if (entity instanceof LivingEntity living && !(living instanceof Player)) {
                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        ItemStack stack = living.getItemBySlot(slot);
                        if (predicate.test(stack)) {
                            living.setItemSlot(slot, ItemStack.EMPTY);
                            totalRemoved++;
                        }
                    }
                }
            }

            // Block entities (chests, barrels, hoppers, dispensers, shulkers, etc.)
            try {
                ChunkMapAccessor chunkMapAccessor = (ChunkMapAccessor) level.getChunkSource().chunkMap;
                for (ChunkHolder holder : chunkMapAccessor.callGetChunks()) {
                    LevelChunk chunk = holder.getTickingChunk();
                    if (chunk != null) {
                        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                            if (blockEntity instanceof Container container) {
                                totalRemoved += clearSwordsMatching(container, predicate);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return totalRemoved;
    }
}
