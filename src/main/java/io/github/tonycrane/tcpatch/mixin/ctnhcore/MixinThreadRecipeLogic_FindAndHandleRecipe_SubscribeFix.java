package io.github.tonycrane.tcpatch.mixin.ctnhcore;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import io.github.cpearl0.ctnhcore.api.recipe.MultiThreadRecipeLogic;
import io.github.tonycrane.tcpatch.mixin.gtceu.MixinRecipeLogic_Accessor;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Backport of https://github.com/CTNH-Team/CTNH-Core/commit/0c96a4a393519ed819b509936c5aa9eed0052808
 *         and https://github.com/CTNH-Team/CTNH-Core/commit/7a6de26aee9d06312cb7696983e7019971ef67f4
 */
@Mixin(targets = "io.github.cpearl0.ctnhcore.api.recipe.ThreadRecipeLogic", remap = false)
@Pseudo
public abstract class MixinThreadRecipeLogic_FindAndHandleRecipe_SubscribeFix {

    @Shadow(remap = false)
    boolean lockRecipe;

    @Shadow(remap = false)
    boolean threadProtect;

    @Overwrite
    public void findAndHandleRecipe() {
        RecipeLogic self = (RecipeLogic) (Object) this;
        IRecipeLogicMachine recipeLogicMachine = self.machine;
        MixinRecipeLogic_Accessor selfAccessor = (MixinRecipeLogic_Accessor) (Object) this;

        self.lastFailedMatches = null;

        // try to execute last recipe if possible
        if (recipeLogicMachine.keepSubscribing() && !(recipeLogicMachine.self().getOffsetTimer() % 60 == 0))
            return;

        @Nullable GTRecipe cachedLastRecipe = selfAccessor.tcpatch$getLastRecipe();
        if (!selfAccessor.tcpatch$isRecipeDirty() && cachedLastRecipe != null && tcpatch$checkRecipe(self, recipeLogicMachine, cachedLastRecipe).isSuccess()) {
            GTRecipe recipe = cachedLastRecipe;
            selfAccessor.tcpatch$setLastRecipe(null);
            //lastOriginRecipe = null;
            self.setupRecipe(recipe);
        } else if (!lockRecipe) { // try to find and handle a new recipe if not locked
            selfAccessor.tcpatch$setLastRecipe(null);
            //lastOriginRecipe = null;

            Iterator<GTRecipe> matches = self.searchRecipe();
            while (matches.hasNext()) {
                GTRecipe match = matches.next();
                if (match == null) continue;

                if (self.checkMatchedRecipeAvailable(match)) {
                    return;
                }

                if (self.lastFailedMatches == null) {
                    self.lastFailedMatches = new ArrayList<>();
                }
                self.lastFailedMatches.add(match);
            }
        }

        selfAccessor.tcpatch$setRecipeDirty(false);
    }

    private ActionResult tcpatch$checkRecipe(RecipeLogic self, IRecipeLogicMachine recipeLogicMachine, GTRecipe recipe) {
        if (threadProtect && recipeLogicMachine.getRecipeLogic() instanceof MultiThreadRecipeLogic multiThreadRecipeLogic &&
                multiThreadRecipeLogic.isRunningRecipe(recipe, self)) {
            return ActionResult.FAIL_NO_REASON;
        }

        var conditionResult = RecipeHelper.checkConditions(recipe, self);
        if (!conditionResult.isSuccess()) return conditionResult;
        return RecipeHelper.matchContents(recipeLogicMachine, recipe);
    }
}
