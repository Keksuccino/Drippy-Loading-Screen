package de.keksuccino.drippyloadingscreen.earlywindow.window;

import net.neoforged.fml.earlydisplay.DisplayWindow;

public class DrippyEarlyWindowProvider extends DisplayWindow {

    public static final String PROVIDER_NAME = "drippy_early_window";

    @Override
    public String name() {
        return PROVIDER_NAME;
    }

}
