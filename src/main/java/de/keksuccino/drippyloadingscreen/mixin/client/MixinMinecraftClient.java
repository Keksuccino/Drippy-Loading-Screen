package de.keksuccino.drippyloadingscreen.mixin.client;

import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.CustomizableLoadingOverlay;
import de.keksuccino.drippyloadingscreen.events.OverlayOpenEvent;
import de.keksuccino.konkrete.Konkrete;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.SplashScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Shadow private Overlay overlay;

    @Inject(at = @At("HEAD"), method = "setOverlay", cancellable = true)
    private void onSetOverlay(Overlay overlay, CallbackInfo info) {

        if (overlay != null) {
            if (DrippyLoadingScreen.isOptifineCompatibilityMode() && (overlay instanceof SplashScreen)) {
                info.cancel();
                this.overlay = new CustomizableLoadingOverlay((SplashScreen) overlay);
                Konkrete.getEventHandler().callEventsFor(new OverlayOpenEvent(this.overlay));
            } else {
                Konkrete.getEventHandler().callEventsFor(new OverlayOpenEvent(overlay));
            }
        }

    }

}
