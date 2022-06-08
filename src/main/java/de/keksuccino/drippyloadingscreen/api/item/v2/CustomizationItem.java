//TODO übernehmen
package de.keksuccino.drippyloadingscreen.api.item.v2;

import de.keksuccino.drippyloadingscreen.customization.items.CustomizationItemBase;
import de.keksuccino.konkrete.properties.PropertiesSection;

public abstract class CustomizationItem extends CustomizationItemBase {

    public CustomizationItemContainer parentItemContainer;

    public CustomizationItem(CustomizationItemContainer parentContainer, PropertiesSection item) {
        super(item);
        this.parentItemContainer = parentContainer;
        if (this.value == null) {
            this.value = parentContainer.getDisplayName();
        }
        if (this.width == -1) {
            this.width = 10;
        }
        if (this.height == -1) {
            this.height = 10;
        }
    }

}
