package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.fancymenu.customization.overlay.CustomizationOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CustomizationOverlay.class)
public class MixinCustomizationOverlay {

    /**
     * @reason Don't render FancyMenu overlays when DrippyOverlayScreen gets rendered in LoadingOverlay.
     */
    @Inject(method = "isOverlayVisible", at = @At("HEAD"), cancellable = true, remap = false)
    private static void on_isOverlayVisible_Drippy(Screen currentScreen, CallbackInfoReturnable<Boolean> info) {
        if (Minecraft.getInstance().getOverlay() instanceof LoadingOverlay) {
            info.setReturnValue(false);
        }
    }

}
