package io.github.tonycrane.tcpatch.mixin.gtceu;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ListIterator;

/**
 * Equivalent backport of https://github.com/GregTechCEu/GregTech-Modern/pull/4290
 */
@Mixin(value = NotifiableItemStackHandler.class, remap = false)
public class MixinNotifiableItemStackHandler_BlockIntProviderOutputWhenFull {

    @Redirect(
            method = "handleRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/ListIterator;remove()V",
                    ordinal = 2
            )
    )
    private static void tcpatch$doNotVoidIntProviderOutputWhenFull(ListIterator<?> iterator) { }
}
