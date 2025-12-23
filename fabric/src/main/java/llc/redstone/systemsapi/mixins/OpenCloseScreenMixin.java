package llc.redstone.systemsapi.mixins;

import llc.redstone.systemsapi.util.MenuUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class OpenCloseScreenMixin {
    @Inject(method = "setScreen", at = @At(value = "RETURN"))
    private void onScreenSet(Screen screen, CallbackInfo ci) {
        if (screen == null) {
            // Screen closed
            MenuUtils.INSTANCE.completeOnClose();
        } else {
            // Screen opened
            MenuUtils.INSTANCE.completeOnOpenScreen(screen);
        }
    }
}
