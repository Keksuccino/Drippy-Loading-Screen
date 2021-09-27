package de.keksuccino.drippyloadingscreen.keybinding;

import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.konkrete.config.exceptions.InvalidValueException;
import de.keksuccino.konkrete.input.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class Keybinding {

	public static KeyBinding KeyToggleHelper;
	
	public static void init() {

		//TODO übernehmen
		KeyToggleHelper = new KeyBinding("Toggle Drippy Customization Overlay | CTRL + ALT + ", 76, "Drippy Loading Screen");
		ClientRegistry.registerKeyBinding(KeyToggleHelper);
		
		initGuiClickActions();
		
	}
	
	private static void initGuiClickActions() {

		KeyboardHandler.addKeyPressedListener((c) -> {

			if ((KeyToggleHelper.getKey().getKeyCode() == c.keycode) && KeyboardHandler.isCtrlPressed() && KeyboardHandler.isAltPressed()) {
				try {
					if (DrippyLoadingScreen.config.getOrDefault("showcustomizationcontrols", true)) {
						DrippyLoadingScreen.config.setValue("showcustomizationcontrols", false);
					} else {
						DrippyLoadingScreen.config.setValue("showcustomizationcontrols", true);
					}
					DrippyLoadingScreen.config.syncConfig();
					if (Minecraft.getInstance().currentScreen != null) {
						Minecraft.getInstance().displayGuiScreen(Minecraft.getInstance().currentScreen);
					}
				} catch (InvalidValueException e) {
					e.printStackTrace();
				}
			}
			
		});
		
	}
	
}
