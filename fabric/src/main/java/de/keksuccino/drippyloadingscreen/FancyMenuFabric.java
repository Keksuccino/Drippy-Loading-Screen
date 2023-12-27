package de.keksuccino.drippyloadingscreen;

import de.keksuccino.fancymenu.networking.Packets;
import de.keksuccino.drippyloadingscreen.platform.Services;
import net.fabricmc.api.ModInitializer;

public class FancyMenuFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {

        DrippyLoadingScreen.init();

        Packets.registerAll();

        if (Services.PLATFORM.isOnClient()) {
            FancyMenuFabricClientEvents.registerAll();
        }

        FancyMenuFabricServerEvents.registerAll();

    }

}
