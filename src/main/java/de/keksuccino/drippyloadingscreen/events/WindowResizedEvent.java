package de.keksuccino.drippyloadingscreen.events;

import de.keksuccino.konkrete.events.EventBase;

public class WindowResizedEvent extends EventBase {
	
	private int width;
	private int height;
	
	public WindowResizedEvent(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	@Override
	public boolean isCancelable() {
		return false;
	}
	
	public int getScaledWidth() {
		return this.width;
	}
	
	public int getScaledHeight() {
		return this.height;
	}

}
