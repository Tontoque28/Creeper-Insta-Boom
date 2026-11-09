package com.tontoque28.creeperinstaboomhard.mixin;

import com.tontoque28.creeperinstaboomhard.CreeperModifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(
            method = "explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Explosion$BlockInteraction;)Lnet/minecraft/world/level/Explosion;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void creeperinstaboomhard$onExplode(Entity exploder, DamageSource source, ExplosionDamageCalculator calculator, double x, double y, double z, float radius, boolean fire, Explosion.BlockInteraction interaction, CallbackInfoReturnable<Explosion> cir) {
        if (exploder instanceof Creeper && CreeperModifier.interceptExplosion((Creeper) exploder)) {
            cir.setReturnValue(new Explosion((Level) (Object) this, exploder, source, calculator, x, y, z, radius, fire, Explosion.BlockInteraction.NONE));
        }
    }
}
