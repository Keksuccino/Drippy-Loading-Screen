package de.keksuccino.drippyloadingscreen;

import de.keksuccino.fancymenu.customization.animation.AnimationHandler;

public class FMAnimationUtils {

    private static boolean engineInitialized = false;

    public static void initAnimationEngine() {
        if (engineInitialized) return;
        AnimationHandler.discoverAndRegisterExternalAnimations();
        engineInitialized = true;
    }

}
