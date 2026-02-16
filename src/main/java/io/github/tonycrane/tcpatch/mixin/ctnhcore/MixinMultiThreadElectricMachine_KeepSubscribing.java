package io.github.tonycrane.tcpatch.mixin.ctnhcore;

import io.github.cpearl0.ctnhcore.api.recipe.MultiThreadRecipeLogic;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Backport of https://github.com/CTNH-Team/CTNH-Core/commit/0c96a4a393519ed819b509936c5aa9eed0052808
 */
@Mixin(targets = "io.github.cpearl0.ctnhcore.api.machine.multiblock.MultiThreadElectricMachine", remap = false)
@Pseudo
public abstract class MixinMultiThreadElectricMachine_KeepSubscribing {

    @Shadow(remap = false)
    public abstract MultiThreadRecipeLogic getRecipeLogic();

    public boolean keepSubscribing() {
        return getRecipeLogic().isWorking();
    }
}
