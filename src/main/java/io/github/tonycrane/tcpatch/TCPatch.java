package io.github.tonycrane.tcpatch;

import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;

@Mod(TCPatch.MOD_ID)
public class TCPatch {
    public static final String MOD_ID = "tcpatch";

    public TCPatch() {
        // Allow clients without this mod to join (server-side patch mod).
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY,
                        (remoteVersionString, isServer) -> true));
    }
}
