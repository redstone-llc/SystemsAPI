package llc.redstone.systemsapi.mixins;

import llc.redstone.systemsapi.importer.HouseImporter;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class CancelPauseScreenMixin {
    @Inject(method = "openGameMenu", at = @At("HEAD"), cancellable = true)
    private void openGameMenu(boolean pauseOnly, CallbackInfo ci) {
        if (HouseImporter.INSTANCE.isImporting()) {
            ci.cancel();
        }
    }
}
