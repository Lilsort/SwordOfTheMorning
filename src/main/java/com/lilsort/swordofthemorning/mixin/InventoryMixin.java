package com.lilsort.swordofthemorning.mixin;

import com.lilsort.swordofthemorning.retention.RetentionManager;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Shadow @Final public Player player;
    @Shadow @Final private List<NonNullList<ItemStack>> compartments;

    @Inject(method = "dropAll", at = @At("HEAD"))
    private void onDropAll(CallbackInfo ci) {
        if (this.player != null) {
            for (NonNullList<ItemStack> list : this.compartments) {
                for (int i = 0; i < list.size(); i++) {
                    ItemStack stack = list.get(i);
                    if (!stack.isEmpty() && RetentionManager.isAnySword(stack)) {
                        // Preserve the sword stack for respawn so it is never lost
                        RetentionManager.preserveForRespawn(this.player.getUUID(), stack.copy());
                        // Clear from list so dropAll() will not drop it onto the ground
                        list.set(i, ItemStack.EMPTY);
                    }
                }
            }
        }
    }
}
