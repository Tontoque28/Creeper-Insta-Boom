package com.tontoque28.creeperinstaboomhard;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class AlphaCreeperCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("alphacreeper")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                .executes(AlphaCreeperCommand::spawnAlphaAtPos))
                        .executes(AlphaCreeperCommand::spawnAlphaAtPlayer)
        );
    }

    private static int spawnAlphaAtPlayer(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        return spawnAlpha(context.getSource().getLevel(), player.position());
    }

    private static int spawnAlphaAtPos(CommandContext<CommandSource> context) throws CommandSyntaxException {
        Vector3d pos = Vec3Argument.getVec3(context, "pos");
        return spawnAlpha(context.getSource().getLevel(), pos);
    }

    private static int spawnAlpha(ServerWorld level, Vector3d pos) {
        CreeperEntity creeper = EntityType.CREEPER.create(level);

        if (creeper == null) return 0;

        creeper.moveTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);

        LightningBoltEntity lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(pos.x, pos.y, pos.z);
            lightning.setVisualOnly(true);
            creeper.thunderHit(level, lightning);
        }

        creeper.setCustomName(new StringTextComponent("Alpha Creeper"));
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

        creeper.addEffect(new EffectInstance(
                Effects.GLOWING,
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
