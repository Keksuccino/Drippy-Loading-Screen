package de.keksuccino.drippyloadingscreen.mixin.mixins.client;

import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayMenuHandler;
import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerBase;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerRegistry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.fml.client.EarlyLoaderGUI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EarlyLoaderGUI.class)
public class MixinEarlyLoaderGUI {

    private static final Screen DRIPPY_OVERLAY_SCREEN = new DrippyOverlayScreen();

    @ModifyArg(method = "renderMessages", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/EarlyLoaderGUI;renderMessage(Ljava/lang/String;[FIF)V"), index = 0, remap = false)
    private String overrideForgeLogMessageLine(String message) {
        MenuHandlerBase handler = MenuHandlerRegistry.getHandlerFor(DRIPPY_OVERLAY_SCREEN);
        if ((handler != null) && (handler instanceof DrippyOverlayMenuHandler)) {
            if (!((DrippyOverlayMenuHandler)handler).showForgeLog) {
                return "";
            }
        }
        return message;
    }

    @Inject(method = "renderMemoryInfo", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelForgeMemoryRendering(CallbackInfo info) {
        MenuHandlerBase handler = MenuHandlerRegistry.getHandlerFor(DRIPPY_OVERLAY_SCREEN);
        if ((handler != null) && (handler instanceof DrippyOverlayMenuHandler)) {
            if (!((DrippyOverlayMenuHandler)handler).showForgeMemory) {
                info.cancel();
            }
        }
    }

}
