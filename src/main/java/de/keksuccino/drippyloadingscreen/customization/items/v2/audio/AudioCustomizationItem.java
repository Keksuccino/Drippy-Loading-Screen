//TODO übernehmen
package de.keksuccino.drippyloadingscreen.customization.items.v2.audio;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.auudio.audio.AudioClip;
import de.keksuccino.auudio.util.UrlUtils;
import de.keksuccino.drippyloadingscreen.api.item.v2.CustomizationItem;
import de.keksuccino.drippyloadingscreen.api.item.v2.CustomizationItemContainer;
import de.keksuccino.drippyloadingscreen.audio.AudioHandler;
import de.keksuccino.drippyloadingscreen.customization.CustomizationHandler;
import de.keksuccino.drippyloadingscreen.utils.SoundSourceUtils;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AudioCustomizationItem extends CustomizationItem {

    private static final ResourceLocation AUDIO_ELEMENT_TEXTURE = new ResourceLocation("drippyloadingscreen", "textures/audio_element.png");

    private static final Logger LOGGER = LogManager.getLogger("drippyloadingscreen/AudioCustomizationItem");

    public List<MenuAudio> audios = new ArrayList<>();
    public SoundCategory channel = SoundCategory.MASTER;
    public boolean loop = true;
    public boolean oncePerSession = false;

    public volatile boolean isLoadingNextAudio = false;
    public volatile MenuAudio currentAudio = null;
    public volatile List<String> alreadyPlayed = new ArrayList<>();

    public float cachedMasterChannelVolume = -1;
    public float cachedItemChannelVolume = -1;

    public AudioCustomizationItem(CustomizationItemContainer parentContainer, PropertiesSection item) {

        super(parentContainer, item);

        String channelString = item.getEntryValue("channel");
        if (channelString != null) {
            try {
                SoundCategory soundSource = SoundSourceUtils.getSourceForName(channelString);
                if (soundSource != null) {
                    this.channel = soundSource;
                } else {
                    LOGGER.warn("WARNING: Channel was NULL after parsing! Channel set to MASTER! (" + channelString + ")");
                }
            } catch (Exception e) {
                LOGGER.warn("WARNING: Unable to parse channel! Channel set to MASTER! (" + channelString + ")");
                e.printStackTrace();
            }
        } else {
            LOGGER.warn("WARNING: Channel is NULL! Channel set to MASTER!");
        }

        String loopString = item.getEntryValue("loop");
        if ((loopString != null) && loopString.equalsIgnoreCase("false")) {
            this.loop = false;
        }

        String oncePerSessionString = item.getEntryValue("once_per_session");
        if ((oncePerSessionString != null) && oncePerSessionString.equalsIgnoreCase("true")) {
            this.oncePerSession = true;
        }

        for (Map.Entry<String, String> m : item.getEntries().entrySet()) {
            if (m.getKey().startsWith("audio_source:")) {
                String audioIdentifier = m.getKey().split(":", 2)[1];
                if (m.getValue().contains(";")) {
                    String[] audioProps = m.getValue().split("[;]", -1);
                    if (audioProps.length >= 4) {

                        String sourceString = audioProps[0];
                        if (sourceString == null) {
                            LOGGER.error("ERROR: Source is NULL! (" + m.getValue() + ")");
                            continue;
                        }
                        String typeString = audioProps[1];
                        if (typeString == null) {
                            LOGGER.error("ERROR: Type is NULL! (" + m.getValue() + ")");
                            continue;
                        }
                        AudioClip.SoundType type;
                        try {
                            type = AudioClip.SoundType.valueOf(typeString);
                            if (type == null) {
                                LOGGER.error("ERROR: Sound type was NULL after parsing! (" + m.getValue() + ")");
                                continue;
                            }
                        } catch (Exception e) {
                            LOGGER.error("ERROR: Unable to parse sound type! (" + m.getValue() + ")");
                            e.printStackTrace();
                            continue;
                        }
                        String volString = audioProps[2];
                        int vol = 100;
                        if (MathUtils.isInteger(volString)) {
                            vol = Integer.parseInt(volString);
                        } else {
                            LOGGER.warn("WARNING: Unable to parse volume! Volume set to 100! (" + m.getValue() + ")");
                        }
                        String indexString = audioProps[3];
                        int index = 0;
                        if ((indexString != null) && MathUtils.isInteger(indexString)) {
                            index = Integer.parseInt(indexString);
                            if (index < 0) {
                                index = 0;
                                LOGGER.warn("WARNING: Tried to use negative index! Index corrected to 0! (" + m.getValue() + ")");
                            }
                        } else {
                            LOGGER.warn("WARNING: Unable to parse index! Index set to 0! (" + m.getValue() + ")");
                        }

                        MenuAudio ma = new MenuAudio(sourceString, type, this);
                        ma.volume = vol;
                        if ((audioIdentifier != null) && (audioIdentifier.length() >= 7)) {
                            ma.audioIdentifier = audioIdentifier;
                        }
                        ma.index = index;
                        this.audios.add(ma);

                    } else {
                        LOGGER.error("ERROR: Unable to parse audio source properties string! (" + m.getValue() + ")");
                    }
                } else {
                    LOGGER.error("ERROR: Invalid audio source properties string! (" + m.getValue() + ")");
                }
            }
        }

        //Handle once-per-session
        if (this.oncePerSession && ACIHandler.startedOncePerSessionItems.containsKey(this.actionId)) {
            this.alreadyPlayed = ACIHandler.startedOncePerSessionItems.get(this.actionId).alreadyPlayed;
        }
        if (this.oncePerSession) {
            this.loop = false;
            ACIHandler.startedOncePerSessionItems.put(this.actionId, this);
        }

        //Load alreadyPlayed cache for non-loop items, if it's the same screen
        if (!this.oncePerSession) {
            if (!this.loop && ACIHandler.currentNonLoopItems.containsKey(this.actionId)) {
                this.alreadyPlayed = ACIHandler.currentNonLoopItems.get(this.actionId).alreadyPlayed;
            }
            if (!this.loop) {
                ACIHandler.currentNonLoopItems.put(this.actionId, this);
            }
        }

        if (!isEditorActive() && (this.loop || (this.alreadyPlayed.size() < this.audios.size()))) {
            for (MenuAudio m : this.audios) {
                if (ACIHandler.lastPlayingAudioSources.contains(m.path)) {
                    this.startAsynchronous(m, false);
                    ACIHandler.lastPlayingAudioSources.remove(m.path);
//                    if (!ACIHandler.newLastPlayingAudioSources.contains(m.path)) {
//                        ACIHandler.newLastPlayingAudioSources.add(m.path);
//                    }
                    break;
                }
            }
        }

    }

    @Override
    public void render(MatrixStack poseStack) {

        if (this.shouldRender()) {

            if (isEditorActive()) {

                RenderSystem.enableBlend();
                Minecraft.getInstance().textureManager.bind(AUDIO_ELEMENT_TEXTURE);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                blit(poseStack, this.getPosX(), this.getPosY(), 1.0F, 1.0F, this.width, this.height, this.width, this.height);

            }

            this.tickAudio();

        }

    }

    public void tickAudio() {

//        if (ACIMuteHandler.isMuted(this.actionId) && (this.currentAudio != null)) {
//            this.currentAudio.getClip().stop();
//            ACIHandler.lastPlayingAudioSources.remove(this.currentAudio.path);
//            this.currentAudio = null;
//        }

        //Only tick when not in editor
        if (!isEditorActive()) {
            if (!audios.isEmpty()) {

                MenuAudio nextAudio = null;
                if (this.currentAudio == null) {
                    if (!this.isLoadingNextAudio) {
                        nextAudio = this.pickNextAudio();
                    }
                } else if (!this.currentAudio.isPlaying()) {
                    nextAudio = this.pickNextAudio();
                }
                if (nextAudio != null) {
                    this.startAsynchronous(nextAudio, true);
                }

                //Update volumes of audios in case MC volume should get changed while audios are playing
                float masterVol = Minecraft.getInstance().options.getSoundSourceVolume(SoundCategory.MASTER);
                float itemChannelVol = Minecraft.getInstance().options.getSoundSourceVolume(this.channel);
                if ((this.cachedMasterChannelVolume != masterVol) || (this.cachedItemChannelVolume != itemChannelVol)) {
                    for (MenuAudio a : this.audios) {
                        a.setVolume(a.volume);
                    }
                }
                this.cachedMasterChannelVolume = masterVol;
                this.cachedItemChannelVolume = itemChannelVol;

            }
        }

    }

    protected MenuAudio pickNextAudio() {

        if (!this.audios.isEmpty()) {

            if (this.alreadyPlayed.size() >= this.audios.size()) {
                if (this.loop) {
                    this.alreadyPlayed.clear();
                } else {
                    return null;
                }
            }

            List<MenuAudio> nextAudios = new ArrayList<>();
            if (this.currentAudio != null) {
                nextAudios = getRemainingAudiosWithIndex(this.currentAudio.index);
                if (nextAudios.isEmpty()) {
                    int nextIndex = getNextAudioIndex();
                    if (nextIndex == -1) {
                        return null;
                    } else {
                        nextAudios = getRemainingAudiosWithIndex(nextIndex);
                    }
                }
            } else {
                int nextIndex = getNextAudioIndex();
                if (nextIndex == -1) {
                    return null;
                } else {
                    nextAudios = getRemainingAudiosWithIndex(nextIndex);
                }
            }

            if (!nextAudios.isEmpty()) {
                return nextAudios.get(MathUtils.getRandomNumberInRange(0, nextAudios.size()-1));
            }

        }

        return null;

    }

    protected void startAsynchronous(MenuAudio audio, boolean restart) {

        if (!this.isLoadingNextAudio && ((this.currentAudio == null) || !this.currentAudio.isPlaying())) {

            if (this.currentAudio != null) {
                ACIHandler.lastPlayingAudioSources.remove(this.currentAudio.path);
            }

            this.isLoadingNextAudio = true;
            this.currentAudio = null;
            if (!this.alreadyPlayed.contains(audio.audioIdentifier)) {
                this.alreadyPlayed.add(audio.audioIdentifier);
            }

            if (audio.soundType == AudioClip.SoundType.EXTERNAL_LOCAL) {
                File f = new File(audio.path);
                if (!f.isFile() || !f.getPath().toLowerCase().endsWith(".ogg")) {
                    this.isLoadingNextAudio = false;
                    LOGGER.error("ERROR: Unable to start next audio! Invalid audio file! (" + audio.path + ")");
                    return;
                }
            } else if (audio.soundType == AudioClip.SoundType.EXTERNAL_WEB) {
                if (!UrlUtils.isValidUrl(audio.path)) {
                    this.isLoadingNextAudio = false;
                    LOGGER.error("ERROR: Unable to start next audio! Invalid audio URL! (" + audio.path + ")");
                    return;
                }
            }

            new Thread(() -> {

                AudioClip clip = audio.getClip();
                if (clip != null) {

                    audio.setVolume(audio.volume);
                    clip.setLooping(false);
                    if (restart) {
                        clip.stop();
                    }
                    clip.play();

                    long startTime = System.currentTimeMillis();
                    while (!clip.playing()) {
                        if (!ACIHandler.canPlaySounds()) {
                            this.isLoadingNextAudio = false;
                            return;
                        }
                        long now = System.currentTimeMillis();
                        if ((startTime + 10000) <= now) {
                            LOGGER.error("ERROR: Unable to start next audio! Timeout while starting clip! (" + audio.path + ")");
                            this.isLoadingNextAudio = false;
                            return;
                        }
                    }

                    if (!ACIHandler.lastPlayingAudioSources.contains(audio.path)) {
                        ACIHandler.lastPlayingAudioSources.add(audio.path);
                    }
                    this.currentAudio = audio;

                } else {
                    LOGGER.error("ERROR: Unable to start next audio! Clip is NULL! (" + audio.path + ")");
                }

                this.isLoadingNextAudio = false;

            }).start();

        }

    }

    protected List<MenuAudio> getRemainingAudiosWithIndex(int index) {
        List<MenuAudio> l = new ArrayList<>();
        for (MenuAudio a : this.audios) {
            if (!this.alreadyPlayed.contains(a.audioIdentifier)) {
                if (a.index == index) {
                    l.add(a);
                }
            }
        }
        return l;
    }

    protected List<Integer> getSortedAudioIndexes() {
        List<Integer> l = new ArrayList<>();
        for (MenuAudio a : this.audios) {
            if (!l.contains(a.index)) {
                l.add(a.index);
            }
        }
        Collections.sort(l);
        return l;
    }

    protected int getNextAudioIndex() {
        List<Integer> indexes = getSortedAudioIndexes();
        if (!indexes.isEmpty()) {
            if (this.currentAudio == null) {
                return indexes.get(0);
            } else {
                for (int i : indexes) {
                    if (i > this.currentAudio.index) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public void reload() {
        if (this.currentAudio != null) {
            this.currentAudio.clip.stop();
        }
        this.currentAudio = null;
    }

    public static class MenuAudio {

        public volatile String audioIdentifier = CustomizationHandler.generateRandomActionId();
        public volatile int index = 0;
        public volatile String path;
        public volatile AudioClip.SoundType soundType;
        public volatile AudioClip clip = null;
        public volatile int volume = 100;

        public final AudioCustomizationItem parent;

        public MenuAudio(@Nonnull String source, @Nonnull AudioClip.SoundType soundType, @Nonnull AudioCustomizationItem parent) {
            this.path = source;
            this.soundType = soundType;
            this.parent = parent;
        }

        @Nullable
        public AudioClip getClip() {
            if (this.clip == null) {
                this.clip = AudioHandler.getAudio(this.path, this.soundType);
            }
            return this.clip;
        }

        public boolean isPlaying() {
            if (this.clip == null) {
                return false;
            }
            return this.clip.playing();
        }

        public void setVolume(int volume) {

            this.volume = volume;

            float newVolFloat = this.volume;

            if (this.parent.channel != SoundCategory.MASTER) {
                float mcVol = Minecraft.getInstance().options.getSoundSourceVolume(this.parent.channel) * 100.0F;
                float clipVolOnePercent = ((float)this.volume) / 100.0F;
                newVolFloat = clipVolOnePercent * mcVol;
            }

            if (this.clip != null) {
                this.clip.setVolume((int)newVolFloat);
            }

        }

    }

}
