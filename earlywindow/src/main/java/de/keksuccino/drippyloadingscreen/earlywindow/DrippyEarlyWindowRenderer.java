package de.keksuccino.drippyloadingscreen.earlywindow;

import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBEasyFont;

/**
 * Shared immediate-mode renderer for the Drippy early window and the fallback display window bridge.
 */
final class DrippyEarlyWindowRenderer {

    private static final float BG_RED = 0.69f;
    private static final float BG_GREEN = 0.87f;
    private static final float BG_BLUE = 0.94f;

    private DrippyEarlyWindowRenderer() {}

    static void renderFrame(int logicalWidth, int logicalHeight, int framebufferWidth, int framebufferHeight, ByteBuffer textBuffer, String text) {
        GL11.glViewport(0, 0, framebufferWidth, framebufferHeight);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glClearColor(BG_RED, BG_GREEN, BG_BLUE, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0, logicalWidth, logicalHeight, 0.0, -1.0, 1.0);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        renderText(logicalWidth, logicalHeight, textBuffer, text);
    }

    private static void renderText(int logicalWidth, int logicalHeight, ByteBuffer textBuffer, String text) {
        textBuffer.clear();
        textBuffer.limit(textBuffer.capacity());
        float textWidth = STBEasyFont.stb_easy_font_width(text);
        float x = Math.max(16.0f, (logicalWidth - textWidth) / 2.0f);
        float y = logicalHeight / 2.0f;
        int quads = STBEasyFont.stb_easy_font_print(x, y, text, null, textBuffer);
        textBuffer.flip();

        GL11.glColor3f(0.05f, 0.05f, 0.05f);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glVertexPointer(2, GL11.GL_FLOAT, 16, textBuffer);
        GL11.glDrawArrays(GL11.GL_QUADS, 0, quads * 4);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }
}
