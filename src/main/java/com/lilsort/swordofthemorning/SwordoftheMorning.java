package com.lilsort.swordofthemorning;

import com.lilsort.swordofthemorning.command.SwordCommand;
import com.lilsort.swordofthemorning.item.ModItems;
import com.lilsort.swordofthemorning.retention.RetentionManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwordoftheMorning implements ModInitializer {
    public static final String MOD_ID = "swordofthemorning";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing ClassicAge Weapons - Sword of the Morning (Dawn)");

        // 1. Register items
        ModItems.registerModItems();

        // 2. Register commands (ONLY /sword)
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SwordCommand.register(dispatcher);
        });

        // 3. Register death retention (restore sword after respawn)
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            RetentionManager.restoreOnRespawn(newPlayer);
        });
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
