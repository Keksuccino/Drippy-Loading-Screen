package de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import de.keksuccino.konkrete.rendering.RenderUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.loading.progress.StartupMessageManager;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.List;

public class ForgeTextSplashElement extends SplashElementBase {

    public String customTextColorHex = null;
    protected String lastCustomTextColorHex = null;
    public Color customTextColor;

    public ForgeTextSplashElement(SplashCustomizationLayer handler) {
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
            this.renderForgeText();
        }

    }

    protected void renderForgeText() {

        //Update width and height
        this.width = (int) (300 * this.scale);
        this.height = (int) (50 * this.scale);

        List<Pair<Integer, StartupMessageManager.Message>> messages = StartupMessageManager.getMessages();
        for (int i = 0; i < messages.size(); i++) {
            boolean noFade = i == 0;
            final Pair<Integer, StartupMessageManager.Message> pair = messages.get(i);
            final float fade = MathHelper.clamp((4000.0f - (float) pair.getLeft() - ( i - 4 ) * 1000.0f) / 5000.0f, 0.0f, 1.0f);
            if (fade <0.01f && !noFade) continue;
            StartupMessageManager.Message msg = pair.getRight();
            renderMessage(msg.getText(), msg.getTypeColour(), i, noFade ? 1.0f : fade);
        }

    }

    private void renderMessage(final String message, float[] color, int line, float alpha) {
        GlStateManager.enableClientState(GL11.GL_VERTEX_ARRAY);
        ByteBuffer charBuffer = MemoryUtil.memAlloc(message.length() * 270);
        int quads = STBEasyFont.stb_easy_font_print(0, 0, message, null, charBuffer);
        GL14.glVertexPointer(2, GL11.GL_FLOAT, 16, charBuffer);

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        GL14.glBlendColor(0, 0, 0, alpha);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
        if (this.customTextColor != null) {
            color = getColor(this.customTextColor);
        }
        RenderSystem.color3f(color[0],color[1],color[2]);
        RenderSystem.pushMatrix();
        int posY = (this.y + this.height - (int)(10 * this.scale)) - (line * (int)(10 * this.scale));
        RenderSystem.translatef(x, posY, 0);
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
