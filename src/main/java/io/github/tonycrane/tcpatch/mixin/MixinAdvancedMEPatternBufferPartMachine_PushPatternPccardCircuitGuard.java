package io.github.tonycrane.tcpatch.mixin;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import tech.luckyblock.mcmod.ctnhenergy.common.machine.advancedpatternbuffer.ProgrammableSlotRecipeHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Field;

/**
 * Backport of https://github.com/CTNH-Team/CTNH-Energy/commit/6e106176d24629743dad70ccba0f0f3cef6efb02
 */
@Pseudo
@Mixin(targets = "tech.luckyblock.mcmod.ctnhenergy.common.machine.advancedpatternbuffer.AdvancedMEPatternBufferPartMachine", remap = false)
public abstract class MixinAdvancedMEPatternBufferPartMachine_PushPatternPccardCircuitGuard {

    @Unique
    private static volatile Field TCPATCH$CIRCUIT_INVENTORY_FIELD;

    @Redirect(method = "pushPattern", at = @At(value = "INVOKE", target = "Ltech/luckyblock/mcmod/ctnhenergy/common/machine/advancedpatternbuffer/ProgrammableSlotRecipeHandler;setCircuit(II)V"))
    private void tcpatch$guardUnsetPccardCircuit(ProgrammableSlotRecipeHandler handler, int index, int circuit) {
        int fixed = circuit;
        if (fixed < 0 || fixed > IntCircuitBehaviour.CIRCUIT_MAX) {
            Integer fallback = tcpatch$getIntegratedCircuitConfig();
            if (fallback == null) return;
            fixed = fallback;
        }
        handler.setCircuit(index, fixed);
    }

    @Unique
    private Integer tcpatch$getIntegratedCircuitConfig() {
        NotifiableItemStackHandler circuitInv = tcpatch$getCircuitInventory();
        if (circuitInv == null) return null;

        var stack = circuitInv.storage.getStackInSlot(0);
        if (!IntCircuitBehaviour.isIntegratedCircuit(stack)) return null;
        return IntCircuitBehaviour.getCircuitConfiguration(stack);
    }

    @Unique
    private NotifiableItemStackHandler tcpatch$getCircuitInventory() {
        Field field = TCPATCH$CIRCUIT_INVENTORY_FIELD;
        if (field == null) {
            field = tcpatch$findFieldInHierarchy(this.getClass(), "circuitInventory");
            TCPATCH$CIRCUIT_INVENTORY_FIELD = field;
        }
        if (field == null) return null;
        try {
            Object value = field.get(this);
            return value instanceof NotifiableItemStackHandler inv ? inv : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Unique
    private static Field tcpatch$findFieldInHierarchy(Class<?> type, String name) {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }
}
