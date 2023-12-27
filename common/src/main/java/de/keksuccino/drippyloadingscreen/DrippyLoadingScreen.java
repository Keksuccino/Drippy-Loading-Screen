package de.keksuccino.drippyloadingscreen;

import java.io.File;
import de.keksuccino.fancymenu.customization.animation.AnimationHandler;
import de.keksuccino.fancymenu.util.event.acara.EventHandler;
import de.keksuccino.drippyloadingscreen.platform.Services;
import de.keksuccino.fancymenu.util.file.FileUtils;
import de.keksuccino.fancymenu.util.file.GameDirectoryUtils;
import de.keksuccino.fancymenu.util.file.type.types.FileTypes;
import de.keksuccino.konkrete.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class DrippyLoadingScreen {

	private static final Logger LOGGER = LogManager.getLogger();

	public static final String VERSION = "3.0.0";
	public static final String MOD_LOADER = Services.PLATFORM.getPlatformName();

	public static final File MOD_DIR = createDirectory(new File(GameDirectoryUtils.getGameDirectory(), "/config/drippyloadingscreen"));

	public static Config config;
	private static de.keksuccino.drippyloadingscreen.Options options;

	public DrippyLoadingScreen() {

		//Initializing animation engine early (only needed in Fabric)
		LOGGER.info("[DRIPPY LOADING SCREEN] Force-initializing FancyMenu's animation engine..");
		AnimationHandler.loadCustomAnimations();

	}

	public static void init() {

		if (Services.PLATFORM.isOnClient()) {
			LOGGER.info("[DRIPPY LOADING SCREEN] Loading v" + VERSION + " in client-side mode on " + MOD_LOADER.toUpperCase() + "!");
		} else {
			LOGGER.info("[DRIPPY LOADING SCREEN] Loading v" + VERSION + " in server-side mode on " + MOD_LOADER.toUpperCase() + "!");
		}

		FileTypes.registerAll();

		if (Services.PLATFORM.isOnClient()) {

			if (!MOD_DIR.exists()) {
				MOD_DIR.mkdirs();
			}

			initConfig();

			Konkrete.getEventHandler().registerEventsFrom(new EventHandler());

		}

	}

	public static void initConfig() {
		if (config == null) {
			updateConfig();
		}
	}

	public static void updateConfig() {

		try {

			config = new Config(MOD_DIR.getPath() + "/config.cfg");

			config.registerValue("allow_universal_layouts", false, "general");
			config.registerValue("early_fade_out_elements", true, "general");

			config.syncConfig();

			config.clearUnusedValues();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static de.keksuccino.drippyloadingscreen.Options getOptions() {
		if (options == null) {
			reloadOptions();
		}
		return options;
	}

	public static void reloadOptions() {
		options = new Options();
	}

	private static File createDirectory(@NotNull File directory) {
		return FileUtils.createDirectory(directory);
	}

}
