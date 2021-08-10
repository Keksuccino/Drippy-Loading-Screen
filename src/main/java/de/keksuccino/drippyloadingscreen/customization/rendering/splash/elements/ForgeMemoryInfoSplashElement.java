package de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import de.keksuccino.konkrete.rendering.RenderUtils;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.nio.ByteBuffer;

public class ForgeMemoryInfoSplashElement extends SplashElementBase {

    private static final float[] MEM_COLOR = new float[] { 0.0f, 0.0f, 0.0f};

    public String customTextColorHex = null;
    protected String lastCustomTextColorHex = null;
    public Color customTextColor;

    public ForgeMemoryInfoSplashElement(SplashCustomizationLayer handler) {
        super(handler);
    }

    @Override
    public void onReloadCustomizations() {
        super.onReloadCustomizations();

        this.customTextColorHex = null;
        this.lastCustomTextColorHex = null;
        this.customTextColor = null;
    }

    @Override
    public void render(MatrixStack matrix, int scaledWidth, int scaledHeight, float partialTicks) {

        if ((this.customTextColorHex != null) && !this.customTextColorHex.equals(this.lastCustomTextColorHex)) {
            this.customTextColor = RenderUtils.getColorFromHexString(this.customTextColorHex);
        }
        this.lastCustomTextColorHex = this.customTextColorHex;

        if (this.visible) {
            this.renderMemoryInfo();
        }

    }

    protected void renderMemoryInfo() {
        final MemoryUsage heapusage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        final MemoryUsage offheapusage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
        final float pctmemory = (float) heapusage.getUsed() / heapusage.getMax();
        String currentText = String.format("Memory Heap: %d / %d MB (%.1f%%)  OffHeap: %d MB", heapusage.getUsed() >> 20, heapusage.getMax() >> 20, pctmemory * 100.0, offheapusage.getUsed() >> 20);

        //Update width and height
        this.width = (int) (STBEasyFont.stb_easy_font_width(currentText) * this.scale);
        this.height = (int) (10 * this.scale);

        final int i = MathHelper.hsvToRGB((1.0f - (float)Math.pow(pctmemory, 1.5f)) / 3f, 1.0f, 0.5f);
        MEM_COLOR[2] = ((i) & 0xFF) / 255.0f;
        MEM_COLOR[1] = ((i >> 8 ) & 0xFF) / 255.0f;
        MEM_COLOR[0] = ((i >> 16 ) & 0xFF) / 255.0f;
        renderMessage(currentText, MEM_COLOR, this.x, this.y, 1.0f);
    }

    protected void renderMessage(final String message, float[] color, int x, int y, float alpha) {
        GlStateManager.enableClientState(GL11.GL_VERTEX_ARRAY);
        ByteBuffer charBuffer = MemoryUtil.memAlloc(message.length() * 270);
        int quads = STBEasyFont.stb_easy_font_print(0, 0, message, null, charBuffer);
        GL14.glVertexPointer(2, GL11.GL_FLOAT, 16, charBuffer);

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        GL14.glBlendColor(0,0,0, alpha);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
        if (this.customTextColor != null) {
            color = getColor(this.customTextColor);
        }
        RenderSystem.color3f(color[0],color[1],color[2]);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, 0);
        RenderSystem.scalef(this.scale, this.scale, 0);
        RenderSystem.drawArrays(GL11.GL_QUADS, 0, quads * 4);
        RenderSystem.popMatrix();

        RenderSystem.enableCull();
        GlStateManager.disableClientState(GL11.GL_VERTEX_ARRAY);
        MemoryUtil.memFree(charBuffer);
    }

    protected static float[] getColor(Color c) {
        float[] color = new float[] { 0.0f, 0.0f, 0.0f};
        final int i = c.getRGB();
        color[2] = ((i) & 0xFF) / 255.0f;
        color[1] = ((i >> 8 ) & 0xFF) / 255.0f;
        color[0] = ((i >> 16 ) & 0xFF) / 255.0f;
        return color;
    }

}
