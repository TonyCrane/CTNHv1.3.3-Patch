package io.github.tonycrane.tcpatch.mixin.gtceu;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Helper of MixinNeutronAcceleratorMachine_UpdateSubscriptionFix
 */
@Mixin(value = EnergyHatchPartMachine.class, remap = false)
public interface MixinEnergyHatchPartMachine_EnergyContainerAccessor {

    @Accessor("energyContainer")
    NotifiableEnergyContainer tcpatch$getEnergyContainer();
}
