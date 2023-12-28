package de.keksuccino.drippyloadingscreen;

import java.io.File;
import de.keksuccino.fancymenu.util.event.acara.EventHandler;
import de.keksuccino.drippyloadingscreen.platform.Services;
import de.keksuccino.fancymenu.util.file.FileUtils;
import de.keksuccino.fancymenu.util.file.GameDirectoryUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class DrippyLoadingScreen {

	private static final Logger LOGGER = LogManager.getLogger();

	public static final String VERSION = "3.0.0";
	public static final String MOD_LOADER = Services.PLATFORM.getPlatformName();
	public static final String MOD_ID = "drippyloadingscreen";

	public static final File MOD_DIR = createDirectory(new File(GameDirectoryUtils.getGameDirectory(), "/config/drippyloadingscreen"));

	private static Options options;

	public DrippyLoadingScreen() {

		//Initializing animation engine early (only needed in Fabric)
		if (Services.PLATFORM.getPlatformName().equalsIgnoreCase("fabric") && Services.PLATFORM.isOnClient()) {
			LOGGER.info("[DRIPPY LOADING SCREEN] Force-initializing FancyMenu's animation engine..");
			FMAnimationUtils.initAnimationEngine();
		}

	}

	public static void init() {

		if (Services.PLATFORM.isOnClient()) {
			LOGGER.info("[DRIPPY LOADING SCREEN] Loading v" + VERSION + " in client-side mode on " + MOD_LOADER.toUpperCase() + "!");
		} else {
			LOGGER.info("[DRIPPY LOADING SCREEN] Loading v" + VERSION + " in server-side mode on " + MOD_LOADER.toUpperCase() + "!");
		}

		if (Services.PLATFORM.isOnClient()) {

			EventHandler.INSTANCE.registerListenersOf(new DrippyEvents());

		}

	}

	public static Options getOptions() {
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
