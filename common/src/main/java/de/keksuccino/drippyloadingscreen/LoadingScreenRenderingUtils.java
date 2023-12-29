package de.keksuccino.drippyloadingscreen;

import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.fancymenu.util.resource.resources.texture.ITexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.jetbrains.annotations.Nullable;

public class LoadingScreenRenderingUtils {

    public static void waitForTexture(@Nullable ITexture t) {
        if (!DrippyLoadingScreen.getOptions().waitForTexturesInLoading.getValue()) return;
        if ((t != null) && !t.isLoadingFailed() && !t.isReady() && isDrippyRendering()) {
            t.waitForReady(5000);
        }
    }

    public static boolean isDrippyRendering() {
        return (Minecraft.getInstance().screen instanceof DrippyOverlayScreen) && (Minecraft.getInstance().getOverlay() instanceof LoadingOverlay);
    }

}
