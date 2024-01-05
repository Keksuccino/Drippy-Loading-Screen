package de.keksuccino.drippyloadingscreen.mixin.mixins.client;

import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.drippyloadingscreen.CustomizableLoadingOverlay;
import net.minecraft.client.gui.GuiGraphics;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Consumer;

@Mixin(LoadingOverlay.class)
public class MixinLoadingOverlay implements CustomizableLoadingOverlay {

    @Shadow private float currentProgress;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(Minecraft mc, ReloadInstance reload, Consumer consumer, boolean b, CallbackInfo info) {
        this.onConstruct();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderPre(GuiGraphics graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {
        this.onRenderPre(graphics, mouseX, mouseY, partial, currentProgress);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderPost(GuiGraphics graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {
        this.onRenderPost(graphics, mouseX, mouseY, partial, currentProgress);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableDepthTest()V"))
    private void onBackgroundRendered(GuiGraphics graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {
        this.onBackgroundRendered(graphics, mouseX, mouseY, partial);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"))
    private void onClose(GuiGraphics graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {
        this.onCloseOverlay();
        this.handleSetForgeEarlyLoadingConfigOption();
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_clearColor(FFFF)V"), index = 0)
    private float overrideBackgroundColorInClearColor0(float f) {
        int i2 = SharedLoadingOverlayData.getBackgroundColorInt(null);
        return (float)(i2 >> 16 & 255) / 255.0F;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_clearColor(FFFF)V"), index = 1)
    private float overrideBackgroundColorInClearColor1(float f) {
        int i2 = SharedLoadingOverlayData.getBackgroundColorInt(null);
        return (float)(i2 >> 8 & 255) / 255.0F;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_clearColor(FFFF)V"), index = 2)
    private float overrideBackgroundColorInClearColor2(float f) {
        int i2 = SharedLoadingOverlayData.getBackgroundColorInt(null);
        return (float)(i2 & 255) / 255.0F;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;replaceAlpha(II)I"), index = 0)
    private int overrideBackgroundColorInReplaceAlpha(int originalColor) {
        return SharedLoadingOverlayData.getBackgroundColorInt(null);
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;replaceAlpha(II)I"), index = 1)
    private int setCustomBackgroundOpacityInReplaceAlpha(int alpha) {
        float opacity = Math.max(0.0F, Math.min(1.0F, (float)alpha / 255.0F));
        this.setCustomBackgroundOpacity(opacity);
        if (!DrippyLoadingScreen.config.getOrDefault("early_fade_out_elements", false)) {
            this.setOverlayOpacity(opacity);
        }
        return alpha;
    }

    @Inject(method = "drawProgressBar", at = @At("HEAD"), cancellable = true)
    private void replaceOriginalProgressBar(GuiGraphics graphics, int p_96184_, int p_96185_, int p_96186_, int p_96187_, float opacity, CallbackInfo info) {
        info.cancel();
        this.renderCustomizableInstanceOfProgressBar(graphics, opacity);
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V"), index = 1)
    private int renderOriginalLogoOffscreenSetXMin(int xMinOriginal) {
        return -1000000;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V"), index = 2)
    private int renderOriginalLogoOffscreenSetYMin(int yMinOriginal) {
        return -1000000;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V"), index = 3)
    private int renderOriginalLogoOffscreenSetXMax(int xMaxOriginal) {
        return -1000000;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V"), index = 4)
    private int renderOriginalLogoOffscreenSetYMax(int yMaxOriginal) {
        return -1000000;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(Lnet/minecraft/client/renderer/RenderType;IIIII)V"))
    private void clearColorBeforeFillDrippy(GuiGraphics graphics, int p_282704_, int p_283650_, float p_283394_, CallbackInfo info) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_clear(IZ)V", shift = At.Shift.AFTER))
    private void clearColorAfterBackgroundRenderingDrippy(GuiGraphics graphics, int p_282704_, int p_283650_, float p_283394_, CallbackInfo info) {
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V", shift = At.Shift.AFTER))
    private void renderCustomizableInstanceOfLogo(GuiGraphics graphics, int p_96179_, int p_96180_, float p_96181_, CallbackInfo info) {
        this.renderCustomizableInstanceOfLogo(graphics);
    }

}
