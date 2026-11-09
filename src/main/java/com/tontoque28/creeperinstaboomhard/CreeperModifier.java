package com.tontoque28.creeperinstaboomhard;

import com.tontoque28.creeperinstaboomhard.mixin.GoalSelectorAccessor;
import com.tontoque28.creeperinstaboomhard.mixin.MobAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;

public class CreeperModifier {

    public static final String TAG_PROCESSED = "CIB_Processed";
    public static final String TAG_EXPLODED = "CIB_Exploded";
    public static final String TAG_EXPLODING = "CIB_Exploding";
    public static final String TAG_ALPHA = "alpha";

    public static void onSpawn(Entity entity, ServerLevel level) {
        if (!(entity instanceof Creeper)) return;
        Creeper creeper = (Creeper) entity;

        List<WrappedGoal> goalsToRemove = new ArrayList<>();
        GoalSelector goalSelector = ((MobAccessor) creeper).creeperinstaboomhard$getGoalSelector();
        ((GoalSelectorAccessor) goalSelector).creeperinstaboomhard$getAvailableGoals().forEach(wrappedGoal -> {
            if (wrappedGoal.getGoal() instanceof SwellGoal) {
                goalsToRemove.add(wrappedGoal);
            }
        });
        goalsToRemove.forEach(goal -> goalSelector.removeGoal(goal.getGoal()));

        if (creeper.getTags().contains(TAG_PROCESSED)) return;
        creeper.addTag(TAG_PROCESSED);

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
                boolean isAlpha = creeper.getTags().contains(TAG_ALPHA) || (random.nextFloat() <= alphaChance);
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

    public static void onCreeperTick(Creeper creeper) {
        if (creeper.level.isClientSide) return;
        if (!creeper.isAlive()) return;

        creeper.setSwellDir(-1);

        if (creeper.getTags().contains(TAG_EXPLODED)) return;

        double triggerRadius = 4.0D;
        List<Player> players = creeper.level.getEntitiesOfClass(Player.class, creeper.getBoundingBox().inflate(triggerRadius));
        if (players.isEmpty()) return;
        players.removeIf(player -> player.isCreative() || player.isSpectator());
        if (players.isEmpty()) return;

        explodeNow(creeper);
    }

    public static boolean interceptExplosion(Creeper creeper) {
        if (creeper.level.isClientSide) return false;
        if (creeper.getTags().contains(TAG_EXPLODING)) return false;
        explodeNow(creeper);
        return true;
    }

    private static void explodeNow(Creeper creeper) {
        if (creeper.getTags().contains(TAG_EXPLODED)) return;
        creeper.addTag(TAG_EXPLODED);
        creeper.addTag(TAG_EXPLODING);

        boolean isAlpha = creeper.getTags().contains(TAG_ALPHA);
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
        Explosion.BlockInteraction interaction = creeper.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                ? Explosion.BlockInteraction.DESTROY
                : Explosion.BlockInteraction.NONE;
        creeper.level.explode(creeper, creeper.getX(), creeper.getY(), creeper.getZ(), explosionPower, false, interaction);

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
            if (entity instanceof Player) {
                ((Player) entity).addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));
            }
        }
        creeper.remove();
    }

    private static void applyAlpha(Creeper creeper) {
        creeper.addTag(TAG_ALPHA);
        creeper.setCustomName(new TextComponent("Alpha Creeper"));
        creeper.setCustomNameVisible(true);
        creeper.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        if (creeper.getAttribute(Attributes.MAX_HEALTH) != null) {
            creeper.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0D);
            creeper.setHealth(40.0F);
        }
        if (creeper.getAttribute(Attributes.FOLLOW_RANGE) != null) {
            creeper.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(64.0D);
        }
    }

    private static void spawnLightning(Creeper creeper) {
        if (creeper.level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) creeper.level;
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (lightning != null) {
                lightning.moveTo(creeper.getX(), creeper.getY(), creeper.getZ());
                lightning.setVisualOnly(true);
                creeper.thunderHit(serverLevel, lightning);
            }
        }
    }
}
