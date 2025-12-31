package llc.redstone.systemsapi.mixins;

import llc.redstone.systemsapi.util.MenuUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class OpenCloseScreenMixin {
    @Inject(method = "onOpenScreen", at = @At(value = "RETURN"))
    private void onScreenOpen(OpenScreenS2CPacket packet, CallbackInfo ci) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen == null) return;
        MenuUtils.INSTANCE.completeOnOpenScreen(screen);
    }

    @Inject(method = "onCloseScreen", at = @At(value = "RETURN"))
    private void onScreenClose(CloseScreenS2CPacket packet, CallbackInfo ci) {
        MenuUtils.INSTANCE.completeOnClose();
    }
}
