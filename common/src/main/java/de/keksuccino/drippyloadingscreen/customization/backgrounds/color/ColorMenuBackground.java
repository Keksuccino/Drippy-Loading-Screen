package de.keksuccino.drippyloadingscreen.customization.backgrounds.color;

import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.fancymenu.customization.background.MenuBackground;
import de.keksuccino.fancymenu.customization.background.MenuBackgroundBuilder;
import de.keksuccino.fancymenu.util.rendering.DrawableColor;
import de.keksuccino.fancymenu.util.rendering.RenderingUtils;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import java.awt.*;

public class ColorMenuBackground extends MenuBackground {

    @NotNull
    public DrawableColor color = DrawableColor.of(new Color(253, 87, 87));

    public ColorMenuBackground(MenuBackgroundBuilder<ColorMenuBackground> builder) {
        super(builder);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial) {

        RenderSystem.enableBlend();
        RenderingUtils.resetShaderColor(graphics);
        graphics.fill(0, 0, getScreenWidth(), getScreenHeight(), RenderingUtils.replaceAlphaInColor(this.color.getColorInt(), this.opacity));
        RenderingUtils.resetShaderColor(graphics);

    }

}
