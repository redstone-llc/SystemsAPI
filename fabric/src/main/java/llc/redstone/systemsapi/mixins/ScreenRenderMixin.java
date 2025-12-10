package llc.redstone.systemsapi.mixins;

import llc.redstone.systemsapi.util.MenuUtils;
import llc.redstone.systemsapi.util.TextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class ScreenRenderMixin {
    @Inject(method = "render", at=@At("HEAD"))
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        int y = 80;
        if (MenuUtils.INSTANCE.getWaitingOn() != null) {
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.of("--- Debug ---"), 50, y += 20, 0xFFFFFFFF, false);
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.of("Waiting On: " + MenuUtils.INSTANCE.getWaitingOn()), 50, y += 20, 0xFFFFFFFF, false);
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.of("Current Title: " + MenuUtils.INSTANCE.getCurrentScreen()), 50, y += 20, 0xFFFFFFFF, false);
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.of("Attempts: " + MenuUtils.INSTANCE.getAttempts()), 50, y += 20, 0xFFFFFFFF, false);
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.of("Last Waiting On: " + MenuUtils.INSTANCE.getLastWaitingOn()), 50, y += 20, 0xFFFFFFFF, false);
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.of("Last Successful: " + MenuUtils.INSTANCE.getLastSuccessful()), 50, y += 20, 0xFFFFFFFF, false);

            context.drawText(MinecraftClient.getInstance().textRenderer, Text.of("--- Error Correction ---"), 50, y += 40, 0xFFFFFFFF, false);
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.of("Last Click: " + MenuUtils.INSTANCE.getLastClick()), 50, y += 20, 0xFFFFFFFF, false);

        }
    }
}
