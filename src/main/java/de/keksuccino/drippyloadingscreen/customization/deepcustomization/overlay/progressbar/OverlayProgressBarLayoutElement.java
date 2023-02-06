package de.keksuccino.drippyloadingscreen.customization.deepcustomization.overlay.progressbar;

import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.LayoutEditorScreen;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.popup.FMTextInputPopup;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationElement;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationLayoutEditorElement;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.rendering.RenderUtils;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class OverlayProgressBarLayoutElement extends DeepCustomizationLayoutEditorElement {

    public OverlayProgressBarLayoutElement(@NotNull DeepCustomizationElement parentDeepCustomizationElement, @NotNull DeepCustomizationItem customizationItemInstance, @NotNull LayoutEditorScreen handler) {
        super(parentDeepCustomizationElement, customizationItemInstance, true, handler);
    }

    @Override
    public void init() {

        super.init();

        OverlayProgressBarItem i = (OverlayProgressBarItem) this.object;

        AdvancedButton setColorButton = new AdvancedButton(0, 0, 0, 0, I18n.get("drippyloadingscreen.deepcustomization.overlay.progress_bar.set_color"), true, (press) -> {
            FMTextInputPopup p = new FMTextInputPopup(new Color(0,0,0,0), I18n.get("drippyloadingscreen.deepcustomization.overlay.progress_bar.set_color"), null, 240, (call) -> {
                if (call != null) {
                    if (call.replace(" ", "").equals("") || call.replace(" ", "").equalsIgnoreCase("#RRGGBB")) {
                        if (i.hexColor != null) {
                            this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
                        }
                        i.hexColor = null;
                        i.hexColorString = "#RRGGBB";
                    } else {
                        if (!call.equalsIgnoreCase(i.hexColorString)) {
                            Color c = RenderUtils.getColorFromHexString(call);
                            if (c != null) {
                                this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
                                i.hexColorString = call;
                                i.hexColor = c;
                            }
                        }
                    }
                }
            });
            if (i.hexColorString != null) {
                p.setText(i.hexColorString);
            }
            PopupHandler.displayPopup(p);
        });
        setColorButton.setDescription(StringUtils.splitLines(I18n.get("drippyloadingscreen.deepcustomization.overlay.progress_bar.set_color.desc"), "\n"));
        this.rightclickMenu.addContent(setColorButton);

    }

    @Override
    public SimplePropertiesSection serializeItem() {

        OverlayProgressBarItem i = (OverlayProgressBarItem) this.object;
        SimplePropertiesSection sec = new SimplePropertiesSection();

        if (i.hexColor != null) {
            sec.addEntry("custom_color_hex", i.hexColorString);
        }

        return sec;

    }

}