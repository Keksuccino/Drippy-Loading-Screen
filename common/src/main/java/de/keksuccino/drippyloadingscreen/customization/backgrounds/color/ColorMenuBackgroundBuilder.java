package de.keksuccino.drippyloadingscreen.customization.backgrounds.color;

import de.keksuccino.fancymenu.customization.background.MenuBackgroundBuilder;
import de.keksuccino.fancymenu.util.properties.PropertyContainer;
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
    public @NotNull ColorMenuBackground buildDefaultInstance() {
        return new ColorMenuBackground(this);
    }

    @Override
    public void deserializeBackground(@NotNull PropertyContainer serializedMenuBackground, @NotNull ColorMenuBackground deserializeTo) {
        String hex = serializedMenuBackground.getValue("color");
        if (hex != null) deserializeTo.color = DrawableColor.of(hex);

    }

    @Override
    public void serializeBackground(@NotNull ColorMenuBackground background, @NotNull PropertyContainer serializeTo) {
        serializeTo.putProperty("color", background.color.getHex());

    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("drippyloadingscreen.background.color");
    }

    @Override
    public @Nullable Component getDescription() {
        return Component.translatable("drippyloadingscreen.background.color.desc");
    }

}
