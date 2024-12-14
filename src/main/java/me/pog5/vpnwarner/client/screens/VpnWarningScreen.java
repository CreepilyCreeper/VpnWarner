package me.pog5.vpnwarner.client.screens;

import me.pog5.vpnwarner.client.VpnwarnerClient;
import me.pog5.vpnwarner.client.utils.VpnDetection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WarningScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Date;

public class VpnWarningScreen extends WarningScreen {
    private static final Text HEADER = net.minecraft.text.Text.literal("CAUTION: You have a VPN enabled!").formatted(Formatting.BOLD, Formatting.RED);
    private static final Text MESSAGE = net.minecraft.text.Text.literal("")
            .append("Some servers (such as Loka) will ")
            .append(Text.literal("ban your account").formatted(Formatting.RED))
            .append(" for using a VPN, and subsequently your main IP address as well.")
            .append("\n")
            .append(Text.literal("Detected VPNs: ").formatted(Formatting.BOLD))

            .append(Text.literal( // trim the last comma
                    VpnwarnerClient.detectedVpn.substring(Math.min(VpnwarnerClient.detectedVpn.length() - 2, 0), VpnwarnerClient.detectedVpn.length() - 2
                    )).formatted(Formatting.RED, Formatting.BOLD));
    private static final Text NARRATED_TEXT = HEADER.copy().append("\n").append(MESSAGE);
    public final Screen parent;
    private final ServerInfo entry;

    public VpnWarningScreen(Screen parent, ServerInfo entry) {
        super(HEADER, MESSAGE, null, NARRATED_TEXT);
        this.parent = parent;
        this.entry = entry;
        if (!VpnDetection.isVpnEnabled()) close();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    protected LayoutWidget getLayout() {
        DirectionalLayoutWidget directionalLayoutWidget = DirectionalLayoutWidget.horizontal().spacing(8);

        directionalLayoutWidget.add(ButtonWidget.builder(Text.literal("Wait... "), button -> {
            VpnwarnerClient.userDismissedWarning = true;
            VpnwarnerClient.detectedVpn = "";
            close();
            assert this.client != null;
            ConnectScreen.connect(this, this.client, ServerAddress.parse(entry.address), entry, false, null);
        }).dimensions(this.width / 2 - 155, 100, 150, 20).build());
        directionalLayoutWidget.add(ButtonWidget.builder(ScreenTexts.BACK, button -> {
            close();
            VpnwarnerClient.detectedVpn = "";
        }).dimensions(this.width / 2 - 155 + 160, 100, 150, 20).build());
        directionalLayoutWidget.forEachChild(child -> {
            if (child instanceof ButtonWidget button && button.getMessage().getString().contains("Wait")) {
                button.active = false;
                var thread = new Thread(() -> {
                    Date start = new Date();
                    while (new Date().getTime() - start.getTime() < 5000) {
                        button.setMessage(Text.literal("Wait... (" + (5 - (new Date().getTime() - start.getTime()) / 1000) + ")"));
                    }
                    if (this.client.currentScreen == this) {
                        button.active = true;
                        button.setMessage(ScreenTexts.ACKNOWLEDGE);
                    }
                });
                thread.start();
            }
        });

        return directionalLayoutWidget;
    }
}
