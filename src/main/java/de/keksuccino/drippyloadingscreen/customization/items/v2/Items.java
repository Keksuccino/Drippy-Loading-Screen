//TODO übernehmen
package de.keksuccino.drippyloadingscreen.customization.items.v2;

import de.keksuccino.drippyloadingscreen.api.item.v2.CustomizationItemRegistry;
import de.keksuccino.drippyloadingscreen.customization.items.v2.audio.ACIHandler;
import de.keksuccino.drippyloadingscreen.customization.items.v2.audio.AudioCustomizationItemContainer;

public class Items {

    public static void registerItems() {

        ACIHandler.init();
        CustomizationItemRegistry.registerItem(new AudioCustomizationItemContainer());

    }

}
