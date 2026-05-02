package de.keksuccino.drippyloadingscreen.customization.backgrounds.color;

import de.keksuccino.fancymenu.customization.background.MenuBackground;
import de.keksuccino.fancymenu.customization.background.MenuBackgroundBuilder;
import de.keksuccino.fancymenu.customization.layout.editor.LayoutEditorScreen;
import de.keksuccino.fancymenu.util.LocalizationUtils;
import de.keksuccino.fancymenu.util.input.TextValidators;
import de.keksuccino.fancymenu.util.rendering.DrawableColor;
import de.keksuccino.fancymenu.util.rendering.RenderingUtils;
import de.keksuccino.fancymenu.util.rendering.ui.contextmenu.v2.ContextMenu;
import de.keksuccino.fancymenu.util.rendering.ui.icon.MaterialIcons;
import de.keksuccino.fancymenu.util.rendering.ui.tooltip.UITooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;
import java.awt.*;

public class ColorMenuBackground extends MenuBackground<ColorMenuBackground> {

    @NotNull
    public DrawableColor color = DrawableColor.of(new Color(253, 87, 87));

    public ColorMenuBackground(MenuBackgroundBuilder<ColorMenuBackground> builder) {
        super(builder);
    }

    @Override
    protected void initConfigMenu(@NotNull ContextMenu menu, @NotNull LayoutEditorScreen editor) {
        this.addStringInputContextMenuEntryTo(menu, "color", ColorMenuBackground.class,
                        background -> background.color.getHex(),
                        (background, value) -> background.color = DrawableColor.of(value),
                        null, false, false, Component.translatable("drippyloadingscreen.background.color.config.color"),
                        true, DrawableColor.WHITE.getHex(), TextValidators.HEX_COLOR_TEXT_VALIDATOR,
                        value -> UITooltip.of(LocalizationUtils.splitLocalizedLines("drippyloadingscreen.background.color.config.invalid_color")))
                .setIcon(MaterialIcons.PALETTE);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial) {

        RenderingUtils.resetShaderColor(graphics);
        int colorToRender = FastColor.ARGB32.color(255, this.color.getColor().getRed(), this.color.getColor().getGreen(), this.color.getColor().getBlue());
        colorToRender = replaceAlpha(colorToRender, (int)(this.opacity * 255.0F));
        if (Minecraft.getInstance().getOverlay() instanceof LoadingOverlay) {
            graphics.fill(RenderType.guiOverlay(), 0, 0, getScreenWidth(), getScreenHeight(), colorToRender);
        } else {
            graphics.fill(0, 0, getScreenWidth(), getScreenHeight(), colorToRender);
        }
        RenderingUtils.resetShaderColor(graphics);

    }

    private static int replaceAlpha(int color, int alpha) {
        if (alpha > 255) alpha = 255;
        if (alpha < 0) alpha = 0;
        return color & 16777215 | alpha << 24;
    }

}
