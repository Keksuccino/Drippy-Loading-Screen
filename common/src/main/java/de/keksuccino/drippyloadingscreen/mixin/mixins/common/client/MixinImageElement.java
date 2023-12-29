package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.LoadingScreenRenderingUtils;
import de.keksuccino.fancymenu.customization.element.elements.image.ImageElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ImageElement.class)
public class MixinImageElement {

    /**
     * @reason This tries to prevent the texture from flickering after reloading the texture manager in the {@link LoadingOverlay}.
     */
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lde/keksuccino/fancymenu/customization/element/elements/image/ImageElement;getTextureResource()Lde/keksuccino/fancymenu/util/resource/resources/texture/ITexture;", shift = At.Shift.AFTER))
    private void afterGetResourceDrippy(GuiGraphics graphics, int mouseX, int mouseY, float partial, CallbackInfo ci) {

        ImageElement e = (ImageElement) ((Object)this);
        LoadingScreenRenderingUtils.waitForTexture(e.getTextureResource());

    }

}
