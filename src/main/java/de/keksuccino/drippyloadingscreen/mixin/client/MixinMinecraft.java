package de.keksuccino.drippyloadingscreen.mixin.client;

import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.CustomizableLoadingOverlay;
import de.keksuccino.drippyloadingscreen.events.OverlayOpenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.LoadingGui;
import net.minecraft.client.gui.ResourceLoadProgressGui;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow private LoadingGui loadingGui;

    @Inject(at = @At("HEAD"), method = "setLoadingGui", cancellable = true)
    private void onSetOverlay(LoadingGui overlay, CallbackInfo info) {

        if (overlay != null) {
            if (DrippyLoadingScreen.isOptifineCompatibilityMode() && (overlay instanceof ResourceLoadProgressGui)) {
                info.cancel();
                this.loadingGui = new CustomizableLoadingOverlay((ResourceLoadProgressGui) overlay);
                MinecraftForge.EVENT_BUS.post(new OverlayOpenEvent(this.loadingGui));
            } else {
                MinecraftForge.EVENT_BUS.post(new OverlayOpenEvent(overlay));
            }
        }

    }

}