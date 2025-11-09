package de.keksuccino.drippyloadingscreen;

import de.keksuccino.drippyloadingscreen.neoforge.EarlyLoadingPreviewScreen;
import de.keksuccino.fancymenu.events.screen.InitOrResizeScreenEvent;
import de.keksuccino.fancymenu.util.event.acara.EventListener;
import de.keksuccino.fancymenu.util.rendering.ui.widget.button.ExtendedButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestNeoForge {

    private static final Logger LOGGER = LogManager.getLogger();


    @EventListener
    public void onRenderPost(InitOrResizeScreenEvent.Post e) {

        if (e.getScreen() instanceof TitleScreen) {

            e.addRenderableWidget(new ExtendedButton(60, 60, 200, 20, "Open Early Loading Preview", button -> {
                Minecraft.getInstance().setScreen(new EarlyLoadingPreviewScreen());
            }));

        }

    }

}
