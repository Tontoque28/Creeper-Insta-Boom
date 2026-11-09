package com.tontoque28.creeperinstaboomhard;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.goal.CreeperSwellGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Explosion;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Mod.EventBusSubscriber(modid = CreeperInstaBoomHard.MODID)
public class CreeperModifier {

    private static final Field AVAILABLE_GOALS = ObfuscationReflectionHelper.findField(GoalSelector.class, "field_220892_d");

    @SuppressWarnings("unchecked")
    private static Set<PrioritizedGoal> availableGoals(GoalSelector selector) {
        try {
            return (Set<PrioritizedGoal>) AVAILABLE_GOALS.get(selector);
        } catch (IllegalAccessException e) {
            return Collections.emptySet();
        }
    }

    @SubscribeEvent
    public static void onSpawn(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof CreeperEntity)) return;
        if (event.getWorld().isClientSide()) return;
        CreeperEntity creeper = (CreeperEntity) event.getEntity();

        List<PrioritizedGoal> goalsToRemove = new ArrayList<>();
        availableGoals(creeper.goalSelector).forEach(wrappedGoal -> {
            if (wrappedGoal.getGoal() instanceof CreeperSwellGoal) {
                goalsToRemove.add(wrappedGoal);
            }
        });
        goalsToRemove.forEach(goal -> creeper.goalSelector.removeGoal(goal.getGoal()));

        if (creeper.getPersistentData().getBoolean("CIB_Processed")) return;
        creeper.getPersistentData().putBoolean("CIB_Processed", true);

        Random random = creeper.getRandom();
        boolean isNight = !creeper.level.isDay();
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
                if (!creeper.isPowered() && random.nextFloat() <= chargedChance) {
                    spawnLightning(creeper);
                }
                if (creeper.isPowered()) {
                    speedMultiplier += 0.2D;
                }
                break;
            case HARD:
                baseSpeed = 0.30D;
                speedMultiplier = 1.5D;

                float hardChargedChance = isNight ? 0.35F : 0.10F;
                if (!creeper.isPowered() && random.nextFloat() <= hardChargedChance) {
                    spawnLightning(creeper);
                }

                float alphaChance = 0.0F;
                Difficulty worldDiff = creeper.level.getDifficulty();
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

                boolean isCharged = creeper.isPowered();
                if (isCharged) speedMultiplier = 1.75D;
                if (isAlpha) {
                    if (isCharged) speedMultiplier = 2.25D;
                    else speedMultiplier = 2.00D;
                }
                break;
        }

        double finalSpeed = baseSpeed * speedMultiplier;
        if (creeper.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            creeper.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(finalSpeed);
        }
        if (creeper.getAttribute(Attributes.FOLLOW_RANGE) != null) {
            creeper.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(64.0D);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCreeperTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof CreeperEntity)) return;
        CreeperEntity creeper = (CreeperEntity) event.getEntityLiving();
        if (creeper.level.isClientSide()) return;
        if (!creeper.isAlive()) return;

        creeper.setSwellDir(-1);

        if (creeper.getPersistentData().getBoolean("CIB_Exploded")) return;

        double triggerRadius = 4.0D;
        List<PlayerEntity> players = creeper.level.getEntitiesOfClass(PlayerEntity.class, creeper.getBoundingBox().inflate(triggerRadius));
        if (players.isEmpty()) return;
        players.removeIf(player -> player.isCreative() || player.isSpectator());
        if (players.isEmpty()) return;

        explodeNow(creeper);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onExplosionStart(ExplosionEvent.Start event) {
        if (event.getWorld().isClientSide()) return;
        if (event.getExplosion().getExploder() instanceof CreeperEntity) {
            CreeperEntity creeper = (CreeperEntity) event.getExplosion().getExploder();
            if (creeper.getPersistentData().getBoolean("CIB_Exploding")) return;
            event.setCanceled(true);
            explodeNow(creeper);
        }
    }

    private static void explodeNow(CreeperEntity creeper) {
        if (creeper.getPersistentData().getBoolean("CIB_Exploded")) return;
        creeper.getPersistentData().putBoolean("CIB_Exploded", true);
        creeper.getPersistentData().putBoolean("CIB_Exploding", true);

        boolean isAlpha = creeper.getTags().contains("alpha");
        boolean isCharged = creeper.isPowered();

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
        Explosion.Mode mode = ForgeEventFactory.getMobGriefingEvent(creeper.level, creeper) ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
        creeper.level.explode(creeper, creeper.getX(), creeper.getY(), creeper.getZ(), explosionPower, false, mode);

        double radius = explosionPower * 2.0D;
        List<Entity> entities = creeper.level.getEntities(creeper, creeper.getBoundingBox().inflate(radius));
        for (Entity entity : entities) {
            if (entity == creeper) continue;
            float baseDamage = 8.0F;
            if (isCharged) baseDamage *= 1.5F;
            if (isAlpha) baseDamage *= 3.0F;
            if (isAlpha && isCharged) baseDamage *= 1.5F;
            if (piercingPercentage > 0) {
                float piercingDamage = baseDamage * piercingPercentage;
                entity.hurt(DamageSource.MAGIC, piercingDamage);
            }
            if (entity instanceof PlayerEntity) {
                ((PlayerEntity) entity).addEffect(new EffectInstance(Effects.WEAKNESS, 120, 0));
            }
        }
        creeper.remove();
    }

    private static void applyAlpha(CreeperEntity creeper) {
        creeper.addTag("alpha");
        creeper.setCustomName(new StringTextComponent("Alpha Creeper"));
        creeper.setCustomNameVisible(true);
        creeper.addEffect(new EffectInstance(Effects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        if (creeper.getAttribute(Attributes.MAX_HEALTH) != null) {
            creeper.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0D);
            creeper.setHealth(40.0F);
        }
        if (creeper.getAttribute(Attributes.FOLLOW_RANGE) != null) {
            creeper.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(64.0D);
        }
    }

    private static void spawnLightning(CreeperEntity creeper) {
        if (creeper.level instanceof ServerWorld) {
            ServerWorld serverLevel = (ServerWorld) creeper.level;
            LightningBoltEntity lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (lightning != null) {
                lightning.moveTo(creeper.getX(), creeper.getY(), creeper.getZ());
                lightning.setVisualOnly(true);
                creeper.thunderHit(serverLevel, lightning);
            }
        }
    }
}
