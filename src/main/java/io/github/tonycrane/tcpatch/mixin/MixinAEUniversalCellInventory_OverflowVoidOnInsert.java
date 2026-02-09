package io.github.tonycrane.tcpatch.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

@Pseudo
@Mixin(targets = "com.wintercogs.ae2omnicells.common.me.AEUniversalCellInventory", remap = false)
public abstract class MixinAEUniversalCellInventory_OverflowVoidOnInsert {

    private static volatile Method tcpatch$handleOverflowVoidOnInsert;

    @Unique
    private boolean tcpatch$passedPartitionAndNestChecks;

    private long tcpatch$callHandleOverflowVoidOnInsert(Object what, long amount, long inserted) {
        try {
            Method method = tcpatch$handleOverflowVoidOnInsert;
            if (method == null) {
                method = tcpatch$resolveHandleOverflowVoidOnInsert();
                tcpatch$handleOverflowVoidOnInsert = method;
            }
            if (method == null) {
                return inserted;
            }
            Object result = method.invoke(this, what, amount, inserted);
            return result instanceof Long l ? l : inserted;
        } catch (Throwable ignored) {
            return inserted;
        }
    }

    private Method tcpatch$resolveHandleOverflowVoidOnInsert() {
        try {
            for (Method m : this.getClass().getDeclaredMethods()) {
                if (!m.getName().equals("handleOverflowVoidOnInsert") || m.getParameterCount() != 3) continue;
                Class<?>[] p = m.getParameterTypes();
                if (p[1] != long.class || p[2] != long.class) continue;
                m.setAccessible(true);
                return m;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    @Inject(method = "insert", at = @At("HEAD"))
    private void tcpatch$resetPerCallFlag(
            @Coerce Object what,
            long amount,
            @Coerce Object mode,
            @Coerce Object source,
            CallbackInfoReturnable<Long> cir) {
        tcpatch$passedPartitionAndNestChecks = false;
    }

    /**
     * The first AEKey#getType() call happens after these checks in insert():
     * - matchesPartitionAndUpgrades(what)
     * - canNestStorageCells(what)
     *
     * We only want overflow-void semantics after these checks have passed.
     */
    @Inject(
            method = "insert",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/stacks/AEKey;getType()Lappeng/api/stacks/AEKeyType;",
                    shift = At.Shift.BEFORE))
    private void tcpatch$markAfterPartitionAndNestChecks(
            @Coerce Object what,
            long amount,
            @Coerce Object mode,
            @Coerce Object source,
            CallbackInfoReturnable<Long> cir) {
        tcpatch$passedPartitionAndNestChecks = true;
    }

    @Inject(method = "insert", at = @At("RETURN"), cancellable = true)
    private void tcpatch$applyOverflowVoidOnAnyReturn(
            @Coerce Object what,
            long amount,
            @Coerce Object mode,
            @Coerce Object source,
            CallbackInfoReturnable<Long> cir) {
        if (!tcpatch$passedPartitionAndNestChecks) {
            return;
        }
        long inserted = cir.getReturnValueJ();
        cir.setReturnValue(tcpatch$callHandleOverflowVoidOnInsert(what, amount, inserted));
    }
}
