package io.github.tonycrane.tcpatch.mixin.gtceu;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Remap some unreasonable material durationOverride in CTNH-Core.
 */
@Mixin(value = BlastProperty.Builder.class, remap = false)
public class MixinBlastPropertyBuilder_DurationOverrideRemap {

    @ModifyVariable(method = "blastStats(II)Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/BlastProperty$Builder;",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true)
    private int tcpatch$remapDurationOverride(int durationOverride) {
        return switch (durationOverride) {
            case 2000 -> 900;
            case 3000 -> 1000;
            case 4000 -> 1200;
            case 6000 -> 1800;
            default -> durationOverride;
        };
    }
}
