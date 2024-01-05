package de.keksuccino.drippyloadingscreen;

import java.io.File;

import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.konkrete.Konkrete;
import de.keksuccino.konkrete.config.Config;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("drippyloadingscreen")
public class DrippyLoadingScreen {

	public static final String VERSION = "2.2.6";
	public static final String MOD_LOADER = "forge";
	
	public static final File MOD_DIR = new File(FancyMenu.getGameDirectory(), "/config/drippyloadingscreen");

	public static final Logger LOGGER = LogManager.getLogger();
	
	public static Config config;

	public DrippyLoadingScreen() {

		if (FMLEnvironment.dist == Dist.CLIENT) {

			if (!MOD_DIR.exists()) {
				MOD_DIR.mkdirs();
			}

			initConfig();

			MinecraftForge.EVENT_BUS.register(new EventHandler());
			
			Konkrete.addPostLoadingEvent("drippyloadingscreen", this::onClientSetup);

		} else {
			LOGGER.warn("WARNING: 'Drippy Loading Screen' is a client mod and has no effect when loaded on a server!");
		}
		
	}
	
	private void onClientSetup() {
		try {

			//do stuff
	    	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	//TODO übernehmen
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

			config.registerValue("enable_early_loading", true, "forge", "If you disable this, Drippy will force-disable Forge's early loading screen. This could cause issues with some mods, so if you experience crashes, please turn it back on.");
			
			config.syncConfig();
			
			config.clearUnusedValues();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
