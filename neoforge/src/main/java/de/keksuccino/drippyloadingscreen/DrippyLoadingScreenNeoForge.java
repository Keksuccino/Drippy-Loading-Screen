package de.keksuccino.drippyloadingscreen;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod(DrippyLoadingScreen.MOD_ID)
public class DrippyLoadingScreenNeoForge {
    
    public DrippyLoadingScreenNeoForge(@NotNull IEventBus eventBus) {

        DrippyLoadingScreen.init();
        
    }

}