package com.tontoque28.creeperinstaboomhard.mixin;

import java.util.Set;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GoalSelector.class)
public interface GoalSelectorAccessor {

    @Accessor("availableGoals")
    Set<WrappedGoal> creeperinstaboomhard$getAvailableGoals();
}
