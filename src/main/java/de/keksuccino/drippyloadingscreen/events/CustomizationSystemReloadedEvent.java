package de.keksuccino.drippyloadingscreen.events;

import de.keksuccino.konkrete.events.EventBase;

public class CustomizationSystemReloadedEvent extends EventBase {
	
	@Override
	public boolean isCancelable() {
		return false;
	}

}
