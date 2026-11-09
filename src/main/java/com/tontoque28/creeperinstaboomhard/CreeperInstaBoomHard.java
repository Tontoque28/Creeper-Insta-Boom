package com.tontoque28.creeperinstaboomhard;

import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CreeperInstaBoomHard.MODID)
public class CreeperInstaBoomHard {

    public static final String MODID = "creeperinstaboomhard";

    public CreeperInstaBoomHard(FMLJavaModLoadingContext context) {
        RegisterCommandsEvent.BUS.addListener(CreeperInstaBoomHard::onRegisterCommands);
        EntityJoinLevelEvent.BUS.addListener(CreeperModifier::onSpawn);
        LivingEvent.LivingTickEvent.BUS.addListener(Priority.HIGHEST, CreeperModifier::onCreeperTick);
        ExplosionEvent.Start.BUS.addListener(Priority.HIGHEST, CreeperModifier::onExplosionStart);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        AlphaCreeperCommand.register(event.getDispatcher());
        CreeperDifficultyCommand.register(event.getDispatcher());
    }
}
