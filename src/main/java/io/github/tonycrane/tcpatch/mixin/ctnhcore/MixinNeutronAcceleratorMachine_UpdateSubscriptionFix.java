package io.github.tonycrane.tcpatch.mixin.ctnhcore;

import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;

import io.github.tonycrane.tcpatch.mixin.gtceu.MixinEnergyHatchPartMachine_EnergyContainerAccessor;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Backport of https://github.com/CTNH-Team/CTNH-Core/commit/e42f2b1d8e7e01a7c59145a110e48eb3fecdecc5
 */
@Mixin(targets = "io.github.cpearl0.ctnhcore.common.machine.multiblock.part.NeutronAcceleratorMachine", remap = false)
@Pseudo
public abstract class MixinNeutronAcceleratorMachine_UpdateSubscriptionFix {

    @Shadow(remap = false)
    private @Nullable TickableSubscription powerSubs;

    @Shadow(remap = false)
    private void energyChanged() {
        throw new AssertionError();
    }

    /**
     * @reason Keep subscription stable by reusing last subscription.
     */
    @Overwrite
    private void updateSubscription() {
        NotifiableEnergyContainer energyContainer =
                ((MixinEnergyHatchPartMachine_EnergyContainerAccessor) (Object) this).tcpatch$getEnergyContainer();

        if (energyContainer.getEnergyStored() > 0) {
            powerSubs = ((ITickSubscription) (Object) this).subscribeServerTick(powerSubs, this::energyChanged);
        } else if (powerSubs != null) {
            powerSubs.unsubscribe();
            powerSubs = null;
        }
    }
}
