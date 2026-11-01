package com.lilsort.swordofthemorning.item;

import com.lilsort.swordofthemorning.SwordoftheMorning;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final Item SWORD_OF_THE_MORNING = registerItem("sword_of_the_morning", new SwordoftheMorningItem());
    public static final Item SWORD_OF_THE_MORNING_CLASSIC = registerItem("sword_of_the_morning_classic", new SwordoftheMorningItem());

    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, SwordoftheMorning.id(name), item);
    }

    public static void registerModItems() {
        SwordoftheMorning.LOGGER.info("Registering Mod Items for " + SwordoftheMorning.MOD_ID);

        // Add to Combat creative tab
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            entries.accept(SWORD_OF_THE_MORNING);
            entries.accept(SWORD_OF_THE_MORNING_CLASSIC);
        });
    }
}
