package de.keksuccino.drippyloadingscreen.customization.backgrounds.color;

import de.keksuccino.fancymenu.customization.background.MenuBackgroundBuilder;
import de.keksuccino.fancymenu.customization.background.SerializedMenuBackground;
import de.keksuccino.fancymenu.util.LocalizationUtils;
import de.keksuccino.fancymenu.util.rendering.DrawableColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

public class ColorMenuBackgroundBuilder extends MenuBackgroundBuilder<ColorMenuBackground> {

    public ColorMenuBackgroundBuilder() {
        super("drippy_color_background");
    }

    @Override
    public boolean isDeprecated() {
        return true;
    }

    @Override
    public void buildNewOrEditInstance(Screen currentScreen, @Nullable ColorMenuBackground backgroundToEdit, @NotNull Consumer<ColorMenuBackground> backgroundConsumer) {
        ColorMenuBackground back = (backgroundToEdit != null) ? (ColorMenuBackground) backgroundToEdit.copy() : null;
        if (back == null) {
            back = new ColorMenuBackground(this);
        }
        ColorMenuBackgroundConfigScreen s = new ColorMenuBackgroundConfigScreen(back, background -> {
           if (background != null) {
               backgroundConsumer.accept(background);
           } else {
               backgroundConsumer.accept(backgroundToEdit);
           }
           Minecraft.getInstance().setScreen(currentScreen);
        });
        Minecraft.getInstance().setScreen(s);
    }

    @Override
    public ColorMenuBackground deserializeBackground(SerializedMenuBackground serializedMenuBackground) {

        ColorMenuBackground b = new ColorMenuBackground(this);

        String hex = serializedMenuBackground.getValue("color");
        if (hex != null) b.color = DrawableColor.of(hex);

        return b;

    }

    @Override
    public SerializedMenuBackground serializedBackground(ColorMenuBackground background) {

        SerializedMenuBackground serialized = new SerializedMenuBackground();

        serialized.putProperty("color", background.color.getHex());

        return serialized;

    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("drippyloadingscreen.background.color");
    }

    @Override
    public @Nullable Component[] getDescription() {
        return LocalizationUtils.splitLocalizedLines("drippyloadingscreen.background.color.desc");
    }

    @Override
    public boolean isDeprecated() {
        return true;
    }

}
