package io.github.tonycrane.tcpatch.mixin;

import io.github.tonycrane.tcpatch.compat.ctnhcore.SlaughterHouseMachineHooks;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorkableMultiblockMachine.class, remap = false)
public class MixinWorkableMultiblockMachine_SlaughterHouseCleanup {

    @Inject(method = "onStructureInvalid", at = @At("TAIL"))
    private void tcpatch$onStructureInvalidTail(CallbackInfo ci) {
        if ((Object) this instanceof SlaughterHouseMachineHooks hooks) {
            hooks.tcpatch$onStructureInvalidOrUnload();
        }
    }

    @Inject(method = "onUnload", at = @At("TAIL"))
    private void tcpatch$onUnloadTail(CallbackInfo ci) {
        if ((Object) this instanceof SlaughterHouseMachineHooks hooks) {
            hooks.tcpatch$onStructureInvalidOrUnload();
        }
    }
}
