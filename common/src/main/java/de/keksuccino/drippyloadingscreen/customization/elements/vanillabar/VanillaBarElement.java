package de.keksuccino.drippyloadingscreen.customization.elements.vanillabar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.mixin.mixins.common.client.IMixinLoadingOverlay;
import de.keksuccino.fancymenu.customization.element.AbstractElement;
import de.keksuccino.fancymenu.customization.element.ElementBuilder;
import de.keksuccino.fancymenu.util.rendering.DrawableColor;
import de.keksuccino.fancymenu.util.rendering.RenderingUtils;
import de.keksuccino.fancymenu.util.rendering.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
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

        RenderSystem.enableBlend();
        RenderingUtils.resetShaderColor(graphics);
        this.drawProgressBar(graphics, x, y, x + w, y + h, currentProgress);
        RenderingUtils.resetShaderColor(graphics);

    }

    protected void drawProgressBar(GuiGraphics graphics, int xMin, int yMin, int xMax, int yMax, float currentProgress) {
        int i = Mth.ceil((float)(xMax - xMin - 2) * currentProgress);
        int k = RenderingUtils.replaceAlphaInColor(this.color.getColorInt(), this.opacity);
        graphics.fill(xMin + 2, yMin + 2, xMin + i, yMax - 2, k);
        graphics.fill(xMin + 1, yMin, xMax - 1, yMin + 1, k);
        graphics.fill(xMin + 1, yMax, xMax - 1, yMax - 1, k);
        graphics.fill(xMin, yMin, xMin + 1, yMax, k);
        graphics.fill(xMax, yMin, xMax - 1, yMax, k);
    }

}
