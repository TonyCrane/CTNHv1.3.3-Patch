package io.github.tonycrane.tcpatch.mixin;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

/**
 * Fix the logic of naquadahReactor and rocketEngine recipeModifiers.
 */
@Pseudo
@Mixin(targets = "io.github.cpearl0.ctnhcore.registry.CTNHRecipeModifiers", remap = false)
public abstract class MixinCTNHRecipeModifiers_GeneratorRecipeModifierFix {

    @Unique
    private static final String TCPATCH$EFFICIENCY_GENERATOR_MACHINE =
            "io.github.cpearl0.ctnhcore.common.machine.simple.EfficiencyGeneratorMachine";

    /**
     * naquadahReactor should support parallels (based on overclock voltage) and scale
     * eut + duration accordingly.
     */
    @Overwrite
    public static ModifierFunction naquadahReactor(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        Class<?> expected = tcpatch$efficiencyGeneratorMachineClass();
        if (expected == null || !expected.isInstance(machine)) {
            return expected == null ? ModifierFunction.NULL : RecipeModifier.nullWrongType(expected, machine);
        }

        long recipeEUt = recipe.getOutputEUt().getTotalEU();
        if (recipeEUt <= 0) return ModifierFunction.NULL;

        long overclockVoltage = tcpatch$getOverclockVoltage(machine);
        int maxParallel = (int) (overclockVoltage / recipeEUt);
        if (maxParallel <= 0) return ModifierFunction.NULL;

        int multiplier = ParallelLogic.getParallelAmountFast(machine, recipe, maxParallel);
        if (multiplier <= 0) return ModifierFunction.NULL;

        int efficiency = tcpatch$getEfficiency(machine);
        return ModifierFunction.builder()
                .eutMultiplier(multiplier)
                .durationMultiplier(((double) efficiency / 100) / multiplier)
                .build();
    }

    /**
     * rocketEngine should behave like an efficiency generator recipe modifier, enabling
     * parallels with input/output scaling and efficiency-based duration.
     */
    @Overwrite
    public static ModifierFunction rocketEngine(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        Class<?> expected = tcpatch$efficiencyGeneratorMachineClass();
        if (expected == null || !expected.isInstance(machine)) {
            return expected == null ? ModifierFunction.NULL : RecipeModifier.nullWrongType(expected, machine);
        }

        long recipeEUt = recipe.getOutputEUt().getTotalEU();
        if (recipeEUt <= 0) return ModifierFunction.NULL;

        long overclockVoltage = tcpatch$getOverclockVoltage(machine);
        int maxParallel = (int) (overclockVoltage / recipeEUt);
        if (maxParallel <= 0) return ModifierFunction.NULL;

        int parallels = ParallelLogic.getParallelAmountFast(machine, recipe, maxParallel);
        if (parallels <= 0) return ModifierFunction.NULL;

        int efficiency = tcpatch$getEfficiency(machine);
        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(parallels))
                .outputModifier(ContentModifier.multiplier(parallels))
                .eutMultiplier(parallels)
                .parallels(parallels)
                .durationMultiplier((double) efficiency / 100)
                .build();
    }

    @Unique
    private static Class<?> tcpatch$efficiencyGeneratorMachineClass() {
        try {
            return Class.forName(TCPATCH$EFFICIENCY_GENERATOR_MACHINE);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Unique
    private static int tcpatch$getEfficiency(MetaMachine machine) {
        try {
            Class<?> clazz = tcpatch$efficiencyGeneratorMachineClass();
            if (clazz == null) return 100;
            var field = clazz.getField("efficiency");
            Object value = field.get(machine);
            if (value instanceof Integer i) return i;
            return 100;
        } catch (Throwable ignored) {
            return 100;
        }
    }

    @Unique
    private static long tcpatch$getOverclockVoltage(MetaMachine machine) {
        if (machine instanceof IOverclockMachine overclockMachine) {
            return overclockMachine.getOverclockVoltage();
        }
        try {
            var method = machine.getClass().getMethod("getOverclockVoltage");
            Object value = method.invoke(machine);
            if (value instanceof Long l) return l;
            if (value instanceof Number n) return n.longValue();
            return 0L;
        } catch (Throwable ignored) {
            return 0L;
        }
    }
}
