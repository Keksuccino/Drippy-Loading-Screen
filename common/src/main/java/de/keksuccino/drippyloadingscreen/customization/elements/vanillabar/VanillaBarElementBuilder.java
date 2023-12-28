package de.keksuccino.drippyloadingscreen.customization.elements.vanillabar;

import de.keksuccino.fancymenu.customization.element.AbstractElement;
import de.keksuccino.fancymenu.customization.element.ElementBuilder;
import de.keksuccino.fancymenu.customization.element.SerializedElement;
import de.keksuccino.fancymenu.customization.layout.editor.LayoutEditorScreen;
import de.keksuccino.fancymenu.util.LocalizationUtils;
import de.keksuccino.fancymenu.util.rendering.DrawableColor;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VanillaBarElementBuilder extends ElementBuilder<VanillaBarElement, VanillaBarEditorElement> {

    public VanillaBarElementBuilder() {
        super("drippy_vanilla_bar");
    }

    @Override
    public @NotNull VanillaBarElement buildDefaultInstance() {
        VanillaBarElement i = new VanillaBarElement(this);
        i.baseWidth = 200;
        i.baseHeight = 10;
        return i;
    }

    @Override
    public VanillaBarElement deserializeElement(@NotNull SerializedElement serialized) {

        VanillaBarElement element = this.buildDefaultInstance();

        String hex = serialized.getValue("color");
        if (hex != null) element.color = DrawableColor.of(hex);

        return element;

    }

    @Override
    protected SerializedElement serializeElement(@NotNull VanillaBarElement element, @NotNull SerializedElement serializeTo) {

        serializeTo.putProperty("color", element.color.getHex());

        return serializeTo;
        
    }

    @Override
    public @NotNull VanillaBarEditorElement wrapIntoEditorElement(@NotNull VanillaBarElement element, @NotNull LayoutEditorScreen editor) {
        return new VanillaBarEditorElement(element, editor);
    }

    @Override
    public @NotNull Component getDisplayName(@Nullable AbstractElement element) {
        return Component.translatable("drippyloadingscreen.elements.vanilla_bar");
    }

    @Override
    public @Nullable Component[] getDescription(@Nullable AbstractElement element) {
        return LocalizationUtils.splitLocalizedLines("drippyloadingscreen.elements.vanilla_bar.desc");
    }

}
