package io.github.tonycrane.tcpatch.mixin;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = WorkableMultiblockMachine.class, remap = false)
public interface MixinWorkableMultiblockMachine_TraitSubscriptionsAccessor {

    @Accessor("traitSubscriptions")
    List<ISubscription> tcpatch$getTraitSubscriptions();
}
