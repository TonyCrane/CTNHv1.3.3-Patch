package io.github.tonycrane.tcpatch.mixin;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.utils.GTUtil;

import io.github.tonycrane.tcpatch.compat.ctnhbio.CtnhBioLivingMachineCompat;
import io.github.tonycrane.tcpatch.compat.ctnhbio.LivingMachineVoltageTracker;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import com.gregtechceu.gtceu.common.data.GTDamageTypes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = NotifiableEnergyContainer.class, remap = false)
public abstract class MixinNotifiableEnergyContainer_LivingMachineNetworkVoltage {

    @Shadow
    protected long amps;

    @Shadow
    protected long lastTimeStamp;

    @Shadow
    public abstract long getInputAmperage();

    @Shadow
    public abstract long getEnergyCapacity();

    @Shadow
    public abstract long getEnergyStored();

    @Shadow
    public abstract boolean inputsEnergy(Direction side);

    @Shadow
    public abstract long getInputVoltage();

    @Shadow
    public abstract void setEnergyStored(long energyStored);

    @Inject(method = "acceptEnergyFromNetwork", at = @At("HEAD"), cancellable = true)
    private void tcpatch$acceptLivingMachineNetworkVoltage(Direction side, long voltage, long amperage,
                                                           CallbackInfoReturnable<Long> cir) {
        Object machine = tcpatch$getMachineReflect();
        // Only apply to CTNH-Bio living machines (and subclasses).
        if (!CtnhBioLivingMachineCompat.isCtnhBioMachine(machine)) {
            return;
        }

        long latestTimeStamp = CtnhBioLivingMachineCompat.getTimeStamp(machine);
        if (lastTimeStamp < latestTimeStamp) {
            amps = 0;
            lastTimeStamp = latestTimeStamp;
        }

        if (amps >= getInputAmperage()) {
            cir.setReturnValue(0L);
            return;
        }

        long canAccept = getEnergyCapacity() - getEnergyStored();
        if (voltage <= 0L || !(side == null || inputsEnergy(side))) {
            cir.setReturnValue(0L);
            return;
        }

        int machineTier = GTUtil.getTierByVoltage(getInputVoltage());
        int incomingTier = GTUtil.getTierByVoltage(voltage);

        LivingMachineVoltageTracker.onPacketSeen(machine, voltage, latestTimeStamp);

        if (incomingTier > machineTier && CtnhBioLivingMachineCompat.shouldApplyOvervoltageDamage(machine)) {
            tcpatch$applyOvervoltageDamageOncePerTick(machine, latestTimeStamp);
        }

        // Tier+2 or higher: fatal overvoltage, do not insert energy.
        if (incomingTier > machineTier + 1) {
            tcpatch$applyFatalOvervoltage(machine);
            cir.setReturnValue(Math.min(amperage, getInputAmperage() - amps));
            cir.cancel();
            return;
        }

        if (canAccept < voltage) {
            cir.setReturnValue(0L);
            return;
        }

        long amperesAccepted = Math.min(canAccept / voltage, Math.min(amperage, getInputAmperage() - amps));
        if (amperesAccepted <= 0) {
            cir.setReturnValue(0L);
            return;
        }

        setEnergyStored(getEnergyStored() + voltage * amperesAccepted);
        amps += amperesAccepted;

        LivingMachineVoltageTracker.onPacketAccepted(machine, voltage, latestTimeStamp);
        tcpatch$maybeUpdateOverclockTier(machine, incomingTier);
        cir.setReturnValue(amperesAccepted);
        cir.cancel();
    }

    @Unique
    private static boolean tcpatch$isCtnhBioLivingMachine(Object machine) {
        // Backward-compatible helper kept for readability inside this class.
        return CtnhBioLivingMachineCompat.isCtnhBioMachine(machine);
    }

    @Unique
    private static void tcpatch$maybeUpdateOverclockTier(Object machine, int incomingTier) {
        if (!(machine instanceof IOverclockMachine overclockMachine)) return;
        try {
            int current = overclockMachine.getOverclockTier();
            if (incomingTier > current) {
                overclockMachine.setOverclockTier(incomingTier);
            }
        } catch (Throwable ignored) {
        }
    }

    @Unique
    private static void tcpatch$applyOvervoltageDamageOncePerTick(Object machine, long timeStamp) {
        if (!LivingMachineVoltageTracker.shouldHurtThisTick(machine, timeStamp)) return;

        Level level = CtnhBioLivingMachineCompat.getLevel(machine);
        if (level == null || level.isClientSide) return;

        LivingEntity entity = CtnhBioLivingMachineCompat.getMachineEntity(machine);
        if (entity == null || !entity.isAlive()) return;

        int tier = (machine instanceof ITieredMachine tm) ? tm.getTier() : 1;
        entity.hurt(GTDamageTypes.ELECTRIC.source(level), tier);
    }

    @Unique
    private static void tcpatch$applyFatalOvervoltage(Object machine) {
        Level level = CtnhBioLivingMachineCompat.getLevel(machine);
        if (level == null || level.isClientSide) return;

        LivingEntity entity = CtnhBioLivingMachineCompat.getMachineEntity(machine);
        if (entity == null || !entity.isAlive()) return;

        entity.hurt(GTDamageTypes.ELECTRIC.source(level), entity.getMaxHealth());
    }

    // NOTE: Level/entity reflection is centralized in CtnhBioLivingMachineCompat

    @Unique
    private Object tcpatch$getMachineReflect() {
        try {
            var method = ((Object) this).getClass().getMethod("getMachine");
            return method.invoke(this);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
