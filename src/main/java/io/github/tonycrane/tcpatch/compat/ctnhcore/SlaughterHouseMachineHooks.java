package io.github.tonycrane.tcpatch.compat.ctnhcore;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface SlaughterHouseMachineHooks {

    void tcpatch$ensureLootCacheUpToDate(ServerLevel level);

    List<ItemStack> tcpatch$getLootCacheStacks();

    int tcpatch$getLootCacheTotalExperience();

    int tcpatch$getLootCacheDuration();

    int tcpatch$getLootCacheRepeatTimes();

    List<String> tcpatch$getMobList();

    void tcpatch$onStructureInvalidOrUnload();
}
