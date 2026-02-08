package llc.redstone.systemsapi.mixins;

import llc.redstone.systemsapi.util.ChatUtils;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(
            method = "onGameMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        ChatUtils.dispatchIncomingChat(packet.content(), ci);
    }

}
