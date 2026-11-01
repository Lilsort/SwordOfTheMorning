package com.lilsort.swordofthemorning.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class DawnTier implements Tier {
    public static final DawnTier INSTANCE = new DawnTier();

    @Override
    public int getUses() {
        return 0; // Infinite durability
    }

    @Override
    public float getSpeed() {
        return 9.0F; // Mining speed bonus
    }

    @Override
    public float getAttackDamageBonus() {
        // Base player damage is 1.0F.
        // Bonus (4.0F) + Sword modifier (4.0F) + Base (1.0F) = 9.0F total Attack Damage!
        return 4.0F;
    }

    @Override
    public int getLevel() {
        return 4; // Netherite-tier mining level
    }

    @Override
    public int getEnchantmentValue() {
        return 22; // High enchantability like gold/netherite
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY; // Unbreakable, cannot and does not need repair
    }
}
