package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.fancymenu.customization.element.ElementBuilder;
import de.keksuccino.fancymenu.customization.element.elements.audio.AudioElementBuilder;
import de.keksuccino.fancymenu.customization.element.elements.browser.BrowserElementBuilder;
import de.keksuccino.fancymenu.customization.element.elements.button.custombutton.ButtonElementBuilder;
import de.keksuccino.fancymenu.customization.element.elements.checkbox.CheckboxElementBuilder;
import de.keksuccino.fancymenu.customization.element.elements.cursor.CursorElementBuilder;
import de.keksuccino.fancymenu.customization.element.elements.dragger.DraggerElementBuilder;
import de.keksuccino.fancymenu.customization.element.elements.inputfield.InputFieldElementBuilder;
import de.keksuccino.fancymenu.customization.element.elements.item.ItemElementBuilder;
import de.keksuccino.fancymenu.customization.element.elements.musiccontroller.MusicControllerElementBuilder;
import de.keksuccino.fancymenu.customization.element.elements.slider.v2.SliderElementBuilder;
import de.keksuccino.fancymenu.customization.element.elements.tooltip.TooltipElementBuilder;
import de.keksuccino.fancymenu.customization.element.elements.video.mcef.MCEFVideoElementBuilder;
import de.keksuccino.fancymenu.customization.layout.editor.LayoutEditorScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ElementBuilder.class)
public class MixinElementBuilder {

    @Inject(method = "shouldShowUpInEditorElementMenu", at = @At("RETURN"), cancellable = true, remap = false)
    private void return_shouldShowUpInEditorElementMenu_Drippy(LayoutEditorScreen editor, CallbackInfoReturnable<Boolean> info) {

        ElementBuilder self = (ElementBuilder)((Object)this);

        if (editor.layoutTargetScreen instanceof DrippyOverlayScreen) {

            if (self instanceof MCEFVideoElementBuilder) info.setReturnValue(false);

            if (self instanceof CursorElementBuilder) info.setReturnValue(false);

            if (self instanceof AudioElementBuilder) info.setReturnValue(false);

            if (self instanceof MusicControllerElementBuilder) info.setReturnValue(false);

            if (self instanceof ItemElementBuilder) info.setReturnValue(false);

            if (self instanceof ButtonElementBuilder) info.setReturnValue(false);

            if (self instanceof SliderElementBuilder) info.setReturnValue(false);

            if (self instanceof CheckboxElementBuilder) info.setReturnValue(false);

            if (self instanceof InputFieldElementBuilder) info.setReturnValue(false);

            if (self instanceof TooltipElementBuilder) info.setReturnValue(false);

            if (self instanceof DraggerElementBuilder) info.setReturnValue(false);

            if (self instanceof BrowserElementBuilder) info.setReturnValue(false);

        }

    }

}
