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
import de.keksuccino.konkrete.Konkrete;
import de.keksuccino.konkrete.events.SubscribeEvent;
import de.keksuccino.konkrete.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
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

    protected static Overlay lastOverlay = null;
    protected static Screen lastScreen = null;
    protected static SoundManager lastSoundManager = null;

    protected static List<Runnable> mainThreadTaskQueue = new ArrayList<>();

    public static void init() {
        Konkrete.getEventHandler().registerEventsFrom(new ACIHandler());
    }


    public static void onRenderOverlay(SplashCustomizationLayer handler) {

        if (DrippyLoadingScreen.config != null) {

            Overlay curOverlay = Minecraft.getInstance().getOverlay();
            SoundManager curSoundManager = Minecraft.getInstance().getSoundManager();
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
    public void onTick(ClientTickEvent.Pre e) {

        List<Runnable> tasks = new ArrayList<>();
        tasks.addAll(mainThreadTaskQueue);
        for (Runnable r : tasks) {
            r.run();
            mainThreadTaskQueue.remove(r);
        }

        Overlay curOverlay = Minecraft.getInstance().getOverlay();
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

        Screen curScreen = Minecraft.getInstance().screen;
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
                    if (reloadEngine) {
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
        if ((Minecraft.getInstance().getOverlay() == null) && !(Minecraft.getInstance().screen instanceof CustomizationHelperScreen)) {
            return false;
        }
        if (Minecraft.getInstance().screen instanceof LayoutEditorScreen) {
            return false;
        }
        return true;
    }

}
