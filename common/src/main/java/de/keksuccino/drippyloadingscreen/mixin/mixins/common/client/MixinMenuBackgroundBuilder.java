package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.fancymenu.customization.background.MenuBackgroundBuilder;
import de.keksuccino.fancymenu.customization.background.backgrounds.browser.BrowserMenuBackgroundBuilder;
import de.keksuccino.fancymenu.customization.background.backgrounds.video.nativevideo.NativeVideoMenuBackgroundBuilder;
import de.keksuccino.fancymenu.customization.background.backgrounds.video.rinku.RinkuVideoMenuBackgroundBuilder;
import de.keksuccino.fancymenu.customization.layout.editor.LayoutEditorScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MenuBackgroundBuilder.class)
public class MixinMenuBackgroundBuilder {

    @Inject(method = "shouldShowUpInEditorBackgroundMenu", at = @At("RETURN"), cancellable = true, remap = false)
    private void return_shouldShowUpInEditorBackgroundMenu_Drippy(LayoutEditorScreen editor, CallbackInfoReturnable<Boolean> info) {

        MenuBackgroundBuilder self = (MenuBackgroundBuilder)((Object)this);

        if (editor.layoutTargetScreen instanceof DrippyOverlayScreen) {

            // FancyMenu omits MCEF classes from Forge, so use its shared serialized identifier without linking the Fabric-only type.
            if (self.getIdentifier().equals("video_mcef")) info.setReturnValue(false);

            if (self instanceof RinkuVideoMenuBackgroundBuilder) info.setReturnValue(false);

            if (self instanceof NativeVideoMenuBackgroundBuilder) info.setReturnValue(false);

            if (self instanceof BrowserMenuBackgroundBuilder) info.setReturnValue(false);


        }

    }

}
