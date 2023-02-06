package de.keksuccino.drippyloadingscreen;

import com.mojang.blaze3d.platform.InputConstants;
import de.keksuccino.konkrete.input.KeyboardHandler;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;

public class Keybinding {

    public static KeyMapping keyToggleOverlay;

    public static void init() {

        keyToggleOverlay = new KeyMapping("drippyloadingscreen.keybinding.toggle_overlay", InputConstants.KEY_D, "drippyloadingscreen.keybinding.category");
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
