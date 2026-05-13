package com.tontoque28.CreeperInstaBoomHard;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.world.phys.Vec3;

public class AlphaCreeperCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("alphacreeper")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                .executes(AlphaCreeperCommand::spawnAlphaAtPos))
                        .executes(AlphaCreeperCommand::spawnAlphaAtPlayer)
        );
    }

    private static int spawnAlphaAtPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        return spawnAlpha(context.getSource().getLevel(), player.position());
    }

    private static int spawnAlphaAtPos(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Vec3 pos = Vec3Argument.getVec3(context, "pos");
        return spawnAlpha(context.getSource().getLevel(), pos);
    }

    private static int spawnAlpha(ServerLevel level, Vec3 pos) {
        Creeper creeper = EntityType.CREEPER.create(level);

        if (creeper == null) return 0;

        creeper.moveTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);

        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(pos);
            lightning.setVisualOnly(true);
            creeper.thunderHit(level, lightning);
        }

        creeper.setCustomName(Component.literal("Alpha Creeper"));
        creeper.setCustomNameVisible(true);

        if (creeper.getAttribute(Attributes.MAX_HEALTH) != null) {
            creeper.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0D);
            creeper.setHealth(40.0F);
        }
        
        if (creeper.getAttribute(Attributes.FOLLOW_RANGE) != null) {
            creeper.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(64.0D);
        }
        
        if (creeper.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
             creeper.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.675D);
        }

        creeper.addEffect(new MobEffectInstance(
                MobEffects.GLOWING,
                Integer.MAX_VALUE,
                0,
                false,
                false
        ));
        
        creeper.getPersistentData().putBoolean("CIB_Processed", true);
        creeper.addTag("alpha");

        level.addFreshEntity(creeper);

        return 1;
    }
}
