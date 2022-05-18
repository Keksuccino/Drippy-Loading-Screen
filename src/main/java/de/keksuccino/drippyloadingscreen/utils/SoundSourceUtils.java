package de.keksuccino.drippyloadingscreen.utils;

import net.minecraft.util.SoundCategory;

public class SoundSourceUtils {

    public static SoundCategory getSourceForName(String name) {
        for (SoundCategory s : SoundCategory.values()) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

}
