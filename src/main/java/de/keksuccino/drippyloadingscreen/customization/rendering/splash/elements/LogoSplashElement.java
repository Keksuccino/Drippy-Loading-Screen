package de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class LogoSplashElement extends SplashElementBase {

    private static final ResourceLocation MOJANG_LOGO_TEXTURE = new ResourceLocation("textures/gui/title/mojangstudios.png");

    public LogoSplashElement(SplashCustomizationLayer handler) {
        super(handler);

        if ((Minecraft.getInstance() != null) && (Minecraft.getInstance().getWindow() != null)) {
            double d0 = Math.min((double) this.mc.getWindow().getGuiScaledWidth() * 0.75D, this.mc.getWindow().getGuiScaledHeight()) * 0.25D;
            double d1 = d0 * 4.0D;
            int k1 = (int) (d1 * 0.5D);
            this.width = (int) (k1 * 2.0D);
            this.height = (int) d0;
        }
    }

    @Override
    public void render(PoseStack matrix, int scaledWidth, int scaledHeight, float partialTicks) {

        if (this.visible) {
            this.renderLogo(matrix);
        }

    }

    protected void renderLogo(PoseStack matrix) {

        double d0 = Math.min((double)this.mc.getWindow().getGuiScaledWidth() * 0.75D, this.mc.getWindow().getGuiScaledHeight()) * 0.25D;
        double d1 = d0 * 4.0D;
        int k1 = (int)(d1 * 0.5D);
        long time = System.currentTimeMillis();
        float f = this.handler.reloadCompleteTime > -1L ? (float)(time - this.handler.reloadCompleteTime) / 1000.0F : -1.0F;
        float f1 = this.handler.reloadStartTime > -1L ? (float)(time - this.handler.reloadStartTime) / 500.0F : -1.0F;
        float f2;
        if (f >= 1.0F) {
            f2 = 1.0F - Mth.clamp(f - 1.0F, 0.0F, 1.0F);
        } else if (handler.reloading) {
            f2 = Mth.clamp(f1, 0.0F, 1.0F);
        } else {
            f2 = 1.0F;
        }
        if (this.handler.isEditor || SplashCustomizationLayer.isCustomizationHelperScreen() || DrippyLoadingScreen.isFancyMenuLoaded()) {
            f2 = 1.0F;
        }

        //Update width and height
        this.width = (int) (k1 * 2.0D);
        this.height = (int) d0;

        RenderSystem.setShaderTexture(0, MOJANG_LOGO_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendEquation(32774);
        RenderSystem.blendFunc(770, 1);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f2);

        //   matrix  X       Y       W   H        uOff      vOff  uW   uH  texW texH
        blit(matrix, this.x, this.y, k1, (int)d0, -0.0625F, 0.0F, 120, 60, 120, 120);
        blit(matrix, this.x + k1, this.y, k1, (int)d0, 0.0625F, 60.0F, 120, 60, 120, 120);

        RenderSystem.defaultBlendFunc();

    }

}
