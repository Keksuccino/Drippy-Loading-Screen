package de.keksuccino.drippyloadingscreen.events;

import de.keksuccino.konkrete.events.EventBase;
import net.minecraft.client.gui.screen.Overlay;

public class OverlayOpenEvent extends EventBase {

    public final Overlay overlay;

    public OverlayOpenEvent(Overlay overlay) {
        this.overlay = overlay;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }

}