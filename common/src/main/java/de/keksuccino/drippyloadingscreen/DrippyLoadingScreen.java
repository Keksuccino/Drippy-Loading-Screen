package de.keksuccino.drippyloadingscreen;

import java.io.File;
import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.drippyloadingscreen.customization.backgrounds.Backgrounds;
import de.keksuccino.drippyloadingscreen.customization.elements.Elements;
import de.keksuccino.drippyloadingscreen.customization.placeholders.Placeholders;
import de.keksuccino.fancymenu.customization.ScreenCustomization;
import de.keksuccino.fancymenu.customization.screen.identifier.UniversalScreenIdentifierRegistry;
import de.keksuccino.fancymenu.util.event.acara.EventHandler;
import de.keksuccino.drippyloadingscreen.platform.Services;
import de.keksuccino.fancymenu.util.file.FileUtils;
import de.keksuccino.fancymenu.util.file.GameDirectoryUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class DrippyLoadingScreen {

	//TODO Player Entities in loading screen layouts deaktivieren (und andere nicht funktionierende element types)

	//TODO Handling für allowUniversalLayouts adden

	//TODO Legacy Drippy elements (progress bars, vanilla customizations, menu background) konvertieren





	private static final Logger LOGGER = LogManager.getLogger();

	public static final String VERSION = "3.0.0";
	public static final String MOD_LOADER = Services.PLATFORM.getPlatformName();
	public static final String MOD_ID = "drippyloadingscreen";

	public static final File MOD_DIR = createDirectory(new File(GameDirectoryUtils.getGameDirectory(), "/config/drippyloadingscreen"));

	private static Options options;

	private static boolean initialized = false;

	public static void init() {

		if (initialized) return;
		initialized = true;

		if (Services.PLATFORM.isOnClient()) {
			LOGGER.info("[DRIPPY LOADING SCREEN] Loading v" + VERSION + " in client-side mode on " + MOD_LOADER.toUpperCase() + "!");
		} else {
			LOGGER.info("[DRIPPY LOADING SCREEN] Loading v" + VERSION + " in server-side mode on " + MOD_LOADER.toUpperCase() + "!");
		}

		if (Services.PLATFORM.isOnClient()) {

			EventHandler.INSTANCE.registerListenersOf(new DrippyEvents());

			//Register universal identifier for Drippy screen
			UniversalScreenIdentifierRegistry.register("drippy_loading_overlay", DrippyOverlayScreen.class.getName());

			//Disable customization for all background config screens
			ScreenCustomization.addScreenBlacklistRule(s -> s.startsWith("de.keksuccino.drippyloadingscreen.customization.backgrounds."));

			//Register custom backgrounds
			Backgrounds.registerAll();

			//Register custom placeholders
			Placeholders.registerAll();

			//Register custom element types
			Elements.registerAll();

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
