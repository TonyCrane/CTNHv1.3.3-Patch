package io.github.tonycrane.tcpatch.mixin.ctnhbio;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gregtechceu.gtceu.utils.GTUtil;

import io.github.tonycrane.tcpatch.compat.ctnhbio.CtnhBioLivingMachineCompat;
import io.github.tonycrane.tcpatch.compat.ctnhbio.LivingMachineVoltageTracker;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collection;

@Pseudo
@Mixin(targets = "com.moguang.ctnhbio.machine.braininavat.BrainInAVatMachine", remap = false)
public abstract class MixinBrainInAVatMachine_OverclockAndDoubt {

    @Shadow
    protected long lastWorkingTime;

    @Shadow
    boolean isDoubted;

    @Unique
    private NotifiableEnergyContainer tcpatch$getEnergyContainer() {
        return ((TieredEnergyMachine) (Object) this).energyContainer;
    }

    @Unique
    private long tcpatch$getOverclockVoltage() {
        return ((IOverclockMachine) (Object) this).getOverclockVoltage();
    }

    @Unique
    private void tcpatch$onChangedSafe() {
        try {
            var m = CtnhBioLivingMachineCompat.findNoArgMethod(((Object) this).getClass(), "onChanged");
            if (m != null) {
                m.invoke(this);
            }
        } catch (Throwable ignored) {
        }
    }

    @Unique
    private boolean tcpatch$isWorkingEnabled() {
        return ((IRecipeLogicMachine) (Object) this).isWorkingEnabled();
    }

    @Unique
    private int tcpatch$getTier() {
        return ((ITieredMachine) (Object) this).getTier();
    }

    @Overwrite
    public void doExplosion(float explosionPower) {
        // Call the same fallback behavior as living machines, but do NOT set oc=true.
        float inputTier = explosionPower - 1;
        LivingEntity entity = CtnhBioLivingMachineCompat.getMachineEntity(this);
        Level level = CtnhBioLivingMachineCompat.getLevel(this);
        if (level == null || entity == null || !entity.isAlive()) return;

        if (inputTier - tcpatch$getTier() >= 2) {
            entity.hurt(GTDamageTypes.ELECTRIC.source(level), entity.getMaxHealth());
        } else {
            entity.hurt(GTDamageTypes.ELECTRIC.source(level), tcpatch$getTier());
        }
    }

    @Overwrite
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add((IOpticalComputationProvider) (Object) this);

        boolean overclocked = GTUtil.getTierByVoltage(tcpatch$getOverclockVoltage()) > tcpatch$getTier();
        boolean ret = tcpatch$isWorkingEnabled() && consume(simulate, overclocked);
        if (!ret) return 0;

        if (!simulate) {
            Level level = CtnhBioLivingMachineCompat.getLevel(this);
            if (level != null) {
                lastWorkingTime = level.getGameTime();
                tcpatch$onChangedSafe();
            }
            long nowTick = level != null ? level.getGameTime() : CtnhBioLivingMachineCompat.getTimeStamp(this);
            byte chanceToDoubt = tcpatch$getChanceToDoubt(tcpatch$getTier());
            if (nowTick % 20 == 0 && !isDoubted && chanceToDoubt > 0 && GTValues.RNG.nextInt(Byte.MAX_VALUE) <= chanceToDoubt) {
                isDoubted = true;
            }
            if (overclocked) {
                tcpatch$applyOvervoltageDamageOncePerTick(CtnhBioLivingMachineCompat.getTimeStamp(this));
            }
        }

        int baseCWU = tcpatch$getCWUt(tcpatch$getTier());
        if (overclocked) {
            return 2 * baseCWU;
        }
        return baseCWU / (isDoubted ? 2 : 1);
    }

    @Overwrite
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add((IOpticalComputationProvider) (Object) this);

        if (!tcpatch$isWorkingEnabled()) return 0;

        boolean overclocked = GTUtil.getTierByVoltage(tcpatch$getOverclockVoltage()) > tcpatch$getTier();
        int baseCWU = tcpatch$getCWUt(tcpatch$getTier());
        if (overclocked) {
            return 2 * baseCWU;
        }
        return isDoubted ? baseCWU / 2 : baseCWU;
    }

    @Overwrite
    private boolean consume(boolean simulate) {
        boolean overclocked = GTUtil.getTierByVoltage(tcpatch$getOverclockVoltage()) > tcpatch$getTier();
        return consume(simulate, overclocked);
    }

    @Unique
    private boolean consume(boolean simulate, boolean overclocked) {
        double nut = overclocked ? 4 * tcpatch$getNUt(tcpatch$getTier()) : tcpatch$getNUt(tcpatch$getTier());
        long eut = overclocked ? 4 * tcpatch$getEUt(tcpatch$getTier()) : tcpatch$getEUt(tcpatch$getTier());

        NotifiableEnergyContainer energyContainer = tcpatch$getEnergyContainer();

        Object storage = tcpatch$getStorage();
        if (storage == null) return false;

        if (simulate) {
            double amount = tcpatch$invokeDouble(storage, "getAmount");
            return amount >= nut && energyContainer.getEnergyStored() >= eut;
        }

        double extracted = tcpatch$invokeDouble(storage, "extract", nut);
        if (extracted < nut) return false;
        return energyContainer.removeEnergy(eut) >= eut;
    }

    @Unique
    private static int tcpatch$getCWUt(int tier) {
        return tier >= GTValues.HV ? 1 << (tier - GTValues.HV) : 0;
    }

    @Unique
    private static double tcpatch$getNUt(int tier) {
        return tcpatch$getCWUt(tier) / 20.0;
    }

    @Unique
    private static long tcpatch$getEUt(int tier) {
        return GTValues.VA[tier];
    }

    @Unique
    private static byte tcpatch$getChanceToDoubt(int tier) {
        return (byte) (tier >= GTValues.IV ? (tier - GTValues.IV + 1) : 0);
    }

    @Unique
    private Object tcpatch$getStorage() {
        try {
            var method = ((Object) this).getClass().getMethod("getStorage");
            return method.invoke(this);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Unique
    private static double tcpatch$invokeDouble(Object target, String methodName, Object... args) {
        try {
            Class<?>[] argTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                argTypes[i] = args[i] instanceof Double ? double.class : args[i].getClass();
            }
            var method = target.getClass().getMethod(methodName, argTypes);
            Object result = method.invoke(target, args);
            return result instanceof Number n ? n.doubleValue() : 0.0;
        } catch (Throwable ignored) {
            return 0.0;
        }
    }

    @Unique
    private void tcpatch$applyOvervoltageDamageOncePerTick(long timeStamp) {
        if (!LivingMachineVoltageTracker.shouldHurtThisTick(this, timeStamp)) return;
        Level level = CtnhBioLivingMachineCompat.getLevel(this);
        if (level == null || level.isClientSide) return;
        LivingEntity entity = CtnhBioLivingMachineCompat.getMachineEntity(this);
        if (entity == null || !entity.isAlive()) return;
        entity.hurt(GTDamageTypes.ELECTRIC.source(level), tcpatch$getTier());
    }
}
