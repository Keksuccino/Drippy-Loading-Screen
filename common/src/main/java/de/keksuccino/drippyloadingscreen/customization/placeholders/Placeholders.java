package de.keksuccino.drippyloadingscreen.customization.placeholders;

import de.keksuccino.fancymenu.customization.placeholder.PlaceholderRegistry;

public class Placeholders {

    public static final GameLoadingProgressPercentPlaceholder GAME_LOADING_PROGRESS_PERCENT = new GameLoadingProgressPercentPlaceholder();

    public static void registerAll() {

        PlaceholderRegistry.register(GAME_LOADING_PROGRESS_PERCENT);

    }

}
