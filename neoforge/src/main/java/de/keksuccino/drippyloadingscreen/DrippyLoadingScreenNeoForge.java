package de.keksuccino.drippyloadingscreen;

import de.keksuccino.drippyloadingscreen.neoforge.EarlyLoadingEditorScreen;
import de.keksuccino.drippyloadingscreen.neoforge.FancyMenuReloadListenerBridge;
import de.keksuccino.fancymenu.util.event.acara.EventHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod(DrippyLoadingScreen.MOD_ID)
public class DrippyLoadingScreenNeoForge {
    
    public DrippyLoadingScreenNeoForge(@NotNull IEventBus eventBus) {

        DrippyLoadingScreen.init();

        eventBus.addListener(FancyMenuReloadListenerBridge::onAddClientReloadListeners);

        DrippyEvents.earlyLoadingEditorScreenSupplier = EarlyLoadingEditorScreen::new;

        EventHandler.INSTANCE.registerListenersOf(new TestNeoForge());
        
    }

}
