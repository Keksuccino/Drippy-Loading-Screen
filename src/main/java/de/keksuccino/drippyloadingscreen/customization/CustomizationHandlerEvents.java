package de.keksuccino.drippyloadingscreen.customization;

import de.keksuccino.drippyloadingscreen.events.WindowResizedEvent;
import de.keksuccino.konkrete.Konkrete;
import de.keksuccino.konkrete.events.SubscribeEvent;
import de.keksuccino.konkrete.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;

public class CustomizationHandlerEvents {
	
	private int lastWindowWidth = -1;
	private int lastWindowHeight = -1;
	
	@SubscribeEvent
	public void onTick(ClientTickEvent.Post e) {

		/** WINDOW RESIZE EVENT HANDLER **/
		int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
		if ((lastWindowWidth != -1) && ((lastWindowWidth != width) || (lastWindowHeight != height))) {
			WindowResizedEvent event = new WindowResizedEvent(width, height);
			Konkrete.getEventHandler().callEventsFor(event);
		}
		this.lastWindowWidth = width;
		this.lastWindowHeight = height;
		
	}

}
