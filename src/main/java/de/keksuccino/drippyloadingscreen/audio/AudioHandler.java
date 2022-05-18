//TODO übernehmen
package de.keksuccino.drippyloadingscreen.audio;

import de.keksuccino.auudio.audio.AudioClip;
import de.keksuccino.auudio.util.UrlUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioHandler {

    protected static Map<String, AudioClip> audios = new HashMap<>();

    protected static List<AudioClip> unfinishedAudioCache = new ArrayList<>();

    @Nullable
    public static AudioClip getAudio(String source, AudioClip.SoundType soundType) {
        if (soundType == AudioClip.SoundType.EXTERNAL_LOCAL) {
            File f = new File(source);
            if (!f.isFile() || !f.getPath().toLowerCase().endsWith(".ogg")) {
                return null;
            }
        } else if (soundType == AudioClip.SoundType.EXTERNAL_WEB) {
            if (!UrlUtils.isValidUrl(source)) {
                return null;
            }
        } else {
            return null;
        }
        if (!audios.containsKey(source)) {
            AudioClip c = null;
            try {
                c = AudioClip.buildExternalClip(source, soundType);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (c != null) {
                audios.put(source, c);
            } else {
                return null;
            }
        }
        return audios.get(source);
    }

    public static AudioClip getAudioIfRegistered(String source) {
        return audios.get(source);
    }

    public static boolean containsAudio(String source) {
        return audios.containsKey(source);
    }

    public static void removeAudioFromCache(String source) {
        AudioClip c = audios.remove(source);
        if (c != null) {
            c.destroy();
        }
    }

    public static List<AudioClip> getCachedAudios() {
        List<AudioClip> l = new ArrayList<>();
        l.addAll(audios.values());
        return l;
    }

    public static void pauseAll() {
        for (AudioClip c : audios.values()) {
            if (c.playing()) {
                if (!unfinishedAudioCache.contains(c)) {
                    unfinishedAudioCache.add(c);
                    c.pause();
                }
            }
        }
    }

    public static void stopAll() {
        for (AudioClip c : audios.values()) {
            c.stop();
        }
        unfinishedAudioCache.clear();
    }

    public static void resumeUnfinishedAudios() {
        for (AudioClip c : unfinishedAudioCache) {
            try {
                c.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        unfinishedAudioCache.clear();
    }

    public static void clearUnfinishedAudioCache() {
        unfinishedAudioCache.clear();
    }

}
