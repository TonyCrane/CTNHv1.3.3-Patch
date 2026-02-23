package io.github.tonycrane.tcpatch.mixin.gtceu;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.RecipeRunner;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * For high parallel count, replace OR chance rolling with deterministic expected output,
 * to avoid huge per-run RNG/entry expansion costs.
 */
@Mixin(value = RecipeRunner.class, remap = false)
public abstract class MixinRecipeRunner_ChancedOutputExpectedOnHighParallel {

    @Unique
    private static final int TCPATCH$CHANCED_OUTPUT_EXPECTED_THRESHOLD = 500;

    @Shadow
    @Final
    private GTRecipe recipe;

    @Shadow
    @Final
    private IO io;

    @Shadow
    @Final
    private boolean isTick;

    @Shadow
    @Final
    private Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches;

    @Shadow
    @Final
    private Map<RecipeCapability<?>, List<Object>> recipeContents;

    @Shadow
    @Final
    private Map<RecipeCapability<?>, List<Object>> searchRecipeContents;

    @Shadow
    @Final
    private boolean simulated;

    @Overwrite
    private void fillContentMatchList(Map<RecipeCapability<?>, List<Content>> entries) {
        ChanceBoostFunction function = recipe.getType().getChanceFunction();
        int recipeTier = RecipeHelper.getPreOCRecipeEuTier(recipe);
        int chanceTier = recipeTier + recipe.ocLevel;
        int runs = recipe.parallels * recipe.batchParallels;

        for (var entry : entries.entrySet()) {
            RecipeCapability<?> cap = entry.getKey();
            if (!cap.doMatchInRecipe()) continue;

            ChanceLogic logic = recipe.getChanceLogicForCapability(cap, this.io, this.isTick);
            List<Content> chancedContents = new ArrayList<>();
            if (entry.getValue().isEmpty()) continue;

            var contentList = this.recipeContents.computeIfAbsent(cap, c -> new ArrayList<>());
            var searchContentList = this.searchRecipeContents.computeIfAbsent(cap, c -> new ArrayList<>());
            for (Content cont : entry.getValue()) {
                searchContentList.add(cont.content);

                if (simulated) continue;

                if (cont.chance >= cont.maxChance) {
                    contentList.add(cont.content);
                } else if (cont.chance > 0 || cont.tierChanceBoost > 0) {
                    chancedContents.add(cont);
                }
            }

            if (!chancedContents.isEmpty()) {
                if (io == IO.OUT && logic == ChanceLogic.OR && runs >= TCPATCH$CHANCED_OUTPUT_EXPECTED_THRESHOLD) {
                    tcpatch$appendExpectedChancedOutputs(contentList, cap, chancedContents, function, recipeTier,
                            chanceTier, runs);
                } else {
                    var cache = this.chanceCaches.get(cap);
                    chancedContents = logic.roll(chancedContents, function, recipeTier, chanceTier, cache, runs);
                    for (Content cont : chancedContents) {
                        contentList.add(cont.content);
                    }
                }
            }

            if (contentList.isEmpty()) recipeContents.remove(cap);
        }
    }

    @Unique
    private static <T> void tcpatch$appendExpectedChancedOutputs(List<Object> contentList, RecipeCapability<T> cap,
                                                                  List<Content> chancedContents,
                                                                  ChanceBoostFunction function,
                                                                  int recipeTier, int chanceTier, int runs) {
        for (Content cont : chancedContents) {
            int boostedChance = function.getBoostedChance(cont, recipeTier, chanceTier);
            if (boostedChance <= 0) continue;

            double expectedRuns = (double) runs * boostedChance / cont.maxChance;
            if (expectedRuns <= 0) continue;

            T scaled = cap.copyWithModifier(cap.of(cont.content), ContentModifier.multiplier(expectedRuns));
            contentList.add(scaled);
        }
    }
}
