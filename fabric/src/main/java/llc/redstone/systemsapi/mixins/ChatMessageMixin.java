package llc.redstone.systemsapi.mixins;

import llc.redstone.systemsapi.util.CommandUtils;
import llc.redstone.systemsapi.util.MenuUtils;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ChatMessageMixin {

    @Inject(
            method = "onGameMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        if (MenuUtils.INSTANCE.getPendingString() == null) return;

        Text message = packet.content();
        if (!message.getSiblings().get(0).equals(Text.literal("Please use the chat to provide the value you wish to set."))) return;

        Text previousComponent = message.getSiblings().get(1);
        Style previousStyle = previousComponent.getStyle();
        ClickEvent clickEvent = previousStyle.getClickEvent();
        if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
            MenuUtils.INSTANCE.onPreviousInputReceived(((ClickEvent.SuggestCommand) clickEvent).command());
            CommandUtils.INSTANCE.runCommand("chatinput cancel", 0L);
            ci.cancel();
        }
    }
}
