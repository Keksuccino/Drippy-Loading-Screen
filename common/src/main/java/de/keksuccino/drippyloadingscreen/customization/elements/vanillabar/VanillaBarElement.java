package de.keksuccino.drippyloadingscreen.customization.elements.vanillabar;

import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.drippyloadingscreen.mixin.mixins.common.client.IMixinLoadingOverlay;
import de.keksuccino.fancymenu.customization.element.AbstractElement;
import de.keksuccino.fancymenu.customization.element.ElementBuilder;
import de.keksuccino.fancymenu.util.rendering.DrawableColor;
import de.keksuccino.fancymenu.util.rendering.RenderingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class VanillaBarElement extends AbstractElement {

    @NotNull
    public DrawableColor color = DrawableColor.WHITE;

    public VanillaBarElement(@NotNull ElementBuilder<?, ?> builder) {
        super(builder);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial) {

        int x = this.getAbsoluteX();
        int y = this.getAbsoluteY();
        int w = this.getAbsoluteWidth();
        int h = this.getAbsoluteHeight();
        float currentProgress = 0.5F;
        if (Minecraft.getInstance().getOverlay() instanceof LoadingOverlay) {
            currentProgress = ((IMixinLoadingOverlay)Minecraft.getInstance().getOverlay()).getCurrentProgressDrippy();
        }

        this.drawProgressBar(graphics, x, y, x + w, y + h, currentProgress);

    }

    protected void drawProgressBar(GuiGraphics graphics, int xMin, int yMin, int xMax, int yMax, float currentProgress) {
        int i = Mth.ceil((float)(xMax - xMin - 2) * currentProgress);
        int k = mergeOpacity(this.color.getColorInt(), this.opacity);
        graphics.fill(xMin + 2, yMin + 2, xMin + i, yMax - 2, k);
        graphics.fill(xMin + 1, yMin, xMax - 1, yMin + 1, k);
        graphics.fill(xMin + 1, yMax, xMax - 1, yMax - 1, k);
        graphics.fill(xMin, yMin, xMin + 1, yMax, k);
        graphics.fill(xMax, yMin, xMax - 1, yMax, k);
    }

    /**
     * Multiplies a color's existing alpha by the given opacity and returns the new ARGB color,
     * enforcing a minimum final opacity of 2% (0.02f).
     *
     * @param color   ARGB color int (0xAARRGGBB). The current alpha (AA) will be used.
     * @param opacity Opacity multiplier in [0.0f, 1.0f]. 1 keeps the same alpha; 0 makes it fully transparent,
     *                but the final opacity is clamped to at least 0.02f.
     * @return The color int with the merged (multiplied) opacity and a 2% minimum.
     */
    private static int mergeOpacity(int color, float opacity) {
        // Clamp opacity input to [0, 1]
        float op = Math.max(0f, Math.min(1f, opacity));

        int originalAlpha = (color >>> 24) & 0xFF;      // 0..255
        int newAlpha = Math.round(originalAlpha * op);  // multiply opacities

        // Enforce minimum final opacity of 2% (≈ 5/255)
        final int MIN_ALPHA = Math.round(0.02f * 255f);
        newAlpha = Math.max(MIN_ALPHA, Math.min(255, newAlpha));

        // Clear old alpha and set new one
        return (color & 0x00FFFFFF) | (newAlpha << 24);
    }

}
