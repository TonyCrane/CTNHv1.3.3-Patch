package io.github.tonycrane.tcpatch.compat.ctnhbio;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class LivingMachineVoltageTracker {

    private LivingMachineVoltageTracker() {}

    public static final class State {
        public long lastAcceptedNetworkVoltage;
        public long lastAcceptedNetworkVoltageTime = Long.MIN_VALUE;
        public long lastOvervoltageHurtTime = Long.MIN_VALUE;
    }

    private static final Map<Object, State> STATES = Collections.synchronizedMap(new WeakHashMap<>());

    public static State getState(Object machine) {
        if (machine == null) {
            return null;
        }
        synchronized (STATES) {
            return STATES.computeIfAbsent(machine, k -> new State());
        }
    }

    public static void onPacketSeen(Object machine, long voltage, long timeStamp) {
        State state = getState(machine);
        if (state == null) return;

        if (state.lastAcceptedNetworkVoltageTime == timeStamp) {
            state.lastAcceptedNetworkVoltage = Math.max(state.lastAcceptedNetworkVoltage, voltage);
        } else {
            state.lastAcceptedNetworkVoltage = voltage;
            state.lastAcceptedNetworkVoltageTime = timeStamp;
        }
    }

    public static void onPacketAccepted(Object machine, long voltage, long timeStamp) {
        State state = getState(machine);
        if (state == null) return;

        state.lastAcceptedNetworkVoltage = voltage;
        state.lastAcceptedNetworkVoltageTime = timeStamp;
    }

    public static long getRecentNetworkVoltage(Object machine, long now, boolean allowHistory) {
        State state = getState(machine);
        if (state == null) return 0L;

        long lastTime = state.lastAcceptedNetworkVoltageTime;
        if (lastTime == now || (allowHistory && lastTime >= now - 5)) {
            return state.lastAcceptedNetworkVoltage;
        }
        return 0L;
    }

    public static boolean shouldHurtThisTick(Object machine, long timeStamp) {
        State state = getState(machine);
        if (state == null) return false;

        if (state.lastOvervoltageHurtTime >= timeStamp) {
            return false;
        }
        state.lastOvervoltageHurtTime = timeStamp;
        return true;
    }
}
