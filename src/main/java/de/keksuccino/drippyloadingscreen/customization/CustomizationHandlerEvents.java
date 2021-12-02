package de.keksuccino.drippyloadingscreen.customization;

import de.keksuccino.drippyloadingscreen.events.WindowResizedEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CustomizationHandlerEvents {
	
	private int lastWindowWidth = -1;
	private int lastWindowHeight = -1;
	
	@SubscribeEvent
	public void onTick(ClientTickEvent e) {
		
		/** WINDOW RESIZE EVENT HANDLER **/
		int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
		if ((lastWindowWidth != -1) && ((lastWindowWidth != width) || (lastWindowHeight != height))) {
			WindowResizedEvent event = new WindowResizedEvent(width, height);
			MinecraftForge.EVENT_BUS.post(event);
		}
		this.lastWindowWidth = width;
		this.lastWindowHeight = height;
		
	}

}
