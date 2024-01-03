package de.keksuccino.drippyloadingscreen.customization.backgrounds.color;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.fancymenu.customization.background.MenuBackground;
import de.keksuccino.fancymenu.customization.background.MenuBackgroundBuilder;
import de.keksuccino.fancymenu.util.rendering.DrawableColor;
import de.keksuccino.fancymenu.util.rendering.RenderingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;
import java.awt.*;

public class ColorMenuBackground extends MenuBackground {

    @NotNull
    public DrawableColor color = DrawableColor.of(new Color(253, 87, 87));

    public ColorMenuBackground(MenuBackgroundBuilder<ColorMenuBackground> builder) {
        super(builder);
    }

    @Override
    public void render(@NotNull PoseStack graphics, int mouseX, int mouseY, float partial) {

        RenderingUtils.resetShaderColor();
        int colorToRender = FastColor.ARGB32.color(255, this.color.getColor().getRed(), this.color.getColor().getGreen(), this.color.getColor().getBlue());
        colorToRender = replaceAlpha(colorToRender, (int)(this.opacity * 255.0F));
        if (Minecraft.getInstance().getOverlay() instanceof LoadingOverlay) {
            GuiComponent.fill(graphics, 0, 0, getScreenWidth(), getScreenHeight(), colorToRender);
        } else {
            GuiComponent.fill(graphics, 0, 0, getScreenWidth(), getScreenHeight(), colorToRender);
        }
        RenderingUtils.resetShaderColor();

    }

    private static int replaceAlpha(int color, int alpha) {
        if (alpha > 255) alpha = 255;
        if (alpha < 0) alpha = 0;
        return color & 16777215 | alpha << 24;
    }

}
