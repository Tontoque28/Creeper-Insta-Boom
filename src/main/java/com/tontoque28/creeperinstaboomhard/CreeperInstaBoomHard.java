package com.tontoque28.creeperinstaboomhard;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = CreeperInstaBoomHard.MODID, name = "Creeper Insta Boom", version = "1.1.1", acceptableRemoteVersions = "*")
public class CreeperInstaBoomHard {

    public static final String MODID = "creeperinstaboomhard";

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new AlphaCreeperCommand());
        event.registerServerCommand(new CreeperDifficultyCommand());
    }
}
