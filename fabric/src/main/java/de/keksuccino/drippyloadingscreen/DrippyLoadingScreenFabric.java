package de.keksuccino.drippyloadingscreen;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DrippyLoadingScreenFabric implements ModInitializer {

    private static final Logger LOGGER = LogManager.getLogger();

    public DrippyLoadingScreenFabric() {
    }
    
    @Override
    public void onInitialize() {

        DrippyLoadingScreen.init();

    }

}
