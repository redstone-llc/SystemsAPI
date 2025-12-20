package llc.redstone.systemsapi.mixins;

import llc.redstone.systemsapi.importer.GameruleImporter;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ReceiveGameruleValue {

    @Inject(
            method = "onGameMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        if (GameruleImporter.INSTANCE.getPendingChat$systemsapi() == null) return;

        String content = packet.content().getSiblings().get(1).getString();
        Boolean value = switch (content) {
            case "enabled" -> true;
            case "disabled" -> false;
            default -> null;
        };
        if (value == null) return;

        ci.cancel();
        GameruleImporter.INSTANCE.receiveChat$systemsapi(value);
    }

}
