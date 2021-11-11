package de.keksuccino.drippyloadingscreen.events;

import net.minecraft.client.gui.LoadingGui;
import net.minecraftforge.eventbus.api.Event;

public class OverlayOpenEvent extends Event {

    public final LoadingGui overlay;

    public OverlayOpenEvent(LoadingGui overlay) {
        this.overlay = overlay;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }

}