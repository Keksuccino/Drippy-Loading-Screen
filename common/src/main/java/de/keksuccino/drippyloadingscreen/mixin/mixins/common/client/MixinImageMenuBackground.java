package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.DrippyUtils;
import de.keksuccino.fancymenu.customization.background.backgrounds.image.ImageMenuBackground;
import de.keksuccino.fancymenu.util.rendering.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ImageMenuBackground.class)
public class MixinImageMenuBackground {

    /**
     * @reason This tries to prevent the texture from flickering after reloading the texture manager in the {@link LoadingOverlay}.
     */
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lde/keksuccino/fancymenu/util/resource/ResourceSupplier;get()Lde/keksuccino/fancymenu/util/resource/Resource;", shift = At.Shift.AFTER, remap = false))
    private void afterGetResourceDrippy(GuiGraphics graphics, int mouseX, int mouseY, float partial, CallbackInfo ci) {

        ImageMenuBackground e = (ImageMenuBackground) ((Object)this);
        DrippyUtils.waitForTexture(e.textureSupplier.get());

    }

}
