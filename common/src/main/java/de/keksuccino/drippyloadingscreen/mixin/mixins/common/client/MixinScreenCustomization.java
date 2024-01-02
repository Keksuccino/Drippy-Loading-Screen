package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.fancymenu.customization.ScreenCustomization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenCustomization.class)
public class MixinScreenCustomization {

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lde/keksuccino/fancymenu/customization/layout/LayoutHandler;init()V"), remap = false)
    private static void beforeInitLayoutHandlerDrippy(CallbackInfo info) {

        DrippyLoadingScreen.registerAll();

    }

}
