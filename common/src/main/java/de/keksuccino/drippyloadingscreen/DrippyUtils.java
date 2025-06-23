package de.keksuccino.drippyloadingscreen;

import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.drippyloadingscreen.mixin.MixinCache;
import de.keksuccino.fancymenu.customization.screen.identifier.ScreenIdentifierHandler;
import de.keksuccino.fancymenu.util.resource.resources.texture.ITexture;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class DrippyUtils {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final DrippyOverlayScreen DUMMY_DRIPPY_OVERLAY_SCREEN = new DrippyOverlayScreen();

    public static void waitForTexture(@Nullable ITexture t) {
        if (!DrippyLoadingScreen.getOptions().waitForTexturesInLoading.getValue()) return;
        if ((t != null) && !t.isLoadingFailed() && !t.isReady() && isDrippyRendering()) {
            t.waitForReady(5000);
        }
    }

    public static boolean isDrippyRendering() {
        return (Minecraft.getInstance().screen instanceof DrippyOverlayScreen);
    }

    public static boolean isDrippyIdentifier(@Nullable String identifier) {
        if (identifier == null) return false;
        return ScreenIdentifierHandler.isIdentifierOfScreen(identifier, DUMMY_DRIPPY_OVERLAY_SCREEN);
    }

    public static boolean fontsReady() {
        return MixinCache.fontsReady;
    }

}
