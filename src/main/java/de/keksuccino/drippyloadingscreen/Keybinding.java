package de.keksuccino.drippyloadingscreen;

import de.keksuccino.konkrete.input.KeyboardHandler;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class Keybinding {

    public static KeyBinding keyToggleOverlay;

    public static void init() {

        keyToggleOverlay = new KeyBinding("drippyloadingscreen.keybinding.toggle_overlay", InputMappings.getKey("key.keyboard.d").getValue(), "drippyloadingscreen.keybinding.category");
        ClientRegistry.registerKeyBinding(keyToggleOverlay);

        initGuiClickActions();

    }

    private static void initGuiClickActions() {

        //It's not possible in GUIs to check for keypresses via Keybinding.isPressed(), so I'm doing it on my own
        KeyboardHandler.addKeyPressedListener((c) -> {
            if ((keyToggleOverlay.getKey().getValue() == c.keycode) && KeyboardHandler.isCtrlPressed() && KeyboardHandler.isAltPressed()) {
                try {
                    if (DrippyLoadingScreen.config.getOrDefault("show_overlay", true)) {
                        DrippyLoadingScreen.config.setValue("show_overlay", false);
                    } else {
                        DrippyLoadingScreen.config.setValue("show_overlay", true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

}
