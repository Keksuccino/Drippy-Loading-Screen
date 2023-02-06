package de.keksuccino.drippyloadingscreen.customization.deepcustomization.overlay.forge.forgememory;

import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.LayoutEditorScreen;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationElement;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationLayer;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationLayoutEditorElement;
import de.keksuccino.konkrete.properties.PropertiesSection;
import net.minecraft.client.resources.I18n;

public class OverlayForgeMemoryElement extends DeepCustomizationElement {

    public OverlayForgeMemoryElement(DeepCustomizationLayer parentLayer) {
        super("drippy_overlay_forge_memory", parentLayer);
    }

    @Override
    public DeepCustomizationItem constructDefaultItemInstance() {
        return new OverlayForgeMemoryItem(this, new PropertiesSection(""));
    }

    @Override
    public DeepCustomizationItem constructCustomizedItemInstance(PropertiesSection serializedItem) {
        return new OverlayForgeMemoryItem(this, serializedItem);
    }

    @Override
    public DeepCustomizationLayoutEditorElement constructEditorElementInstance(DeepCustomizationItem item, LayoutEditorScreen handler) {
        return new OverlayForgeMemoryLayoutElement(item.parentElement, item, handler);
    }

    @Override
    public String getDisplayName() {
        return I18n.get("drippyloadingscreen.deepcustomization.overlay.forge_memory.display_name");
    }

}