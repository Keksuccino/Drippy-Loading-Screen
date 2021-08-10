package de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import de.keksuccino.konkrete.rendering.RenderUtils;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class ProgressBarSplashElement extends SplashElementBase {

    public String customBarColorHex = null;
    protected String lastCustomBarColorHex = null;
    public Color customBarColor;

    public ProgressBarSplashElement(SplashCustomizationLayer handler) {
        super(handler);

        int screenWidth = this.mc.getMainWindow().getScaledWidth();
        int screenHeight = this.mc.getMainWindow().getScaledHeight();
        double d0 = Math.min((double)screenWidth * 0.75D, screenHeight) * 0.25D;
        double d1 = d0 * 4.0D;
        int k1 = (int)(d1 * 0.5D);
        this.width = k1 * 2;
        this.height = 10;
    }

    @Override
    public void onReloadCustomizations() {
        super.onReloadCustomizations();

        this.customBarColorHex = null;
        this.lastCustomBarColorHex = null;
        this.customBarColor = null;
    }

    @Override
    public void render(MatrixStack matrix, int scaledWidth, int scaledHeight, float partialTicks) {

        if ((this.customBarColorHex != null) && !this.customBarColorHex.equals(this.lastCustomBarColorHex)) {
            this.customBarColor = RenderUtils.getColorFromHexString(this.customBarColorHex);
        }
        this.lastCustomBarColorHex = this.customBarColorHex;

        if (this.visible) {
            this.renderProgressBar(matrix);
        }

    }

    protected void renderProgressBar(MatrixStack matrix) {
        long time = Util.milliTime();
        float f = handler.fadeOutStart > -1L ? (float)(time - handler.fadeOutStart) / 1000.0F : -1.0F;
        int screenWidth = this.mc.getMainWindow().getScaledWidth();
        int screenHeight = this.mc.getMainWindow().getScaledHeight();
        double d0 = Math.min((double)screenWidth * 0.75D, screenHeight) * 0.25D;
        double d1 = d0 * 4.0D;
        int k1 = (int)(d1 * 0.5D);
        int l1 = (int)((double)screenHeight * 0.8325D);
        float barTransparency = 1.0F - MathHelper.clamp(f, 0.0F, 1.0F);

        //Update width and height
        this.width = k1 * 2;
        this.height = 10;

        this.renderProgressBarRaw(matrix, this.x, this.y, this.x + (k1 * 2), this.y + 10, barTransparency);
    }

    protected void renderProgressBarRaw(MatrixStack matrix, int minX, int minY, int maxX, int maxY, float alpha) {
        float prog = handler.progress;
        if (this.handler.isEditor || SplashCustomizationLayer.isCustomizationHelperScreen()) {
            prog = 0.5F;
        }
        int i = MathHelper.ceil((float)(maxX - minX - 2) * prog);
        int j = Math.round(alpha * 255.0F);
        if (this.handler.isEditor || SplashCustomizationLayer.isCustomizationHelperScreen() || DrippyLoadingScreen.isFancyMenuLoaded()) {
            j = 255;
        }
        int k = ColorHelper.PackedColor.packColor(j, 255, 255, 255);
        if (this.customBarColor != null) {
            k = ColorHelper.PackedColor.packColor(j, this.customBarColor.getRed(), this.customBarColor.getGreen(), this.customBarColor.getBlue());
        }
        fill(matrix, minX + 1, minY, maxX - 1, minY + 1, k);
        fill(matrix, minX + 1, maxY, maxX - 1, maxY - 1, k);
        fill(matrix, minX, minY, minX + 1, maxY, k);
        fill(matrix, maxX, minY, maxX - 1, maxY, k);
        fill(matrix, minX + 2, minY + 2, minX + i, maxY - 2, k);
    }

}
