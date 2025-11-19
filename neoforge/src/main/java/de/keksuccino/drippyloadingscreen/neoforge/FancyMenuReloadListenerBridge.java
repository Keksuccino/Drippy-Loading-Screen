package de.keksuccino.drippyloadingscreen.neoforge;

import de.keksuccino.drippyloadingscreen.mixin.mixins.common.client.IMixinMinecraft;
import de.keksuccino.drippyloadingscreen.platform.Services;
import de.keksuccino.fancymenu.util.resource.ResourceHandlers;
import de.keksuccino.fancymenu.util.resource.preload.ResourcePreLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Compatibility bridge that registers FancyMenu's reload listener via NeoForge's event. */
public final class FancyMenuReloadListenerBridge {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation LISTENER_ID = ResourceLocation.fromNamespaceAndPath("drippyloadingscreen", "fancymenu_reload_listener");
    private static final PreparableReloadListener LISTENER = new SimplePreparableReloadListener<String>() {
        @Override
        protected String prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
            return "PREPARE RETURN VALUE";
        }

        @Override
        protected void apply(String state, ResourceManager resourceManager, ProfilerFiller profiler) {
            ResourceHandlers.reloadAll();
            ResourcePreLoader.preLoadAll(120000);
        }
    };

    private FancyMenuReloadListenerBridge() {}

    public static void onAddClientReloadListeners(AddClientReloadListenersEvent event) {
        if (!Services.PLATFORM.isModLoaded("fancymenu")) {
            return;
        }
        boolean alreadyRegistered;
        try {
            alreadyRegistered = IMixinMinecraft.isFancyMenuReloadListenerRegisteredDrippy();
        } catch (Throwable throwable) {
            LOGGER.warn("[DRIPPY] Failed to query FancyMenu reload listener flag, proceeding anyway", throwable);
            alreadyRegistered = false;
        }
        if (alreadyRegistered) {
            return;
        }
        LOGGER.info("[DRIPPY] Registering FancyMenu reload listener via AddClientReloadListenersEvent");
        event.addListener(LISTENER_ID, LISTENER);
        try {
            IMixinMinecraft.setFancyMenuReloadListenerRegisteredDrippy(true);
        } catch (Throwable throwable) {
            LOGGER.warn("[DRIPPY] Failed to update FancyMenu reload listener flag", throwable);
        }
    }
}
