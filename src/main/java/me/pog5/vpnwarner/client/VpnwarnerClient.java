package me.pog5.vpnwarner.client;

import net.fabricmc.api.ClientModInitializer;

public class VpnwarnerClient implements ClientModInitializer {
    public static boolean userDismissedWarning = false;
    public static String detectedVpn = "";

    @Override
    public void onInitializeClient() {
        // everything is handled by mixins
    }
}
