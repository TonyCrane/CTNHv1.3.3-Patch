package io.github.tonycrane.tcpatch.mixin;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuuki1293.pccard.impl.PatternProviderLogicImpl;
import yuuki1293.pccard.wrapper.IAEPattern;

/**
 * Backport of https://github.com/CTNH-Team/CTNH-Energy/commit/6e106176d24629743dad70ccba0f0f3cef6efb02
 */
@Mixin(value = PatternProviderLogicImpl.class, remap = false)
public class MixinPatternProviderLogicImpl_DefaultCircuit {

    @ModifyConstant(method = "updatePatterns", constant = @Constant(intValue = 0))
    private static int replaceDefaultCircuit(int constant) {
        return -1;
    }

    @Inject(method = "setInvNumber", at = @At("HEAD"), cancellable = true)
    private static void ignore(NotifiableItemStackHandler inv, IAEPattern details, CallbackInfo ci) {
        if (details.pCCard$getNumber() == -1) {
            ci.cancel();
        }
    }
}