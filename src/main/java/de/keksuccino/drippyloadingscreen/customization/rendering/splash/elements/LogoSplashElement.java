package de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;

import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class LogoSplashElement extends SplashElementBase {

    private static final Identifier MOJANG_LOGO_TEXTURE = new Identifier("textures/gui/title/mojangstudios.png");

    public LogoSplashElement(SplashCustomizationLayer handler) {
        super(handler);

        if ((MinecraftClient.getInstance() != null) && (MinecraftClient.getInstance().getWindow() != null)) {
            double d0 = Math.min((double) this.mc.getWindow().getScaledWidth() * 0.75D, this.mc.getWindow().getScaledHeight()) * 0.25D;
            double d1 = d0 * 4.0D;
            int k1 = (int) (d1 * 0.5D);
            this.width = (int) (k1 * 2.0D);
            this.height = (int) d0;
        }
    }

    @Override
    public void render(MatrixStack matrix, int scaledWidth, int scaledHeight, float partialTicks) {

        if (this.visible) {
            this.renderLogo(matrix);
        }

    }

    protected void renderLogo(MatrixStack matrix) {

        double d0 = Math.min((double)this.mc.getWindow().getScaledWidth() * 0.75D, this.mc.getWindow().getScaledHeight()) * 0.25D;
        double d1 = d0 * 4.0D;
        int k1 = (int)(d1 * 0.5D);
        long time = System.currentTimeMillis();
        float f = this.handler.reloadCompleteTime > -1L ? (float)(time - this.handler.reloadCompleteTime) / 1000.0F : -1.0F;
        float f1 = this.handler.reloadStartTime > -1L ? (float)(time - this.handler.reloadStartTime) / 500.0F : -1.0F;
        float f2;
        if (f >= 1.0F) {
            f2 = 1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F);
        } else if (handler.reloading) {
            f2 = MathHelper.clamp(f1, 0.0F, 1.0F);
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
        drawTexture(matrix, this.x, this.y, k1, (int)d0, -0.0625F, 0.0F, 120, 60, 120, 120);
        drawTexture(matrix, this.x + k1, this.y, k1, (int)d0, 0.0625F, 60.0F, 120, 60, 120, 120);

        RenderSystem.defaultBlendFunc();

    }

}
