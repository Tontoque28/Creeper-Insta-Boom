package com.tontoque28.CreeperInstaBoomHard;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(CreeperInstaBoomHard.MODID)
public class CreeperInstaBoomHard {

    public static final String MODID = "creeperinstaboomhard";

    public CreeperInstaBoomHard() {
    }

    @Mod.EventBusSubscriber(modid = MODID)
    public static class CommandRegistration {

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            AlphaCreeperCommand.register(event.getDispatcher());
            CreeperDifficultyCommand.register(event.getDispatcher());
        }
    }
}
