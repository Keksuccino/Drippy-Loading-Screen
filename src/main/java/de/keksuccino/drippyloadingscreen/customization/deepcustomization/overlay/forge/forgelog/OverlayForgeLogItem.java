package de.keksuccino.drippyloadingscreen.customization.deepcustomization.overlay.forge.forgelog;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationElement;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationItem;
import de.keksuccino.konkrete.properties.PropertiesSection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.fml.loading.progress.StartupMessageManager;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.List;

public class OverlayForgeLogItem extends DeepCustomizationItem {

    public OverlayForgeLogItem(DeepCustomizationElement parentElement, PropertiesSection item) {
        super(parentElement, item);
    }

    @Override
    public void render(MatrixStack matrix, Screen menu) {

        this.posX = 10;
        this.posY = menu.height - 50;
        this.width = 300;
        this.height = 50;

        this.renderMessages();

    }

    private void renderMessages() {
        List<Pair<Integer, StartupMessageManager.Message>> messages = StartupMessageManager.getMessages();
        for (int i = 0; i < messages.size(); i++) {
            final Pair<Integer, StartupMessageManager.Message> pair = messages.get(i);
            StartupMessageManager.Message msg = pair.getRight();
            renderMessage(msg.getText(), msg.getTypeColour(), ((Minecraft.getInstance().getWindow().getGuiScaledHeight() - 15) / 10) - i + 1, 1.0F);
        }
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