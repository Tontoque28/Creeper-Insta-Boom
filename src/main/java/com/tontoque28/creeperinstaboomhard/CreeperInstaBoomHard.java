package com.tontoque28.creeperinstaboomhard;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

public class CreeperInstaBoomHard implements ModInitializer {

    public static final String MODID = "creeperinstaboomhard";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            AlphaCreeperCommand.register(dispatcher);
            CreeperDifficultyCommand.register(dispatcher);
        });
        ServerEntityEvents.ENTITY_LOAD.register(CreeperModifier::onSpawn);
    }
}
