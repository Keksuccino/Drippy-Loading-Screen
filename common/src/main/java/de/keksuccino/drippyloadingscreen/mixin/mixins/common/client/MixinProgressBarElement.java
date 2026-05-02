package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.DrippyUtils;
import de.keksuccino.fancymenu.customization.element.elements.progressbar.ProgressBarElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProgressBarElement.class)
public class MixinProgressBarElement {

    /**
     * @reason This tries to prevent the texture from flickering after reloading the texture manager in the {@link LoadingOverlay}.
     */
    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lde/keksuccino/fancymenu/customization/element/elements/progressbar/ProgressBarElement;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
    private void beforeRenderBackgroundDrippy(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial, CallbackInfo ci) {

        ProgressBarElement e = (ProgressBarElement) ((Object)this);

        if (e.barTextureSupplier != null) DrippyUtils.waitForTexture(e.barTextureSupplier.get());
        if (e.backgroundTextureSupplier != null) DrippyUtils.waitForTexture(e.backgroundTextureSupplier.get());

    }

}
