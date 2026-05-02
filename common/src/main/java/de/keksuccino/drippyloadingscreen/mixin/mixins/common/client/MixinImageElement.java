package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.shaders.ShaderType;
import de.keksuccino.drippyloadingscreen.DrippyUtils;
import de.keksuccino.fancymenu.customization.element.elements.image.ImageElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ImageElement.class)
public class MixinImageElement {

    @Unique
    private static final Identifier FANCYMENU_SMOOTH_IMAGE_RECT_SHADER_DRIPPY = Identifier.withDefaultNamespace("core/fancymenu_gui_smooth_image_rect");

    /**
     * @reason This tries to prevent the texture from flickering after reloading the texture manager in the {@link LoadingOverlay}.
     */
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lde/keksuccino/fancymenu/customization/element/elements/image/ImageElement;getTextureResource()Lde/keksuccino/fancymenu/util/resource/resources/texture/ITexture;", shift = At.Shift.AFTER, remap = false))
    private void afterGetResourceDrippy(GuiGraphics graphics, int mouseX, int mouseY, float partial, CallbackInfo ci) {

        ImageElement e = (ImageElement) ((Object)this);
        DrippyUtils.waitForTexture(e.getTextureResource());

    }

    /**
     * @reason The NeoForge loading overlay can render before FancyMenu's shader sources are available.
     */
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lde/keksuccino/fancymenu/util/rendering/SmoothImageRectangleRenderer;renderSmoothImageRectRoundAllCornersScaled(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/Identifier;FFFFFFFFIF)V"), remap = false)
    private void wrap_renderSmoothImageRectRoundAllCornersScaled_Drippy(GuiGraphics graphics, Identifier texture, float x, float y, float width, float height, float topLeftRadius, float topRightRadius, float bottomRightRadius, float bottomLeftRadius, int color, float partial, Operation<Void> original) {

        if ((Minecraft.getInstance().getOverlay() instanceof LoadingOverlay) && !areSmoothImageShadersAvailableDrippy()) {
            int roundedWidth = Math.max(1, Math.round(width));
            int roundedHeight = Math.max(1, Math.round(height));
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, Math.round(x), Math.round(y), 0.0F, 0.0F, roundedWidth, roundedHeight, roundedWidth, roundedHeight);
            return;
        }

        original.call(graphics, texture, x, y, width, height, topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius, color, partial);

    }

    @Unique
    private static boolean areSmoothImageShadersAvailableDrippy() {
        return (Minecraft.getInstance().getShaderManager().getShader(FANCYMENU_SMOOTH_IMAGE_RECT_SHADER_DRIPPY, ShaderType.VERTEX) != null)
                && (Minecraft.getInstance().getShaderManager().getShader(FANCYMENU_SMOOTH_IMAGE_RECT_SHADER_DRIPPY, ShaderType.FRAGMENT) != null);
    }

}
