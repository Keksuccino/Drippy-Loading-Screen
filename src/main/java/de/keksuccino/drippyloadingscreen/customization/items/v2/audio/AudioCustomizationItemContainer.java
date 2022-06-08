package de.keksuccino.drippyloadingscreen.customization.items.v2.audio;

import de.keksuccino.drippyloadingscreen.api.item.v2.CustomizationItem;
import de.keksuccino.drippyloadingscreen.api.item.v2.CustomizationItemContainer;
import de.keksuccino.drippyloadingscreen.api.item.v2.LayoutEditorElement;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.items.v2.audio.editor.AudioLayoutEditorElement;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.konkrete.properties.PropertiesSection;

public class AudioCustomizationItemContainer extends CustomizationItemContainer {

    public AudioCustomizationItemContainer() {
        super("fancymenu_extension:audio_item");
    }

    @Override
    public CustomizationItem constructDefaultItemInstance() {
        PropertiesSection sec = new PropertiesSection("dummy");
        sec.addEntry("channel", "master");
        AudioCustomizationItem i = new AudioCustomizationItem(this, sec);
        i.width = 50;
        i.height = 50;
        return i;
    }

    @Override
    public CustomizationItem constructCustomizedItemInstance(PropertiesSection propertiesSection) {
        return new AudioCustomizationItem(this, propertiesSection);
    }

    @Override
    public LayoutEditorElement constructEditorElementInstance(CustomizationItem customizationItem, LayoutEditorScreen layoutEditorScreen) {
        return new AudioLayoutEditorElement(this, customizationItem, true, layoutEditorScreen);
    }

    @Override
    public String getDisplayName() {
        return Locals.localize("drippyloadingscreen.audio.item");
    }

    @Override
    public String[] getDescription() {
        return StringUtils.splitLines(Locals.localize("drippyloadingscreen.audio.item.desc"), "%n%");
    }

}
