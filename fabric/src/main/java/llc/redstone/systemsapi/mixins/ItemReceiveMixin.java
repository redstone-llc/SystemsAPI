package llc.redstone.systemsapi.mixins;

import llc.redstone.systemsapi.util.MenuUtils;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ItemReceiveMixin {
    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("HEAD"))
    private void onItemReceived(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        ItemStack stack = packet.getStack();

        if (stack.isEmpty()) return;
        MenuUtils.INSTANCE.onItemReceived(stack, packet.getSlot());
    }
}
