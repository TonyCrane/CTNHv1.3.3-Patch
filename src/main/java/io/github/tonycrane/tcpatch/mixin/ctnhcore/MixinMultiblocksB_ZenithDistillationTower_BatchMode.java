package io.github.tonycrane.tcpatch.mixin.ctnhcore;

import java.util.Arrays;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifierList;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import io.github.cpearl0.ctnhcore.registry.machines.multiblock.MultiblocksB;

@Mixin(value = MultiblocksB.class, remap = false)
public abstract class MixinMultiblocksB_ZenithDistillationTower_BatchMode {

    @Shadow
    @Final
    public static MultiblockMachineDefinition ZENITH_DISTILLATION_TOWER;

    @Unique
    private static boolean tcpatch$zenithDistillationTower$patched;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void tcpatch$zenithDistillationTower$addBatchModeTail(CallbackInfo ci) {
        if (tcpatch$zenithDistillationTower$patched) return;
        tcpatch$zenithDistillationTower$patched = true;

        if (ZENITH_DISTILLATION_TOWER == null) return;
        RecipeModifier current = ZENITH_DISTILLATION_TOWER.getRecipeModifier();
        ZENITH_DISTILLATION_TOWER.setRecipeModifier(tcpatch$zenithDistillationTower$appendBatchMode(current));
    }

    @Unique
    private static RecipeModifier tcpatch$zenithDistillationTower$appendBatchMode(RecipeModifier original) {
        if (original == null) return new RecipeModifierList(GTRecipeModifiers.BATCH_MODE);
        if (original == GTRecipeModifiers.BATCH_MODE) return original;

        if (original instanceof RecipeModifierList list) {
            RecipeModifier[] modifiers = list.getModifiers();
            for (RecipeModifier modifier : modifiers) {
                if (modifier == GTRecipeModifiers.BATCH_MODE) return original;
            }
            RecipeModifier[] appended = Arrays.copyOf(modifiers, modifiers.length + 1);
            appended[appended.length - 1] = GTRecipeModifiers.BATCH_MODE;
            return new RecipeModifierList(appended);
        }

        return new RecipeModifierList(original, GTRecipeModifiers.BATCH_MODE);
    }
}
