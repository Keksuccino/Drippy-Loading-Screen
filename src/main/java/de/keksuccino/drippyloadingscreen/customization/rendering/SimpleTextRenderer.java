//TODO übernehmen
package de.keksuccino.drippyloadingscreen.customization.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.konkrete.rendering.RenderUtils;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class SimpleTextRenderer {

    private static final Identifier DEFAULT_FONT = new Identifier("textures/font/ascii.png");
    private static final Map<Character, Integer> CHARACTER_X_OFFSET = new HashMap<Character, Integer>();
    private static final Map<Character, Integer> CHARACTER_HEIGHT_OFFSET = new HashMap<Character, Integer>();

    public static void init() {

        CHARACTER_X_OFFSET.clear();

        CHARACTER_X_OFFSET.put(" ".charAt(0), -3);
        CHARACTER_X_OFFSET.put("!".charAt(0), -4);
        CHARACTER_X_OFFSET.put(".".charAt(0), -4);
        CHARACTER_X_OFFSET.put(",".charAt(0), -4);
        CHARACTER_X_OFFSET.put(":".charAt(0), -4);
        CHARACTER_X_OFFSET.put(";".charAt(0), -4);
        CHARACTER_X_OFFSET.put("*".charAt(0), -3);
        CHARACTER_X_OFFSET.put("'".charAt(0), -4);
        CHARACTER_X_OFFSET.put("´".charAt(0), -4);
        CHARACTER_X_OFFSET.put("`".charAt(0), -4);
        CHARACTER_X_OFFSET.put("/".charAt(0), -3);
        CHARACTER_X_OFFSET.put("\\".charAt(0), -3);
        CHARACTER_X_OFFSET.put("}".charAt(0), -3);
        CHARACTER_X_OFFSET.put("{".charAt(0), -3);
        CHARACTER_X_OFFSET.put(")".charAt(0), -3);
        CHARACTER_X_OFFSET.put("(".charAt(0), -3);
        CHARACTER_X_OFFSET.put("]".charAt(0), -3);
        CHARACTER_X_OFFSET.put("[".charAt(0), -3);
        CHARACTER_X_OFFSET.put("1".charAt(0), -3);
        CHARACTER_X_OFFSET.put("i".charAt(0), -4);
        CHARACTER_X_OFFSET.put("I".charAt(0), -2);
        CHARACTER_X_OFFSET.put("l".charAt(0), -3);
        CHARACTER_X_OFFSET.put("t".charAt(0), -2);
        CHARACTER_X_OFFSET.put("k".charAt(0), -1);

        CHARACTER_HEIGHT_OFFSET.clear();

        CHARACTER_HEIGHT_OFFSET.put("p".charAt(0), 1);
        CHARACTER_HEIGHT_OFFSET.put("q".charAt(0), 1);
        CHARACTER_HEIGHT_OFFSET.put("y".charAt(0), 1);
        CHARACTER_HEIGHT_OFFSET.put("j".charAt(0), 1);
        CHARACTER_HEIGHT_OFFSET.put("g".charAt(0), 1);
        CHARACTER_HEIGHT_OFFSET.put("@".charAt(0), 1);

    }

    public static void drawString(MatrixStack matrix, String text, int x, int y, int rgbColor, float alpha, float scale) {

        float[] color = getColor(rgbColor);

        int xOffset = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c > 0xFF) {
                continue;
            }
            int charX = (c & 0x0F) * 8;
            int charY = (c >> 4 & 0x0F) * 8;
            int heightOffset = 0;
            if (CHARACTER_HEIGHT_OFFSET.containsKey(c)) {
                heightOffset = CHARACTER_HEIGHT_OFFSET.get(c);
            }
            RenderUtils.bindTexture(DEFAULT_FONT, true);
            matrix.push();
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(color[0], color[1], color[2], alpha);
            matrix.translate((x + ((i * 6) * scale)) + (xOffset * scale), y, 0);
            matrix.scale(scale, scale, 0);
            DrawableHelper.drawTexture(matrix, 0, 0, charX, charY, 6, 7 + heightOffset, 128, 128); //charX  charY  6  7  128  128
            //Apply char offset for next char
            if (CHARACTER_X_OFFSET.containsKey(c)) {
                xOffset += CHARACTER_X_OFFSET.get(c);
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            matrix.pop();
        }

    }

    public static void drawStringWithShadow(MatrixStack matrix, String text, int x, int y, int rgbColor, float alpha, float scale) {
        //draw shadow
        drawString(matrix, text, x + Math.max((int)(1 * scale), 1), y + Math.max((int)(1 * scale), 1), 0, alpha / 2.0F, scale);
        //draw normal text
        drawString(matrix, text, x, y, rgbColor, alpha, scale);
    }

    public static int getStringWidth(String text) {
        int length = 0;
        for (char c : text.toCharArray()) {
            int i = 6;
            if (CHARACTER_X_OFFSET.containsKey(c)) {
                i += CHARACTER_X_OFFSET.get(c);
            }
            length += i;
        }
        return length;
    }

    public static int getStringHeight() {
        return 7;
    }

    protected static float[] getColor(int rgb) {
        float[] color = new float[] { 0.0f, 0.0f, 0.0f};
        color[2] = ((rgb) & 0xFF) / 255.0f;
        color[1] = ((rgb >> 8 ) & 0xFF) / 255.0f;
        color[0] = ((rgb >> 16 ) & 0xFF) / 255.0f;
        return color;
    }

}
