package de.keksuccino.drippyloadingscreen;

import de.keksuccino.fancymenu.util.AbstractOptions;
import de.keksuccino.konkrete.config.Config;

public class Options extends AbstractOptions {

    protected final Config config = new Config(DrippyLoadingScreen.MOD_DIR.getAbsolutePath().replace("\\", "/") + "/options.txt");

    public final Option<Boolean> allowUniversalLayouts = new Option<>(config, "allow_universal_layouts", false, "general");
    public final Option<Boolean> earlyFadeOutElements = new Option<>(config, "early_fade_out_elements", true, "general");
    public final Option<Boolean> waitForTexturesInLoading = new Option<>(config, "wait_for_textures_in_loading", true, "general");
    public final Option<Boolean> fadeInOutLoadingScreen = new Option<>(config, "fade_out_loading_screen", true, "general");

    public final Option<String> earlyLoadingBackgroundTexturePath = new Option<>(config, "early_loading_background_texture_path", "/config/fancymenu/assets/some_image.png", "early_loading"); // only supports PNG; falls back to Vanilla-like color background if image not found (respects Minecraft's dark background setting like NeoForge)
    public final Option<Boolean> earlyLoadingBackgroundPreserveAspectRatio = new Option<>(config, "early_loading_background_preserve_aspect_ratio", true, "early_loading");
    public final Option<String> earlyLoadingLogoTexturePath = new Option<>(config, "early_loading_logo_texture_path", "/config/fancymenu/assets/some_image.png", "early_loading"); // only supports PNG; falls back to a solid rendered block of color if image not found (color = red)
    public final Option<Integer> earlyLoadingLogoWidth = new Option<>(config, "early_loading_logo_width", 500, "early_loading");
    public final Option<Integer> earlyLoadingLogoHeight = new Option<>(config, "early_loading_logo_height", 100, "early_loading");
    public final Option<Integer> earlyLoadingLogoPositionOffsetX = new Option<>(config, "early_loading_logo_position_offset_x", 0, "early_loading"); // logo is rendered perfectly centered on the X axis by default
    public final Option<Integer> earlyLoadingLogoPositionOffsetY = new Option<>(config, "early_loading_logo_position_offset_y", 0, "early_loading"); // logo is rendered pretty much centered on the Y axis by default, slightly higher than absolute center
    public final Option<String> earlyLoadingWindowTitle = new Option<>(config, "early_loading_window_title", "Minecraft", "early_loading"); // falls back to "Minecraft" if empty/blank value is set
    public final Option<String> earlyLoadingBarBackgroundTexturePath = new Option<>(config, "early_loading_bar_background_texture_path", "/config/fancymenu/assets/some_bar_background_image.png", "early_loading"); // only supports PNG; bar falls back to Vanilla-like dynamically rendered background if image not found
    public final Option<String> earlyLoadingBarProgressTexturePath = new Option<>(config, "early_loading_bar_background_texture_path", "/config/fancymenu/assets/some_bar_progress_image.png", "early_loading"); // only supports PNG; bar falls back to Vanilla-like dynamically rendered progress if image not found
    public final Option<Integer> earlyLoadingBarWidth = new Option<>(config, "early_loading_bar_width", 400, "early_loading");
    public final Option<Integer> earlyLoadingBarHeight = new Option<>(config, "early_loading_bar_height", 40, "early_loading");
    public final Option<Integer> earlyLoadingBarPositionOffsetX = new Option<>(config, "early_loading_bar_position_offset_x", 0, "early_loading"); // progress bar is rendered below logo by default
    public final Option<Integer> earlyLoadingBarPositionOffsetY = new Option<>(config, "early_loading_bar_position_offset_y", 0, "early_loading"); // progress bar is rendered below logo by default

    public Options() {
        this.config.syncConfig();
        this.config.clearUnusedValues();
    }

}
