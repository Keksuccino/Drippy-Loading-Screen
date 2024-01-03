package de.keksuccino.drippyloadingscreen.customization.backgrounds.color;

import de.keksuccino.fancymenu.util.LocalizationUtils;
import de.keksuccino.fancymenu.util.input.TextValidators;
import de.keksuccino.fancymenu.util.rendering.DrawableColor;
import de.keksuccino.fancymenu.util.rendering.text.Components;
import de.keksuccino.fancymenu.util.rendering.ui.screen.CellScreen;
import de.keksuccino.fancymenu.util.rendering.ui.tooltip.Tooltip;
import org.jetbrains.annotations.NotNull;
import java.util.function.Consumer;

public class ColorMenuBackgroundConfigScreen extends CellScreen {

    @NotNull
    protected Consumer<ColorMenuBackground> callback;
    @NotNull
    protected ColorMenuBackground background;
    @NotNull
    protected String colorHex;

    protected ColorMenuBackgroundConfigScreen(@NotNull ColorMenuBackground background, @NotNull Consumer<ColorMenuBackground> callback) {
        super(Components.translatable("drippyloadingscreen.background.color.config"));
        this.background = background;
        this.colorHex = this.background.color.getHex();
        this.callback = callback;
    }

    @Override
    protected void initCells() {

        this.addStartEndSpacerCell();

        this.addLabelCell(Components.translatable("drippyloadingscreen.background.color.config.color"));

        this.addTextInputCell(null, false, false)
                .setEditListener(s -> this.colorHex = s)
                .setText(this.colorHex);

        this.addStartEndSpacerCell();

    }

    @Override
    protected void init() {

        super.init();

        if (this.doneButton != null) {
            this.doneButton.setTooltipSupplier(consumes -> {
                if (!TextValidators.HEX_COLOR_TEXT_VALIDATOR.get(this.colorHex)) {
                    return Tooltip.of(LocalizationUtils.splitLocalizedLines("drippyloadingscreen.background.color.config.invalid_color")).setDefaultStyle();
                }
                return null;
            });
        }

    }

    @Override
    public boolean allowDone() {
        return TextValidators.HEX_COLOR_TEXT_VALIDATOR.get(this.colorHex);
    }

    @Override
    protected void onCancel() {
        this.callback.accept(null);
    }

    @Override
    protected void onDone() {
        this.background.color = DrawableColor.of(this.colorHex);
        this.callback.accept(this.background);
    }

}
