package io.github.tonycrane.tcpatch.mixin.gtceu;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RecipeLogic.class, remap = false)
public interface MixinRecipeLogic_Accessor {

    @Accessor("recipeDirty")
    boolean tcpatch$isRecipeDirty();

    @Accessor("recipeDirty")
    void tcpatch$setRecipeDirty(boolean recipeDirty);

    @Accessor("lastRecipe")
    @Nullable
    GTRecipe tcpatch$getLastRecipe();

    @Accessor("lastRecipe")
    void tcpatch$setLastRecipe(@Nullable GTRecipe recipe);
}
