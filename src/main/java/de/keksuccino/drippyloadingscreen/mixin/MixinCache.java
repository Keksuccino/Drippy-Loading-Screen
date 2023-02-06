package de.keksuccino.drippyloadingscreen.mixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntSupplier;

public class MixinCache {

    public static IntSupplier originalLoadingScreenBackgroundColorSupplier = null;
    public static float cachedCurrentLoadingScreenProgress = 0.0F;
    //TODO übernehmen 2.1.1
    public static volatile List<Runnable> gameThreadRunnables = Collections.synchronizedList(new ArrayList<>());

}
