package de.keksuccino.drippyloadingscreen.mixin.client;

import de.keksuccino.drippyloadingscreen.events.OverlayOpenEvent;
import de.keksuccino.konkrete.Konkrete;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(at = @At("HEAD"), method = "setOverlay")
    private void onSetOverlay(Overlay overlay, CallbackInfo info) {

        Konkrete.getEventHandler().callEventsFor(new OverlayOpenEvent(overlay));

    }

}
