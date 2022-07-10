package de.keksuccino.drippyloadingscreen;

import java.io.File;

import de.keksuccino.drippyloadingscreen.customization.items.v2.Items;
import de.keksuccino.drippyloadingscreen.customization.items.visibilityrequirements.VisibilityRequirementHandler;
import de.keksuccino.drippyloadingscreen.customization.rendering.SimpleTextRenderer;

import de.keksuccino.drippyloadingscreen.api.PlaceholderTextValueRegistry;
import de.keksuccino.drippyloadingscreen.api.item.CustomizationItemRegistry;
import de.keksuccino.drippyloadingscreen.customization.CustomizationHandler;
import de.keksuccino.drippyloadingscreen.customization.helper.CustomizationHelper;
import de.keksuccino.drippyloadingscreen.customization.rendering.slideshow.SlideshowHandler;
import de.keksuccino.drippyloadingscreen.keybinding.Keybinding;
import de.keksuccino.drippyloadingscreen.logger.Logging;
import de.keksuccino.konkrete.Konkrete;
import de.keksuccino.konkrete.config.Config;
import de.keksuccino.konkrete.localization.Locals;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("drippyloadingscreen")
public class DrippyLoadingScreen {

	//TODO übernehmen
	public static final String VERSION = "1.6.4";
	public static final String MOD_LOADER = "forge";
	
	public static final File HOME_DIR = new File("config/drippyloadingscreen");
	public static final File CUSTOMIZATION_DIR = new File(HOME_DIR.getPath() + "/customization");
	public static final File SLIDESHOW_DIR = new File(HOME_DIR.getPath() + "/slideshows");

	public static final Logger LOGGER = LogManager.getLogger();
	
	public static Config config;
	
	private static boolean fancymenuLoaded = false;

	public DrippyLoadingScreen() {

		if (FMLEnvironment.dist == Dist.CLIENT) {

			if (!HOME_DIR.exists()) {
				HOME_DIR.mkdirs();
			}
			if (!CUSTOMIZATION_DIR.exists()) {
				CUSTOMIZATION_DIR.mkdirs();
			}
			if (!SLIDESHOW_DIR.exists()) {
				SLIDESHOW_DIR.mkdirs();
			}

			if (ModList.get().isLoaded("fancymenu")) {
				LOGGER.info("[DRIPPY LOADING SCREEN] FancyMenu detected!");
				fancymenuLoaded = true;
			}
			
			updateConfig();
			
			Logging.init();

			Items.registerItems();
			
			SlideshowHandler.init();

			CustomizationHandler.init();
			
			CustomizationHelper.init();

			VisibilityRequirementHandler.init();

			SimpleTextRenderer.init();

//			MinecraftForge.EVENT_BUS.register(new Test());
			
			if (config.getOrDefault("enablekeybinds", true)) {
        		Keybinding.init();
        	}
			
			Konkrete.addPostLoadingEvent("drippyloadingscreen", this::onClientSetup);

			if (isOptifineCompatibilityMode()) {
				LOGGER.info("[DRIPPY LOADING SCREEN] Optifine compatibility mode!");
			}

		} else {
			LOGGER.warn("WARNING: 'Drippy Loading Screen' is a client mod and has no effect when loaded on a server!");
		}
		
	}
	
	private void onClientSetup() {
		try {

			initLocals();
	    	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static boolean isFancyMenuLoaded() {
		return fancymenuLoaded;
	}

	@Deprecated
	public static boolean isOptifineLoaded() {
		return isOptifineCompatibilityMode();
	}

	public static boolean isOptifineCompatibilityMode() {
		return Konkrete.isOptifineLoaded;
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

			config.registerValue("custom_sound_engine_reloading", true, "loading_behaviour");
			
			config.registerValue("uiscale", 1.0F, "ui");
			
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
		
		Locals.copyLocalsFileToDir(new ResourceLocation("drippyloadingscreen", baseresdir + "en_us.local"), "en_us", f.getPath());
		
		Locals.getLocalsFromDir(f.getPath());
	}
	
	/**
	 * Gets the {@link PlaceholderTextValueRegistry} instance that allows you to register your own placeholder text values to the mod.<br>
	 * These can be used in many text-based elements and items.
	 */
	public static PlaceholderTextValueRegistry getDynamicValueRegistry() {
		return PlaceholderTextValueRegistry.getInstance();
	}
	
	/**
	 * Gets the {@link CustomizationItemRegistry} instance that allows you to register your own <b>customization items</b>.<br>
	 * Customization items are all elements you can add to the loading screen, like images, texts and more.<br><br>
	 * 
	 * <b>NOTE:</b><br>
	 * Internally, these are called "items" and vanilla elements are called "elements", but they are both called "elements" in the actual mod.<br>
	 * This was done to not forget that they are different things in the code, even if they look the same in the loading screen.
	 */
	public static CustomizationItemRegistry getCustomizationItemRegistry() {
		return CustomizationItemRegistry.getInstance();
	}
	
}
