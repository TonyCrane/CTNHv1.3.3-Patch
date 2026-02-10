package io.github.tonycrane.tcpatch.compat.ctnhbio;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.lang.reflect.Method;

public final class CtnhBioLivingMachineCompat {

    private CtnhBioLivingMachineCompat() {}

    public static boolean isCtnhBioMachine(Object machine) {
        return machine != null && machine.getClass().getName().startsWith("com.moguang.ctnhbio.");
    }

    public static boolean isBrainInAVat(Object machine) {
        return machine != null && machine.getClass().getName().equals("com.moguang.ctnhbio.machine.braininavat.BrainInAVatMachine");
    }

    public static boolean allowNetworkVoltageHistory(Object machine) {
        if (isBrainInAVat(machine)) {
            return true;
        }
        return isRecipeActive(machine);
    }

    public static boolean shouldApplyOvervoltageDamage(Object machine) {
        if (isBrainInAVat(machine)) {
            return false;
        }
        return isRecipeActive(machine);
    }

    public static boolean isRecipeActive(Object machine) {
        if (machine instanceof IRecipeLogicMachine rlm) {
            try {
                return rlm.getRecipeLogic() != null && rlm.getRecipeLogic().isActive();
            } catch (Throwable ignored) {
                return false;
            }
        }
        return false;
    }

    public static Level getLevel(Object machine) {
        Object result = invokeNoArg(machine, "getLevel");
        return result instanceof Level level ? level : null;
    }

    public static long getTimeStamp(Object machine) {
        Object result = invokeNoArg(machine, "getOffsetTimer");
        if (result instanceof Number n) {
            return n.longValue();
        }
        Level level = getLevel(machine);
        return level != null ? level.getGameTime() : 0L;
    }

    public static LivingEntity getMachineEntity(Object machine) {
        Object result = invokeNoArg(machine, "getMachineEntity");
        return result instanceof LivingEntity living ? living : null;
    }

    public static Object invokeNoArg(Object target, String name) {
        if (target == null) return null;
        try {
            Method m = findNoArgMethod(target.getClass(), name);
            if (m == null) return null;
            return m.invoke(target);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static Method findNoArgMethod(Class<?> cls, String name) {
        Class<?> c = cls;
        while (c != null && c != Object.class) {
            try {
                Method m = c.getDeclaredMethod(name);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException ignored) {
                c = c.getSuperclass();
            } catch (Throwable ignored) {
                return null;
            }
        }
        try {
            Method m = cls.getMethod(name);
            m.setAccessible(true);
            return m;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
