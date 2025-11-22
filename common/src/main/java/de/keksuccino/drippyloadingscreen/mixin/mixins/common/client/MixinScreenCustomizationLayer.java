package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.DrippyUtils;
import de.keksuccino.fancymenu.customization.layer.ScreenCustomizationLayer;
import de.keksuccino.fancymenu.customization.layout.Layout;
import de.keksuccino.fancymenu.customization.layout.LayoutHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.List;

@Mixin(ScreenCustomizationLayer.class)
public class MixinScreenCustomizationLayer {

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
