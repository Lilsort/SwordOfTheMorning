package com.lilsort.swordofthemorning.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SwordoftheMorningItem extends SwordItem {

    public SwordoftheMorningItem() {
        // Base damage 1.0 + Tier bonus 4.0 + Modifier 4 = 9.0 Attack Damage!
        // Attack speed -2.4F => 4.0 - 2.4 = 1.6 Attack Speed.
        super(DawnTier.INSTANCE, 4, -2.4F, new Properties()
                .fireResistant()
                .rarity(Rarity.EPIC)
                .stacksTo(1));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 1. Attacker receives Speed II for 2 seconds (40 ticks)
        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, true, true));

        // 2. 20% chance to afflict target with Glowing for 10 seconds (200 ticks)
        if (attacker.getRandom().nextFloat() < 0.20F) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, true, true));
        }

        // 3. Spawn white metallic sparks and impact flash particles
        if (attacker.level() instanceof ServerLevel serverLevel) {
            double px = target.getX();
            double py = target.getY() + target.getBbHeight() * 0.55;
            double pz = target.getZ();

            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 16, 0.35, 0.35, 0.35, 0.15);
            serverLevel.sendParticles(ParticleTypes.FLASH, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
            serverLevel.sendParticles(ParticleTypes.CRIT, px, py, pz, 10, 0.25, 0.25, 0.25, 0.2);
        }

        // 4. Heavy metallic strike sounds
        attacker.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 0.85F);
        attacker.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.4F, 1.25F);
        attacker.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.NETHERITE_BLOCK_HIT, SoundSource.PLAYERS, 0.8F, 0.7F);

        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Right-Click Dash / Lunge (Выпад вперед)
        Vec3 look = player.getLookAngle();
        double forwardStrength = 1.25;
        double upHop = player.onGround() ? 0.20 : 0.08;
        Vec3 motion = new Vec3(look.x * forwardStrength, Math.max(upHop, look.y * 0.4 + upHop), look.z * forwardStrength);
        player.setDeltaMovement(motion);
        player.hurtMarked = true;

        // Snappy cooldown: 1.25 seconds (25 ticks)
        player.getCooldowns().addCooldown(this, 25);

        // Audio whoosh + slice
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.25F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.PLAYERS, 0.40F, 1.6F);

        // Visual sweep particle
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + look.x * 0.7, player.getY() + 0.9, player.getZ() + look.z * 0.7, 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 0.8, player.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("item.swordofthemorning.sword_of_the_morning.lore_title")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.swordofthemorning.sword_of_the_morning.lore_desc")
                .withStyle(ChatFormatting.GRAY));

        if (stack.hasTag() && stack.getTag().contains("OwnerName")) {
            tooltip.add(Component.literal("Владелец: " + stack.getTag().getString("OwnerName"))
                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        }

        tooltip.add(Component.empty());

        tooltip.add(Component.translatable("item.swordofthemorning.sword_of_the_morning.effect_speed")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.swordofthemorning.sword_of_the_morning.effect_glowing")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.swordofthemorning.sword_of_the_morning.effect_unbreakable")
                .withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.translatable("item.swordofthemorning.sword_of_the_morning.effect_lunge")
                .withStyle(ChatFormatting.BLUE));

        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }
}
