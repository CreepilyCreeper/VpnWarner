package me.pog5.vpnwarner.mixins.client;

import me.pog5.vpnwarner.client.VpnwarnerClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "method_18096", at = @At("HEAD"))
    private void vpnWarner$handleDisconnect(CallbackInfo ci) {
        VpnwarnerClient.userDismissedWarning = false;
    }
}
