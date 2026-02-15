package io.github.tonycrane.tcpatch;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkConstants;
import org.slf4j.Logger;

import io.github.tonycrane.tcpatch.compat.jade.TCPatchJadeCompat;

@Mod(TCPatch.MOD_ID)
public class TCPatch {
    public static final String MOD_ID = "tcpatch";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TCPatch() {
        // Allow clients without this mod to join (server-side patch mod).
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY,
                        (remoteVersionString, isServer) -> true));

        if (ModList.get().isLoaded("jade") && ModList.get().isLoaded("ctnhlib") && ModList.get().isLoaded("ctnhcore") &&
                ModList.get().isLoaded("gtceu")) {
            try {
                TCPatchJadeCompat.init();
            } catch (Throwable t) {
                LOGGER.warn("Failed to init Jade compat; skipping multithread recipe output provider.", t);
            }
        }
    }
}
