package de.keksuccino.drippyloadingscreen;

import com.mojang.blaze3d.platform.InputConstants;
import de.keksuccino.konkrete.input.KeyboardHandler;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class Keybinding {

    public static KeyMapping keyToggleOverlay;

    public static boolean initialized = false;

    public static void init() {

        FMLJavaModLoadingContext.get().getModEventBus().register(Keybinding.class);

    }

    @SubscribeEvent
    public static void registerKeyBinds(RegisterKeyMappingsEvent e) {

        if (!initialized) {
            keyToggleOverlay = new KeyMapping("drippyloadingscreen.keybinding.toggle_overlay", InputConstants.KEY_D, "drippyloadingscreen.keybinding.category");
            initGuiClickActions();
            initialized = true;
        }

        e.register(keyToggleOverlay);

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
