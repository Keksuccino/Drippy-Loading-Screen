package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.fancymenu.customization.customgui.CustomGuiBaseScreen;
import de.keksuccino.fancymenu.customization.overlay.CustomizationOverlayUI;
import de.keksuccino.fancymenu.util.rendering.ui.contextmenu.v2.ContextMenu;
import de.keksuccino.fancymenu.util.rendering.ui.menubar.v2.MenuBar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CustomizationOverlayUI.class)
public class MixinCustomizationOverlayUI {

    @Inject(method = "buildMenuBar", at = @At("RETURN"), remap = false)
    private static void onReturnBuildMenuBarDrippy(boolean expanded, CallbackInfoReturnable<MenuBar> info) {
        MenuBar bar = info.getReturnValue();
        if (bar.getEntry("screen") instanceof MenuBar.ContextMenuBarEntry screenTab) {
            ContextMenu contextMenu = screenTab.getContextMenu();
            if (contextMenu.getEntry("toggle_current_customization") instanceof ContextMenu.ValueCycleContextMenuEntry<?> toggleCustomizationEntry) {
                Screen screen = Minecraft.getInstance().screen;
                toggleCustomizationEntry.setIsActiveSupplier((menu, entry) -> !(screen instanceof CustomGuiBaseScreen) && !(screen instanceof DrippyOverlayScreen));
            }
        }
    }

}
