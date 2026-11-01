package com.lilsort.swordofthemorning.mixin;

import com.lilsort.swordofthemorning.retention.RetentionManager;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerDropMixin {

    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"))
    private void onDrop(ItemStack itemStack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        if (!throwRandomly && !itemStack.isEmpty() && RetentionManager.isAnySword(itemStack)) {
            Player player = (Player) (Object) this;
            RetentionManager.onIntentionalDrop(player, itemStack);
        }
    }
}
