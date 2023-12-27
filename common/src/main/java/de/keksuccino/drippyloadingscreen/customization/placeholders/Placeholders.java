package de.keksuccino.drippyloadingscreen.customization.placeholders;

import de.keksuccino.fancymenu.customization.placeholder.PlaceholderRegistry;

public class Placeholders {

    public static void registerAll() {

        PlaceholderRegistry.register(new GameLoadingProgressPercentPlaceholder());

    }

}
