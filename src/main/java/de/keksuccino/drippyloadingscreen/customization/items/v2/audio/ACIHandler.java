//TODO übernehmen
package de.keksuccino.drippyloadingscreen.customization.items.v2.audio;

import de.keksuccino.auudio.audio.AudioClip;
import de.keksuccino.auudio.audio.VanillaSoundUtils;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.audio.AudioHandler;
import de.keksuccino.drippyloadingscreen.customization.helper.CustomizationHelperScreen;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.items.CustomizationItemBase;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import de.keksuccino.drippyloadingscreen.events.CustomizationSystemReloadedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundEngine;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.LoadingGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ACIHandler {

    private static final Logger LOGGER = LogManager.getLogger("drippyloadingscreen/ACIHandler");

    public static volatile boolean allowSoundEngineReload = false;
    public static volatile boolean earlySoungEngineReload = true;

    public static List<String> lastPlayingAudioSources = new ArrayList<>();

    public static Map<String, AudioCustomizationItem> currentNonLoopItems = new HashMap<>();
    public static Map<String, AudioCustomizationItem> startedOncePerSessionItems = new HashMap<>();

    protected static LoadingGui lastOverlay = null;
    protected static Screen lastScreen = null;
    protected static SoundHandler lastSoundManager = null;

    protected static List<Runnable> mainThreadTaskQueue = new ArrayList<>();

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new ACIHandler());
    }


    public static void onRenderOverlay(SplashCustomizationLayer handler) {

        if (DrippyLoadingScreen.config != null) {

            LoadingGui curOverlay = Minecraft.getInstance().getLoadingGui();
            SoundHandler curSoundManager = Minecraft.getInstance().getSoundHandler();
            SoundEngine engine = VanillaSoundUtils.getSoundEngine();
            if (engine != null) {
                if ((curSoundManager != null) && (lastSoundManager == null)) {
                    if (curOverlay != null) {
                        List<AudioClip> clips = getAuudioClips();
                        if (clips != null) {
                            AudioHandler.stopAll();
                            if (DrippyLoadingScreen.config.getOrDefault("custom_sound_engine_reloading", false)) {
                                allowSoundEngineReload = true;
                                engine.reload();
                                allowSoundEngineReload = false;
                                for (AudioClip c : clips) {
                                    c.prepare();
                                }
                                reloadItems();
                                LOGGER.info("Sounds reloaded early, because MC sound manager already loaded!");
                            }
                        }
                    }
                }
                lastSoundManager = curSoundManager;
            }

        }

    }

    public static void reloadItems() {
        lastPlayingAudioSources.clear();
        currentNonLoopItems.clear();
        SplashCustomizationLayer handler = SplashCustomizationLayer.getInstance();
        if (handler != null) {
            List<CustomizationItemBase> foreground = handler.foregroundElements;
            if (foreground != null) {
                for (CustomizationItemBase i : foreground) {
                    if (i instanceof AudioCustomizationItem) {
                        ((AudioCustomizationItem)i).reload();
                    }
                }
            }
            List<CustomizationItemBase> background = handler.backgroundElements;
            if (background != null) {
                for (CustomizationItemBase i : background) {
                    if (i instanceof AudioCustomizationItem) {
                        ((AudioCustomizationItem)i).reload();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onSystemReload(CustomizationSystemReloadedEvent e) {
        lastPlayingAudioSources.clear();
        currentNonLoopItems.clear();
        AudioHandler.stopAll();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {

        List<Runnable> tasks = new ArrayList<>();
        tasks.addAll(mainThreadTaskQueue);
        for (Runnable r : tasks) {
            r.run();
            mainThreadTaskQueue.remove(r);
        }

        LoadingGui curOverlay = Minecraft.getInstance().getLoadingGui();
        if ((curOverlay == null) && (lastOverlay != null)) {
            lastPlayingAudioSources.clear();
            currentNonLoopItems.clear();
            allowSoundEngineReload = true;
            fadeOutSounds(true);
        }
        if ((curOverlay != null) && (lastOverlay == null)) {
            allowSoundEngineReload = false;
        }
        lastOverlay = curOverlay;

        Screen curScreen = Minecraft.getInstance().currentScreen;
        if ((lastScreen instanceof CustomizationHelperScreen) && !(curScreen instanceof CustomizationHelperScreen)) {
            lastPlayingAudioSources.clear();
            currentNonLoopItems.clear();
            fadeOutSounds(false);
        }
        lastScreen = curScreen;

    }

    protected static List<AudioClip> getAuudioClips() {
        try {
            Field f = de.keksuccino.auudio.audio.AudioHandler.class.getDeclaredField("clips");
            f.setAccessible(true);
            return (List<AudioClip>) f.get(de.keksuccino.auudio.audio.AudioHandler.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static void fadeOutSounds(boolean reloadEngine) {
        new Thread(() -> {
            Map<AudioClip, Integer> volumes = new HashMap<>();
            for (AudioClip c : AudioHandler.getCachedAudios()) {
                volumes.put(c, c.getVolume());
            }
            int vol = 100;
            while (true) {
                for (AudioClip c : AudioHandler.getCachedAudios()) {
                    if (c.getVolume() >= vol) {
                        c.setVolume(vol);
                    }
                }
                vol = vol - 2;
                if (vol <= 0) {
                    if (reloadEngine && (DrippyLoadingScreen.config != null)) {
                        if (DrippyLoadingScreen.config.getOrDefault("custom_sound_engine_reloading", false)) {
                            allowSoundEngineReload = true;
                            SoundEngine engine = VanillaSoundUtils.getSoundEngine();
                            if (engine != null) {
                                mainThreadTaskQueue.add(() -> {
                                    engine.reload();
                                });
                            }
                        }
                    }
                    if (DrippyLoadingScreen.config == null) {
                        LOGGER.error("Error! Drippy config was null!");
                    }
                    break;
                }
                try {
                    Thread.sleep(5);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            AudioHandler.stopAll();
            for (AudioClip c : AudioHandler.getCachedAudios()) {
                c.setVolume(volumes.get(c));
            }
        }).start();
    }

    public static boolean canPlaySounds() {
        if ((Minecraft.getInstance().getLoadingGui() == null) && !(Minecraft.getInstance().currentScreen instanceof CustomizationHelperScreen)) {
            return false;
        }
        if (Minecraft.getInstance().currentScreen instanceof LayoutEditorScreen) {
            return false;
        }
        return true;
    }

}
