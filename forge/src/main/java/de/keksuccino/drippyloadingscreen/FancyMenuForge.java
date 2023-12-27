package de.keksuccino.drippyloadingscreen;

import de.keksuccino.fancymenu.networking.Packets;
import de.keksuccino.drippyloadingscreen.platform.Services;
import net.minecraftforge.fml.common.Mod;

@Mod(DrippyLoadingScreen.MOD_ID)
public class FancyMenuForge {
    
    public FancyMenuForge() {

        DrippyLoadingScreen.init();

        Packets.registerAll();

        if (Services.PLATFORM.isOnClient()) {
            FancyMenuForgeClientEvents.registerAll();
        }

        FancyMenuForgeServerEvents.registerAll();
        
    }

}