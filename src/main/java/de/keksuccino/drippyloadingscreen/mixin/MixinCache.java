package de.keksuccino.drippyloadingscreen.mixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntSupplier;

public class MixinCache {

    public static IntSupplier originalLoadingScreenBackgroundColorSupplier = null;
    public static float cachedCurrentLoadingScreenProgress = 0.0F;
    public static volatile List<Runnable> gameThreadRunnables = Collections.synchronizedList(new ArrayList<>());

}
