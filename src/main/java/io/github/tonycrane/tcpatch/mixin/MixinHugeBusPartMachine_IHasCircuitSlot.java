package io.github.tonycrane.tcpatch.mixin;

import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "com.hepdd.gtmthings.common.block.machine.multiblock.part.HugeBusPartMachine", remap = false)
public abstract class MixinHugeBusPartMachine_IHasCircuitSlot implements IHasCircuitSlot {
}
