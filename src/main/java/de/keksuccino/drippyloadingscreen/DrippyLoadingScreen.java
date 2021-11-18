package de.keksuccino.drippyloadingscreen;

import de.keksuccino.drippyloadingscreen.api.PlaceholderTextValueRegistry;
import de.keksuccino.drippyloadingscreen.api.item.CustomizationItemRegistry;
import de.keksuccino.drippyloadingscreen.customization.CustomizationHandler;
import de.keksuccino.drippyloadingscreen.customization.helper.CustomizationHelper;
import de.keksuccino.drippyloadingscreen.customization.rendering.SimpleTextRenderer;
import de.keksuccino.drippyloadingscreen.customization.rendering.slideshow.SlideshowHandler;
import de.keksuccino.drippyloadingscreen.keybinding.Keybinding;
import de.keksuccino.drippyloadingscreen.logger.Logging;
import de.keksuccino.konkrete.Konkrete;
import de.keksuccino.konkrete.config.Config;
import de.keksuccino.konkrete.localization.Locals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.File;

public class DrippyLoadingScreen implements ModInitializer {
	
	public static final String VERSION = "1.3.1";
	
	public static final File HOME_DIR = new File("config/drippyloadingscreen");
	public static final File CUSTOMIZATION_DIR = new File(HOME_DIR.getPath() + "/customization");
	public static final File SLIDESHOW_DIR = new File(HOME_DIR.getPath() + "/slideshows");
	
	public static Config config;
	
	private static boolean fancymenuLoaded = false;
	private static boolean optifineLoaded = false;

	@Override
	public void onInitialize() {

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {

			if (!HOME_DIR.exists()) {
				HOME_DIR.mkdirs();
			}
			if (!CUSTOMIZATION_DIR.exists()) {
				CUSTOMIZATION_DIR.mkdirs();
			}
			if (!SLIDESHOW_DIR.exists()) {
				SLIDESHOW_DIR.mkdirs();
			}

			try {
				Class.forName("de.keksuccino.fancymenu.FancyMenu");
				System.out.println("[DRIPPY LOADING SCREEN] FancyMenu found!");
				fancymenuLoaded = true;
			} catch (Exception e) {}

			updateConfig();

			Logging.init();

			SlideshowHandler.init();

			CustomizationHandler.init();

			CustomizationHelper.init();

			SimpleTextRenderer.init();

			if (config.getOrDefault("enablekeybinds", true)) {
				Keybinding.init();
			}

//			Konkrete.getEventHandler().registerEventsFrom(new Test());

			Konkrete.addPostLoadingEvent("drippyloadingscreen", this::onClientSetup);

		} else {
			System.out.println("## WARNING ## 'Drippy Loading Screen' is a client mod and has no effect when loaded on a server!");
		}

	}
	
	private void onClientSetup() {
		try {

			initLocals();

        	try {
                Class.forName("optifine.Installer");
                optifineLoaded = true;
                System.out.println("[DRIPPY LOADING SCREEN] Optifine found!");
            }
            catch (ClassNotFoundException e) {}
	    	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static boolean isFancyMenuLoaded() {
		return fancymenuLoaded;
	}
	
	public static boolean isOptifineLoaded() {
		return optifineLoaded;
	}
	
	public static void updateConfig() {
		
		try {
			
			config = new Config(HOME_DIR.getPath() + "/config.cfg");
			
			//---------------------
			
			config.registerValue("printwarnings", true, "logging");
			
			config.registerValue("editordeleteconfirmation", true, "layouteditor");
			config.registerValue("showgrid", false, "layouteditor");
			config.registerValue("gridsize", 10, "layouteditor");
			
			config.registerValue("showcustomizationcontrols", true, "customization");
			config.registerValue("enablekeybinds", true, "customization");
			
			config.registerValue("uiscale", 1.0F, "ui");
			
			//---------------------
			
			config.syncConfig();
			
			config.clearUnusedValues();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private static void initLocals() {
		String baseresdir = "locals/";
		File f = new File("config/drippyloadingscreen/locals");
		if (!f.exists()) {
			f.mkdirs();
		}

		Locals.copyLocalsFileToDir(new Identifier("drippyloadingscreen", baseresdir + "en_us.local"), "en_us", f.getPath());
		
		Locals.getLocalsFromDir(f.getPath());
	}
	
	/**
	 * Gets the {@link PlaceholderTextValueRegistry} instance that allows you to register your own placeholder text values to the mod.<br>
	 * These can be used in many text-based HUD elements and items.
	 */
	public static PlaceholderTextValueRegistry getDynamicValueRegistry() {
		return PlaceholderTextValueRegistry.getInstance();
	}
	
	/**
	 * Gets the {@link CustomizationItemRegistry} instance that allows you to register your own <b>customization items</b>.<br>
	 * Customization items are all elements you can add to the HUD, like images, texts and more.<br><br>
	 * 
	 * <b>NOTE:</b><br>
	 * Internally, these are called "items" and vanilla elements are called "elements", but they are both called "elements" in the actual mod.<br>
	 * This was done to not forget that they are different things in the code, even if they look the same in the HUD.
	 */
	public static CustomizationItemRegistry getCustomizationItemRegistry() {
		return CustomizationItemRegistry.getInstance();
	}

}
