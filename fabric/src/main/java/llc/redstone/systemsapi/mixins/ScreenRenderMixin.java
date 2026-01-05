package llc.redstone.systemsapi.mixins;

import llc.redstone.systemsapi.util.MenuUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class ScreenRenderMixin {
    @Inject(method = "render", at=@At("HEAD"))
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        MenuUtils.INSTANCE.render$systemsapi();
    }

    @Inject(method="drawSlot", at=@At("RETURN"))
    public void drawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        MenuUtils.INSTANCE.renderStack$systemsapi(slot.getStack());
    }
}
