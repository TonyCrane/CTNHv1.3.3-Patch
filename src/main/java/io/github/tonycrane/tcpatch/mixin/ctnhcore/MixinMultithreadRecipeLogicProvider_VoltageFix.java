package io.github.tonycrane.tcpatch.mixin.ctnhcore;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;


/**
 * Backport of https://github.com/GregTechCEu/GregTech-Modern/pull/4002
 * and some improvements to handle more energy hatch part machine.
 */
@Mixin(targets = "io.github.cpearl0.ctnhcore.api.jade.MultithreadRecipeLogicProvider", remap = false)
@Pseudo
public abstract class MixinMultithreadRecipeLogicProvider_VoltageFix {

    @Overwrite
    public static long getVoltage(RecipeLogic capability) {
        long voltage = -1;
        if (capability.machine instanceof SimpleTieredMachine machine) {
            voltage = GTValues.V[machine.getTier()];
        } else if (capability.machine instanceof SimpleGeneratorMachine machine) {
            voltage = GTValues.V[machine.getTier()];
        } else if (capability.machine instanceof WorkableElectricMultiblockMachine machine) {
            var handlers = machine.getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP);
            if (handlers.isEmpty()) handlers = machine.getCapabilitiesFlat(IO.OUT, EURecipeCapability.CAP);
            for (IRecipeHandler<?> handler : handlers) {
                if (handler instanceof IEnergyContainer container) {
                    voltage = Math.max(voltage, Math.max(container.getInputVoltage(), container.getOutputVoltage()));
                }
            }
        }
        // default display as LV, this shouldn't happen because a machine is either electric or steam
        if (voltage == -1) voltage = 32;
        return voltage;
    }
}
