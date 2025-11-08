package de.keksuccino.drippyloadingscreen.earlywindow;

import java.nio.ByteBuffer;
import net.neoforged.fml.earlydisplay.ColourScheme;
import net.neoforged.fml.earlydisplay.DisplayWindow;
import net.neoforged.fml.earlydisplay.RenderElement;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * Minimal DisplayWindow implementation that re-renders the Drippy scene into a texture NeoForge's
 * loading overlay can consume once Minecraft takes over.
 */
final class DrippyDisplayWindowBridge extends DisplayWindow {

    private final DrippyEarlyWindowProvider provider;
    private final ByteBuffer textBuffer = BufferUtils.createByteBuffer(64 * 1024);
    private final RenderElement.DisplayContext context;
    private final int logicalWidth;
    private final int logicalHeight;

    private int framebuffer;
    private int texture;
    private boolean dirty = true;

    DrippyDisplayWindowBridge(DrippyEarlyWindowProvider provider) {
        this.provider = provider;
        this.logicalWidth = Math.max(1, provider.overlayLogicalWidth());
        this.logicalHeight = Math.max(1, provider.overlayLogicalHeight());
        this.context = new RenderElement.DisplayContext(this.logicalWidth, this.logicalHeight, 1, null, ColourScheme.BLACK, null);
    }

    @Override
    public void addMojangTexture(int textureId) {
        // The Drippy overlay renders its own visuals; ignore Mojang texture injection.
    }

    @Override
    public RenderElement.DisplayContext context() {
        return this.context;
    }

    @Override
    public void render(int alpha) {
        ensureFramebuffer();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.framebuffer);
        DrippyEarlyWindowRenderer.renderFrame(this.logicalWidth, this.logicalHeight, this.logicalWidth, this.logicalHeight, this.textBuffer, provider.overlayText());
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        this.dirty = false;
    }

    @Override
    public int getFramebufferTextureId() {
        ensureFramebuffer();
        if (this.dirty) {
            render(0xFF);
        }
        return this.texture;
    }

    @Override
    public void close() {
        if (this.texture != 0) {
            GL11.glDeleteTextures(this.texture);
            this.texture = 0;
        }
        if (this.framebuffer != 0) {
            GL30.glDeleteFramebuffers(this.framebuffer);
            this.framebuffer = 0;
        }
    }

    private void ensureFramebuffer() {
        if (this.framebuffer != 0) {
            return;
        }
        this.texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, this.logicalWidth, this.logicalHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        this.framebuffer = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.framebuffer);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, this.texture, 0);
        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Failed to initialize Drippy loading overlay framebuffer: status " + status);
        }
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        this.dirty = true;
    }
}
