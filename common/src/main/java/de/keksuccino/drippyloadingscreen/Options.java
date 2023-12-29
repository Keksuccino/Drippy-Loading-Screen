package de.keksuccino.drippyloadingscreen;

import de.keksuccino.fancymenu.util.AbstractOptions;
import de.keksuccino.konkrete.config.Config;

public class Options extends AbstractOptions {

    protected final Config config = new Config(DrippyLoadingScreen.MOD_DIR.getAbsolutePath().replace("\\", "/") + "/options.txt");

    public final Option<Boolean> allowUniversalLayouts = new Option<>(config, "allow_universal_layouts", false, "general");
    public final Option<Boolean> earlyFadeOutElements = new Option<>(config, "early_fade_out_elements", true, "general");
    public final Option<Boolean> waitForTexturesInLoading = new Option<>(config, "wait_for_textures_in_loading", true, "general");

    public Options() {
        this.config.syncConfig();
        this.config.clearUnusedValues();
    }

}
