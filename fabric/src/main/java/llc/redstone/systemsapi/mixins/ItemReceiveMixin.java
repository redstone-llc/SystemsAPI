package llc.redstone.systemsapi.mixins;

import llc.redstone.systemsapi.util.InputUtils;
import net.minecraft.client.MinecraftClient;
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
        if (InputUtils.INSTANCE.getPendingStack$systemsapi() == null) return;
        ItemStack oldStack = MinecraftClient.getInstance().player.currentScreenHandler.getSlot(packet.getSlot()).getStack();
        ItemStack newStack = packet.getStack();
        if (oldStack.isEmpty()) {
            if (newStack.isEmpty()) return;
            InputUtils.INSTANCE.onItemReceived$systemsapi(newStack);
        } else {
            ItemStack stack = packet.getStack().copyWithCount(newStack.getCount() - oldStack.getCount());
            if (stack.isEmpty()) return;
            InputUtils.INSTANCE.onItemReceived$systemsapi(stack);
        }
    }
}
