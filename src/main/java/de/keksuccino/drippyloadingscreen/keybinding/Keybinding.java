package de.keksuccino.drippyloadingscreen.keybinding;

import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.konkrete.config.exceptions.InvalidValueException;
import de.keksuccino.konkrete.input.KeyboardHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientRegistry;

public class Keybinding {

	public static KeyMapping KeyToggleHelper;
	
	public static void init() {

		KeyToggleHelper = new KeyMapping("Toggle Drippy Customization Overlay | CTRL + ALT + ", 76, "Drippy Loading Screen");
		ClientRegistry.registerKeyBinding(KeyToggleHelper);
		
		initGuiClickActions();
		
	}
	
	private static void initGuiClickActions() {

		KeyboardHandler.addKeyPressedListener((c) -> {

			if ((KeyToggleHelper.getKey().getValue() == c.keycode) && KeyboardHandler.isCtrlPressed() && KeyboardHandler.isAltPressed()) {
				try {
					if (DrippyLoadingScreen.config.getOrDefault("showcustomizationcontrols", true)) {
						DrippyLoadingScreen.config.setValue("showcustomizationcontrols", false);
					} else {
						DrippyLoadingScreen.config.setValue("showcustomizationcontrols", true);
					}
					DrippyLoadingScreen.config.syncConfig();
					if (Minecraft.getInstance().screen != null) {
						Minecraft.getInstance().setScreen(Minecraft.getInstance().screen);
					}
				} catch (InvalidValueException e) {
					e.printStackTrace();
				}
			}
			
		});
		
	}
	
}
