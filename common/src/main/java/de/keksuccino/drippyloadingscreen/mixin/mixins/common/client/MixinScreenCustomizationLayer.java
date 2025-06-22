package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.DrippyUtils;
import de.keksuccino.fancymenu.customization.layer.ScreenCustomizationLayer;
import de.keksuccino.fancymenu.customization.layout.Layout;
import de.keksuccino.fancymenu.customization.layout.LayoutBase;
import de.keksuccino.fancymenu.customization.layout.LayoutHandler;
import de.keksuccino.fancymenu.events.screen.InitOrResizeScreenEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(ScreenCustomizationLayer.class)
public abstract class MixinScreenCustomizationLayer {

    @Shadow public abstract @NotNull String getScreenIdentifier();

    @Shadow public LayoutBase layoutBase;

    /**
     * @reason Custom GUI scaling is not supported in the loading overlay.
     */
    @Inject(method = "onInitOrResizeScreenPre", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", shift = At.Shift.AFTER))
    private void before_scaling_in_onInitOrResizeScreenPre_Drippy(InitOrResizeScreenEvent.Pre e, CallbackInfo info) {
        if (DrippyUtils.isDrippyIdentifier(this.getScreenIdentifier())) {
            this.layoutBase.forcedScale = 0.0F;
            this.layoutBase.autoScalingWidth = 0;
            this.layoutBase.autoScalingHeight = 0;
        }
    }

    /**
     * @reason Add handling for allowUniversalLayouts config option if identifier is Drippy identifier.
     */
    @Redirect(method = "onInitOrResizeScreenPre", at = @At(value = "INVOKE", target = "Lde/keksuccino/fancymenu/customization/layout/LayoutHandler;getEnabledLayoutsForScreenIdentifier(Ljava/lang/String;Z)Ljava/util/List;"), remap = false)
    private List<Layout> onGetEnabledLayoutsForScreenIdentifierDrippy(@NotNull String identifier, boolean includeUniversalLayouts) {
        if (DrippyUtils.isDrippyIdentifier(identifier)) {
            includeUniversalLayouts = DrippyLoadingScreen.getOptions().allowUniversalLayouts.getValue();
        }
        return LayoutHandler.getEnabledLayoutsForScreenIdentifier(identifier, includeUniversalLayouts);
    }

}
