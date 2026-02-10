package io.github.tonycrane.tcpatch.mixin;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.utils.GTUtil;

import io.github.tonycrane.tcpatch.compat.ctnhbio.CtnhBioLivingMachineCompat;
import io.github.tonycrane.tcpatch.compat.ctnhbio.LivingMachineVoltageTracker;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WorkableTieredMachine.class, remap = false)
public abstract class MixinWorkableTieredMachine_CtnhBioNetworkOverclock {

    @Inject(method = "getOverclockVoltage", at = @At("HEAD"), cancellable = true)
    private void tcpatch$ctnhbio$getOverclockVoltage(CallbackInfoReturnable<Long> cir) {
        Object self = this;
        if (!CtnhBioLivingMachineCompat.isCtnhBioMachine(self)) return;

        NotifiableEnergyContainer energyContainer = ((TieredEnergyMachine) self).energyContainer;
        long baseVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());

        long now = ((MetaMachine) self).getOffsetTimer();
        boolean allowHistory = CtnhBioLivingMachineCompat.allowNetworkVoltageHistory(self);
        long networkVoltage = LivingMachineVoltageTracker.getRecentNetworkVoltage(self, now, allowHistory);

        long candidate = Math.max(baseVoltage, networkVoltage);
        int baseTier = GTUtil.getTierByVoltage(baseVoltage);
        int maxAllowedTier = Math.min(baseTier + 1, GTValues.V.length - 1);
        long maxAllowedVoltage = GTValues.V[maxAllowedTier];

        cir.setReturnValue(Math.min(candidate, maxAllowedVoltage));
    }

    @Inject(method = "getMaxOverclockTier", at = @At("HEAD"), cancellable = true)
    private void tcpatch$ctnhbio$getMaxOverclockTier(CallbackInfoReturnable<Integer> cir) {
        Object self = this;
        if (!CtnhBioLivingMachineCompat.isCtnhBioMachine(self)) return;

        // Ensure setOverclockTier() validation reflects the network voltage overclock rule.
        long voltage = ((WorkableTieredMachine) self).getOverclockVoltage();
        cir.setReturnValue((int) GTUtil.getTierByVoltage(voltage));
    }
}
