package de.keksuccino.drippyloadingscreen.mixin.mixins.client;

import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.fancymenu.menu.fancy.helper.CustomizationHelper;
import de.keksuccino.fancymenu.menu.fancy.helper.CustomizationHelperUI;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CustomizationHelper.class)
public class MixinCustomizationHelper {

    @Inject(at = @At("RETURN"), method = "onRenderPost", remap = false)
    private void onOnRenderPost(GuiScreenEvent.DrawScreenEvent.Post e, CallbackInfo info) {

        if (e.getGui() instanceof DrippyOverlayScreen) {
            CustomizationHelperUI.render(e.getMatrixStack(), e.getGui());
        }

    }

}
