package de.keksuccino.drippyloadingscreen.customization.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.ColorHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.nio.ByteBuffer;

public class SimpleTextRenderer {

    public static void drawString(String text, int x, int y, int rgbColor, float alpha, float scale) {

        GlStateManager.enableClientState(GL11.GL_VERTEX_ARRAY);
        ByteBuffer charBuffer = MemoryUtil.memAlloc(text.length() * 270);
        int quads = STBEasyFont.stb_easy_font_print(0, 0, text, null, charBuffer);
        GL14.glVertexPointer(2, GL11.GL_FLOAT, 16, charBuffer);
        float[] color = getColor(rgbColor);

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        GL14.glBlendColor(0, 0, 0, alpha);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
        RenderSystem.color3f(color[0],color[1],color[2]);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, 0);
        RenderSystem.scalef(scale, scale, 0);
        RenderSystem.drawArrays(GL11.GL_QUADS, 0, quads * 4);
        RenderSystem.popMatrix();

        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        RenderSystem.defaultBlendFunc();
        GlStateManager.disableClientState(GL11.GL_VERTEX_ARRAY);
        MemoryUtil.memFree(charBuffer);

    }

    public static void drawStringWithShadow(String text, int x, int y, int rgbColor, float alpha, float scale) {
        //draw shadow
        Color c = new Color(ColorHelper.PackedColor.getRed(rgbColor), ColorHelper.PackedColor.getGreen(rgbColor), ColorHelper.PackedColor.getBlue(rgbColor));
        c = c.darker().darker().darker();
        drawString(text, x + Math.max((int)(1 * scale), 1), y + Math.max((int)(1 * scale), 1), c.getRGB(), alpha / 2, scale);
        //draw normal text
        drawString(text, x, y, rgbColor, alpha, scale);
    }

    public static int getStringWidth(String text) {
        return STBEasyFont.stb_easy_font_width(text);
    }

    public static int getStringHeight(String text) {
        return STBEasyFont.stb_easy_font_height(text);
    }

    protected static float[] getColor(int rgb) {
        float[] color = new float[] { 0.0f, 0.0f, 0.0f};
        color[2] = ((rgb) & 0xFF) / 255.0f;
        color[1] = ((rgb >> 8 ) & 0xFF) / 255.0f;
        color[0] = ((rgb >> 16 ) & 0xFF) / 255.0f;
        return color;
    }

}
