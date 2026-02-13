package io.github.tonycrane.tcpatch.mixin.ctnhcore;

import java.util.Arrays;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifierList;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

@Pseudo
@Mixin(targets = "io.github.cpearl0.ctnhcore.registry.machines.multiblock.MultiblocksA", remap = false)
public abstract class MixinMultiblocksA_SlaughterHouse_BatchMode {

    @Shadow
    public static MultiblockMachineDefinition SLAUGHTER_HOUSE;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void tcpatch$slaughterHouse$addBatchModeTail(CallbackInfo ci) {
        if (SLAUGHTER_HOUSE == null) return;
        RecipeModifier current = SLAUGHTER_HOUSE.getRecipeModifier();
        SLAUGHTER_HOUSE.setRecipeModifier(tcpatch$slaughterHouse$appendBatchMode(current));
    }

    private static RecipeModifier tcpatch$slaughterHouse$appendBatchMode(RecipeModifier original) {
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
