package de.keksuccino.drippyloadingscreen.keybinding;

import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.konkrete.config.exceptions.InvalidValueException;
import de.keksuccino.konkrete.input.KeyboardHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class Keybinding {

	public static KeyMapping KeyToggleHelper;
	
	public static void init() {

		KeyToggleHelper = new KeyMapping("Toggle Drippy Customization Overlay | CTRL + ALT + ", 76, "Drippy Loading Screen");
		
		initGuiClickActions();

		FMLJavaModLoadingContext.get().getModEventBus().register(Keybinding.class);
		
	}

	@SubscribeEvent
	public static void onRegisterCommands(RegisterKeyMappingsEvent e) {

		e.register(KeyToggleHelper);

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
