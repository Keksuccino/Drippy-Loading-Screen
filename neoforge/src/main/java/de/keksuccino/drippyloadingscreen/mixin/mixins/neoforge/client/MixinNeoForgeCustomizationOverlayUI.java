package de.keksuccino.drippyloadingscreen.mixin.mixins.neoforge.client;

import de.keksuccino.drippyloadingscreen.neoforge.EarlyLoadingEditorScreen;
import de.keksuccino.fancymenu.customization.overlay.CustomizationOverlayMenuBar;
import de.keksuccino.fancymenu.customization.overlay.CustomizationOverlayUI;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CustomizationOverlayUI.class)
public class MixinNeoForgeCustomizationOverlayUI {

    @Inject(method = "buildMenuBar", at = @At("RETURN"))
    private static void after_buildMenuBar_NeoForge_Drippy(boolean expanded, CallbackInfoReturnable<CustomizationOverlayMenuBar> info) {

        CustomizationOverlayMenuBar bar = info.getReturnValue();
        boolean isEarlyLoadingEditor = (Minecraft.getInstance().screen instanceof EarlyLoadingEditorScreen);
        if ((bar != null) && isEarlyLoadingEditor) {
            bar.removeEntry("screen");
            bar.removeEntry("tools");
        }

    }

}
