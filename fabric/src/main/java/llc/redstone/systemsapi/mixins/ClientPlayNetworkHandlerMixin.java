package llc.redstone.systemsapi.mixins;

import com.mojang.brigadier.suggestion.Suggestions;
import llc.redstone.systemsapi.util.CommandUtils;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onCommandSuggestions", at = @At("HEAD"))
    private void onCommandSuggestions(CommandSuggestionsS2CPacket packet, CallbackInfo ci) {
        Suggestions suggestions = packet.getSuggestions();
        CommandUtils.INSTANCE.handleSuggestions(suggestions);
    }

}
