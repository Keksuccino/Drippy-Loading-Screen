package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.DrippyUtils;
import de.keksuccino.fancymenu.customization.element.elements.playerentity.PlayerEntityElement;
import de.keksuccino.fancymenu.customization.element.elements.playerentity.model.PlayerEntityElementRenderer;
import de.keksuccino.fancymenu.customization.element.elements.playerentity.model.PlayerEntityProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityElement.class)
public class MixinPlayerEntityElement {

    /**
     * @reason Cancel rendering if in Drippy layout (to not spam errors to the log)
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void headRenderDrippy(PoseStack graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {
        if (DrippyUtils.isDrippyRendering()) info.cancel();
    }

    /**
     * @reason Return a dummy properties instance if in Drippy layout (to not spam errors to the log)
     */
    @Inject(method = "getActiveEntityProperties", at = @At("HEAD"), cancellable = true, remap = false)
    private void headGetActiveEntityPropertiesDrippy(CallbackInfoReturnable<PlayerEntityProperties> info) {
        if (DrippyUtils.isDrippyRendering()) info.setReturnValue(new PlayerEntityProperties(false));
    }

    /**
     * @reason Return NULL if in Drippy layout (to not spam errors to the log)
     */
    @Inject(method = "buildEntityRenderer", at = @At("HEAD"), cancellable = true, remap = false)
    private static void headBuildEntityRendererDrippy(boolean slim, CallbackInfoReturnable<PlayerEntityElementRenderer> info) {
        if (DrippyUtils.isDrippyRendering()) info.setReturnValue(null);
    }

}
