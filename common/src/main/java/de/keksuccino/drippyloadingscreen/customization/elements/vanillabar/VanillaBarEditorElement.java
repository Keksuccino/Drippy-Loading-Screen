package de.keksuccino.drippyloadingscreen.customization.elements.vanillabar;

import de.keksuccino.fancymenu.customization.element.AbstractElement;
import de.keksuccino.fancymenu.customization.element.editor.AbstractEditorElement;
import de.keksuccino.fancymenu.customization.layout.editor.LayoutEditorScreen;
import de.keksuccino.fancymenu.util.input.TextValidators;
import de.keksuccino.fancymenu.util.rendering.DrawableColor;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class VanillaBarEditorElement extends AbstractEditorElement {

    public VanillaBarEditorElement(@NotNull AbstractElement element, @NotNull LayoutEditorScreen editor) {
        super(element, editor);
    }

    @Override
    public void init() {

        super.init();

        this.addStringInputContextMenuEntryTo(this.rightClickMenu, "bar_color",
                        VanillaBarEditorElement.class,
                        consumes -> consumes.getElement().color.getHex(),
                        (vanillaBarEditorElement, s) -> vanillaBarEditorElement.getElement().color = DrawableColor.of(s),
                        null, false, false, Component.translatable("drippyloadingscreen.elements.vanilla_bar.color"),
                        true, DrawableColor.WHITE.getHex(), TextValidators.HEX_COLOR_TEXT_VALIDATOR, null)
                .setStackable(true);

    }

    public VanillaBarElement getElement() {
        return (VanillaBarElement) this.element;
    }

}
