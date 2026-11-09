package com.tontoque28.creeperinstaboomhard;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAICreeperSwell;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = CreeperInstaBoomHard.MODID)
public class CreeperModifier {

    @SubscribeEvent
    public static void onSpawn(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityCreeper)) return;
        if (event.getWorld().isRemote) return;
        EntityCreeper creeper = (EntityCreeper) event.getEntity();

        List<EntityAIBase> goalsToRemove = new ArrayList<>();
        for (EntityAITasks.EntityAITaskEntry entry : creeper.tasks.taskEntries) {
            if (entry.action instanceof EntityAICreeperSwell) {
                goalsToRemove.add(entry.action);
            }
        }
        for (EntityAIBase goal : goalsToRemove) {
            creeper.tasks.removeTask(goal);
        }

        if (creeper.getEntityData().getBoolean("CIB_Processed")) return;
        creeper.getEntityData().setBoolean("CIB_Processed", true);

        Random random = creeper.getRNG();
        boolean isNight = !creeper.world.isDaytime();
        CreeperDifficultyCommand.Difficulty currentMode = CreeperDifficultyCommand.currentDifficulty;

        double baseSpeed = 0.25D;
        double speedMultiplier = 1.0D;

        switch (currentMode) {
            case EASY:
                baseSpeed = 0.28D;
                speedMultiplier = 1.2D;
                break;
            case NORMAL:
                baseSpeed = 0.30D;
                speedMultiplier = 1.4D;
                float chargedChance = isNight ? 0.15F : 0.05F;
                if (!creeper.getPowered() && random.nextFloat() <= chargedChance) {
                    spawnLightning(creeper);
                }
                if (creeper.getPowered()) {
                    speedMultiplier += 0.2D;
                }
                break;
            case HARD:
                baseSpeed = 0.30D;
                speedMultiplier = 1.5D;

                float hardChargedChance = isNight ? 0.35F : 0.10F;
                if (!creeper.getPowered() && random.nextFloat() <= hardChargedChance) {
                    spawnLightning(creeper);
                }

                float alphaChance;
                EnumDifficulty worldDiff = creeper.world.getDifficulty();
                switch (worldDiff) {
                    case EASY: alphaChance = isNight ? 0.03F : 0.01F; break;
                    case NORMAL: alphaChance = isNight ? 0.05F : 0.02F; break;
                    case HARD: alphaChance = isNight ? 0.07F : 0.03F; break;
                    default: alphaChance = 0.02F;
                }
                boolean isAlpha = creeper.getTags().contains("alpha") || (random.nextFloat() <= alphaChance);
                if (isAlpha) {
                    applyAlpha(creeper);
                }

                boolean isCharged = creeper.getPowered();
                if (isCharged) speedMultiplier = 1.75D;
                if (isAlpha) {
                    if (isCharged) speedMultiplier = 2.25D;
                    else speedMultiplier = 2.00D;
                }
                break;
        }

        double finalSpeed = baseSpeed * speedMultiplier;
        if (creeper.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
            creeper.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(finalSpeed);
        }
        if (creeper.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE) != null) {
            creeper.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCreeperTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityCreeper)) return;
        EntityCreeper creeper = (EntityCreeper) event.getEntityLiving();
        if (creeper.world.isRemote) return;
        if (!creeper.isEntityAlive()) return;

        creeper.setCreeperState(-1);

        if (creeper.getEntityData().getBoolean("CIB_Exploded")) return;

        double triggerRadius = 4.0D;
        List<EntityPlayer> players = creeper.world.getEntitiesWithinAABB(EntityPlayer.class, creeper.getEntityBoundingBox().grow(triggerRadius));
        if (players.isEmpty()) return;
        players.removeIf(player -> player.isCreative() || player.isSpectator());
        if (players.isEmpty()) return;

        explodeNow(creeper);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onExplosionStart(ExplosionEvent.Start event) {
        if (event.getWorld().isRemote) return;
        if (event.getExplosion().getExplosivePlacedBy() instanceof EntityCreeper) {
            EntityCreeper creeper = (EntityCreeper) event.getExplosion().getExplosivePlacedBy();
            if (creeper.getEntityData().getBoolean("CIB_Exploding")) return;
            event.setCanceled(true);
            explodeNow(creeper);
        }
    }

    private static void explodeNow(EntityCreeper creeper) {
        if (creeper.getEntityData().getBoolean("CIB_Exploded")) return;
        creeper.getEntityData().setBoolean("CIB_Exploded", true);
        creeper.getEntityData().setBoolean("CIB_Exploding", true);

        boolean isAlpha = creeper.getTags().contains("alpha");
        boolean isCharged = creeper.getPowered();

        float explosionPower = 6.0F;
        float piercingPercentage = 0.0F;

        if (isCharged) {
            explosionPower = 12.0F;
            piercingPercentage = 0.20F;
        }

        if (isAlpha) {
            if (isCharged) {
                explosionPower = 24.0F;
                piercingPercentage = 0.60F;
            } else {
                explosionPower = 18.0F;
                piercingPercentage = 0.40F;
            }
        }

        creeper.ignite();
        boolean destroy = ForgeEventFactory.getMobGriefingEvent(creeper.world, creeper);
        creeper.world.createExplosion(creeper, creeper.posX, creeper.posY, creeper.posZ, explosionPower, destroy);

        double radius = explosionPower * 2.0D;
        List<Entity> entities = creeper.world.getEntitiesWithinAABBExcludingEntity(creeper, creeper.getEntityBoundingBox().grow(radius));
        for (Entity entity : entities) {
            if (entity == creeper) continue;
            float baseDamage = 8.0F;
            if (isCharged) baseDamage *= 1.5F;
            if (isAlpha) baseDamage *= 3.0F;
            if (isAlpha && isCharged) baseDamage *= 1.5F;
            if (piercingPercentage > 0) {
                float piercingDamage = baseDamage * piercingPercentage;
                entity.attackEntityFrom(DamageSource.MAGIC, piercingDamage);
            }
            if (entity instanceof EntityPlayer) {
                ((EntityPlayer) entity).addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 120, 0));
            }
        }
        creeper.setDead();
    }

    private static void applyAlpha(EntityCreeper creeper) {
        creeper.addTag("alpha");
        creeper.setCustomNameTag("Alpha Creeper");
        creeper.setAlwaysRenderNameTag(true);
        creeper.addPotionEffect(new PotionEffect(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        if (creeper.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null) {
            creeper.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
            creeper.setHealth(40.0F);
        }
        if (creeper.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE) != null) {
            creeper.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        }
    }

    private static void spawnLightning(EntityCreeper creeper) {
        if (!creeper.world.isRemote) {
            EntityLightningBolt lightning = new EntityLightningBolt(creeper.world, creeper.posX, creeper.posY, creeper.posZ, true);
            creeper.onStruckByLightning(lightning);
        }
    }
}
