package de.keksuccino.drippyloadingscreen.customization.deepcustomization.overlay.forge.forgememory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationElement;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationItem;
import de.keksuccino.konkrete.properties.PropertiesSection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class OverlayForgeMemoryItem extends DeepCustomizationItem {

    private static final float[] MEMORY_COLOR = new float[] { 0.0f, 0.0f, 0.0f};

    public OverlayForgeMemoryItem(DeepCustomizationElement parentElement, PropertiesSection item) {
        super(parentElement, item);
    }

    @Override
    public void render(MatrixStack matrix, Screen menu) {

        this.posX = 10;
        this.posY = 10;
        this.width = 300;
        this.height = 10;

        this.renderMemoryInfo();

    }

    private void renderMemoryInfo() {
        final float pctmemory = (float) 10 / 1000;
        String memory = "Memory Heap: 1000 / 1000 MB (100%)  OffHeap: 1000 MB";
        final int i = MathHelper.hsvToRgb((1.0f - (float)Math.pow(pctmemory, 1.5f)) / 3f, 1.0f, 0.5f);
        MEMORY_COLOR[2] = ((i) & 0xFF) / 255.0f;
        MEMORY_COLOR[1] = ((i >> 8 ) & 0xFF) / 255.0f;
        MEMORY_COLOR[0] = ((i >> 16 ) & 0xFF) / 255.0f;
        renderMessage(memory, MEMORY_COLOR, 1, 1.0f);
    }

    private void renderMessage(final String message, final float[] colour, int line, float alpha) {
        GlStateManager._enableClientState(GL11.GL_VERTEX_ARRAY);
        ByteBuffer charBuffer = MemoryUtil.memAlloc(message.length() * 270);
        int quads = STBEasyFont.stb_easy_font_print(0, 0, message, null, charBuffer);
        GL14.glVertexPointer(2, GL11.GL_FLOAT, 16, charBuffer);

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        GL14.glBlendColor(0,0,0, alpha);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
        RenderSystem.color3f(colour[0],colour[1],colour[2]);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(10, line * 10, 0);
        RenderSystem.scalef(1, 1, 0);
        RenderSystem.drawArrays(GL11.GL_QUADS, 0, quads * 4);
        RenderSystem.popMatrix();

        RenderSystem.enableCull();
        GlStateManager._disableClientState(GL11.GL_VERTEX_ARRAY);
        MemoryUtil.memFree(charBuffer);
    }

}