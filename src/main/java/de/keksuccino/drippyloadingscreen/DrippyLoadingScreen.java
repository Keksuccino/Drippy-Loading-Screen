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

	public static final String VERSION = "2.1.1";
	public static final String MOD_LOADER = "forge";
	
	public static final File MOD_DIR = new File(FancyMenu.getGameDirectory(), "/config/drippyloadingscreen");

	public static final Logger LOGGER = LogManager.getLogger();
	
	public static Config config;

	public DrippyLoadingScreen() {

		if (FMLEnvironment.dist == Dist.CLIENT) {

			if (!MOD_DIR.exists()) {
				MOD_DIR.mkdirs();
			}
			
			updateConfig();

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
	
	public static void updateConfig() {
		
		try {
			
			config = new Config(MOD_DIR.getPath() + "/config.cfg");

			config.registerValue("allow_universal_layouts", false, "general");
			
			config.syncConfig();
			
			config.clearUnusedValues();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
