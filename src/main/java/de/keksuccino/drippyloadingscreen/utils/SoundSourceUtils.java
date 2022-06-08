//TODO übernehmen
package de.keksuccino.drippyloadingscreen.utils;

import net.minecraft.sounds.SoundSource;

public class SoundSourceUtils {

    public static SoundSource getSourceForName(String name) {
        for (SoundSource s : SoundSource.values()) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

}
