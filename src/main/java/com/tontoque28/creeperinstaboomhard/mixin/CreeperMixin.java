package com.tontoque28.creeperinstaboomhard.mixin;

import com.tontoque28.creeperinstaboomhard.CreeperModifier;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void creeperinstaboomhard$onTick(CallbackInfo ci) {
        CreeperModifier.onCreeperTick((Creeper) (Object) this);
    }
}
