package de.keksuccino.drippyloadingscreen.customization.helper;

import de.keksuccino.konkrete.Konkrete;

public class CustomizationHelper {

	public static void init() {

		Konkrete.getEventHandler().registerEventsFrom(new CustomizationHelperEvents());
		
	}
	
}
