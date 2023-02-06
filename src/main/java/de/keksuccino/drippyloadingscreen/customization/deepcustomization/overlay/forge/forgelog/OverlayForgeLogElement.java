package de.keksuccino.drippyloadingscreen.customization.deepcustomization.overlay.forge.forgelog;

import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.LayoutEditorScreen;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationElement;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationLayer;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationLayoutEditorElement;
import de.keksuccino.konkrete.properties.PropertiesSection;
import net.minecraft.client.resources.I18n;

public class OverlayForgeLogElement extends DeepCustomizationElement {

    public OverlayForgeLogElement(DeepCustomizationLayer parentLayer) {
        super("drippy_overlay_forge_log", parentLayer);
    }

    @Override
    public DeepCustomizationItem constructDefaultItemInstance() {
        return new OverlayForgeLogItem(this, new PropertiesSection(""));
    }

    @Override
    public DeepCustomizationItem constructCustomizedItemInstance(PropertiesSection serializedItem) {
        return new OverlayForgeLogItem(this, serializedItem);
    }

    @Override
    public DeepCustomizationLayoutEditorElement constructEditorElementInstance(DeepCustomizationItem item, LayoutEditorScreen handler) {
        return new OverlayForgeLogLayoutElement(item.parentElement, item, handler);
    }

    @Override
    public String getDisplayName() {
        return I18n.get("drippyloadingscreen.deepcustomization.overlay.forge_log.display_name");
    }

}