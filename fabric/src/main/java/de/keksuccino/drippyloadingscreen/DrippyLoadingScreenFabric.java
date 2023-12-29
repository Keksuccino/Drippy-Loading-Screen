package de.keksuccino.drippyloadingscreen;

import de.keksuccino.drippyloadingscreen.platform.Services;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DrippyLoadingScreenFabric implements ModInitializer {

    private static final Logger LOGGER = LogManager.getLogger();

    public DrippyLoadingScreenFabric() {

        if (Services.PLATFORM.isOnClient()) {
            LOGGER.info("[DRIPPY LOADING SCREEN] Force-initializing FancyMenu's animation engine..");
            FMAnimationUtils.initAnimationEngine();
        }

    }
    
    @Override
    public void onInitialize() {

        DrippyLoadingScreen.init();

    }

}
