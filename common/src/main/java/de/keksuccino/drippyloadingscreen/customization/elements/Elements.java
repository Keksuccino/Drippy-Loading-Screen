package de.keksuccino.drippyloadingscreen.customization.elements;

import de.keksuccino.drippyloadingscreen.customization.elements.vanillabar.VanillaBarElementBuilder;
import de.keksuccino.fancymenu.customization.element.ElementRegistry;

public class Elements {

    public static void registerAll() {

        ElementRegistry.register(new VanillaBarElementBuilder());

    }

}
