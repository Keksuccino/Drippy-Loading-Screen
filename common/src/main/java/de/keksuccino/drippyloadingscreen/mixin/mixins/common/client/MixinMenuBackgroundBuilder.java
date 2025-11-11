package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.fancymenu.customization.background.MenuBackgroundBuilder;
import de.keksuccino.fancymenu.customization.background.backgrounds.video.mcef.MCEFVideoMenuBackgroundBuilder;
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

            if (self instanceof MCEFVideoMenuBackgroundBuilder) info.setReturnValue(false);

        }

    }

}
