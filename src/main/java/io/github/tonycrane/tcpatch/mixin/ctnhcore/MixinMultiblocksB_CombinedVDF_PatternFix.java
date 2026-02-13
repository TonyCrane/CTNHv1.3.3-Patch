package io.github.tonycrane.tcpatch.mixin.ctnhcore;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fixes COMBINED_VAPOR_DEPOSITION_FACILITY pattern registration in CTNH-Core.
 *
 * The original code accidentally called setExactLimit(1) on the whole "A" predicate chain,
 * which effectively breaks the pattern matching.
 */
@Pseudo
@Mixin(targets = "io.github.cpearl0.ctnhcore.registry.machines.multiblock.MultiblocksB", remap = false)
public abstract class MixinMultiblocksB_CombinedVDF_PatternFix {

    @Shadow
    @Final
    private static MultiblockMachineDefinition COMBINED_VAPOR_DEPOSITION_FACILITY;

    @Unique
    private static boolean tcpatch$combinedVdfPatched;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void tcpatch$patchCombinedVdfPattern(CallbackInfo ci) {
        if (tcpatch$combinedVdfPatched) return;
        tcpatch$combinedVdfPatched = true;

        var definition = COMBINED_VAPOR_DEPOSITION_FACILITY;
        if (definition == null) return;

        definition.setPatternFactory(GTMemoizer.memoize(() -> FactoryBlockPattern.start()
                .aisle("ABBBA", "BACAB", "BCCCB", "BACAB", "ABBBA")
                .aisle("BDDDB", "AEFEA", "AGHGA", "AEFEA", "BDDDB")
                .aisle("BDDDB", "AFFFA", "AIFIA", "AFFFA", "BDDDB")
                .aisle("BDDDB", "AEFEA", "AGHGA", "AEFEA", "BDDDB")
                .aisle("ABBBA", "BACAB", "BC@CB", "BACAB", "ABBBA")
                .where('A', Predicates.blocks(GTBlocks.MACHINE_CASING_LuV.get())
                        .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                        .or(Predicates.abilities(PartAbility.COMPUTATION_DATA_RECEPTION).setExactLimit(1)
                                .setPreviewCount(1))
                        .or(Predicates.autoAbilities(true, false, false)))
                .where('B', Predicates.blocks(GTBlocks.HERMETIC_CASING_LuV.get()))
                .where('C', Predicates.blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                .where('@', Predicates.controller(Predicates.blocks(definition.get())))
                .where('D', Predicates.blocks(GTBlocks.HIGH_POWER_CASING.get()))
                .where('E', Predicates.blocks(GTBlocks.COIL_HSSG.get()))
                .where('F', Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                .where('G', Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.get()))
                .where('H', Predicates.blocks(GCYMBlocks.MOLYBDENUM_DISILICIDE_COIL_BLOCK.get()))
                .where('I', Predicates.blocks(GCYMBlocks.MOLYBDENUM_DISILICIDE_COIL_BLOCK.get()))
                .build()));

        // Ensure JEI/XEI previews use the updated patternFactory instead of any pre-baked shapes.
        // MultiblockMachineDefinition#getMatchingShapes falls back to patternFactory when shapes is empty.
        definition.setShapes(java.util.List::of);
    }
}
