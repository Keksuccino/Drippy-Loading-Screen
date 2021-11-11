//TODO übernehmen
package de.keksuccino.drippyloadingscreen.events;

import net.minecraft.client.gui.screens.Overlay;
import net.minecraftforge.eventbus.api.Event;

public class OverlayOpenEvent extends Event {

    public final Overlay overlay;

    public OverlayOpenEvent(Overlay overlay) {
        this.overlay = overlay;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }

}
