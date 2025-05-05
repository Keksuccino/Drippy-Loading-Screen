package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.DrippyUtils;
import de.keksuccino.fancymenu.customization.element.elements.playerentity.v2.PlayerEntityElement;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityElement.class)
public class MixinPlayerEntityElement {

    /**
     * @reason Cancel rendering if in Drippy layout (to not spam errors to the log)
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void headRenderDrippy(GuiGraphics graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {
        if (DrippyUtils.isDrippyRendering()) info.cancel();
    }

}
