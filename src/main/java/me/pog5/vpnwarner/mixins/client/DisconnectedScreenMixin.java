package me.pog5.vpnwarner.mixins.client;

import me.pog5.vpnwarner.client.VpnwarnerClient;
import me.pog5.vpnwarner.client.screens.VpnWarningScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = DisconnectedScreen.class)
public class DisconnectedScreenMixin {
    @Shadow @Final public Screen parent;

    @ModifyArg(method = "method_19814", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"), index = 0)
    private Screen vpnWarner$changeDisconnectedBackToServerListButton(Screen screen) {
        if (screen instanceof VpnWarningScreen vpnWarningScreen) {
            VpnwarnerClient.userDismissedWarning = false;
            return vpnWarningScreen.parent;
        }
        return parent;
    }
}