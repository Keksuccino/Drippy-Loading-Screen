package de.keksuccino.drippyloadingscreen.customization.backgrounds;

import de.keksuccino.drippyloadingscreen.customization.backgrounds.color.ColorMenuBackgroundBuilder;
import de.keksuccino.fancymenu.customization.background.MenuBackgroundRegistry;

public class Backgrounds {

    public static final ColorMenuBackgroundBuilder COLOR_MENU_BACKGROUND = new ColorMenuBackgroundBuilder();

    public static void registerAll() {

        MenuBackgroundRegistry.register(COLOR_MENU_BACKGROUND);

    }

}
