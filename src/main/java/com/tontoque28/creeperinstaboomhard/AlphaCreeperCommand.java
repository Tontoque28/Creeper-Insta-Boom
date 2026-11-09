package com.tontoque28.creeperinstaboomhard;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class AlphaCreeperCommand extends CommandBase {

    @Override
    public String getName() {
        return "alphacreeper";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/alphacreeper [x y z]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        World world = sender.getEntityWorld();
        if (args.length == 0) {
            Entity entity = sender.getCommandSenderEntity();
            if (!(entity instanceof EntityPlayerMP)) {
                throw new PlayerNotFoundException("commands.generic.player.unspecified");
            }
            spawnAlpha(entity.world, entity.getPositionVector());
            return;
        }
        if (args.length != 3) {
            throw new WrongUsageException(getUsage(sender));
        }
        Vec3d base = sender.getPositionVector();
        double x = parseCoordinate(base.x, args[0], false).getResult();
        double y = parseCoordinate(base.y, args[1], false).getResult();
        double z = parseCoordinate(base.z, args[2], false).getResult();
        spawnAlpha(world, new Vec3d(x, y, z));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length >= 1 && args.length <= 3) {
            return getTabCompletionCoordinate(args, 0, targetPos);
        }
        return Collections.emptyList();
    }

    private static void spawnAlpha(World world, Vec3d pos) {
        EntityCreeper creeper = new EntityCreeper(world);

        creeper.setLocationAndAngles(pos.x, pos.y, pos.z, 0.0F, 0.0F);

        EntityLightningBolt lightning = new EntityLightningBolt(world, pos.x, pos.y, pos.z, true);
        creeper.onStruckByLightning(lightning);

        creeper.setCustomNameTag("Alpha Creeper");
        creeper.setAlwaysRenderNameTag(true);

        if (creeper.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null) {
            creeper.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
            creeper.setHealth(40.0F);
        }

        if (creeper.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE) != null) {
            creeper.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        }

        if (creeper.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
            creeper.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.675D);
        }

        creeper.addPotionEffect(new PotionEffect(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));

        creeper.getEntityData().setBoolean("CIB_Processed", true);
        creeper.addTag("alpha");

        world.spawnEntity(creeper);
    }
}
