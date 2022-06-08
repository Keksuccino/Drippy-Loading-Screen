//TODO übernehmen
package de.keksuccino.drippyloadingscreen.customization.items.v2.audio.editor;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.api.item.v2.CustomizationItem;
import de.keksuccino.drippyloadingscreen.api.item.v2.CustomizationItemContainer;
import de.keksuccino.drippyloadingscreen.api.item.v2.LayoutEditorElement;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.items.v2.audio.AudioCustomizationItem;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;

public class AudioLayoutEditorElement extends LayoutEditorElement {

    public AudioLayoutEditorElement(CustomizationItemContainer parentContainer, CustomizationItem customizationItemInstance, boolean destroyable, LayoutEditorScreen handler) {
        super(parentContainer, customizationItemInstance, destroyable, handler);
    }

    @Override
    public void init() {

        this.delayable = false;

        super.init();

        AudioCustomizationItem i = (AudioCustomizationItem) this.object;

        AdvancedButton manageAudiosButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.audio.manage"), (press) -> {
            ManageAudiosScreen s = new ManageAudiosScreen(this.handler, this);
            Minecraft.getInstance().setScreen(s);
            this.rightclickMenu.closeMenu();
        });
        manageAudiosButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.audio.manage.desc"), "%n%"));
        this.rightclickMenu.addContent(manageAudiosButton);

        AdvancedButton channelButton = new AdvancedButton(0, 0, 0, 0, "", (press) -> {
            if (i.channel == SoundSource.MASTER) {
                i.channel = SoundSource.MUSIC;
            } else if (i.channel == SoundSource.MUSIC) {
                i.channel = SoundSource.RECORDS;
            } else if (i.channel == SoundSource.RECORDS) {
                i.channel = SoundSource.WEATHER;
            } else if (i.channel == SoundSource.WEATHER) {
                i.channel = SoundSource.BLOCKS;
            } else if (i.channel == SoundSource.BLOCKS) {
                i.channel = SoundSource.HOSTILE;
            } else if (i.channel == SoundSource.HOSTILE) {
                i.channel = SoundSource.NEUTRAL;
            } else if (i.channel == SoundSource.NEUTRAL) {
                i.channel = SoundSource.PLAYERS;
            } else if (i.channel == SoundSource.PLAYERS) {
                i.channel = SoundSource.AMBIENT;
            } else if (i.channel == SoundSource.AMBIENT) {
                i.channel = SoundSource.VOICE;
            } else if (i.channel == SoundSource.VOICE) {
                i.channel = SoundSource.MASTER;
            }
        }) {
            @Override
            public void render(PoseStack p_93657_, int p_93658_, int p_93659_, float p_93660_) {
                this.setMessage(Locals.localize("drippyloadingscreen.audio.channel", i.channel.getName().toUpperCase()));
                super.render(p_93657_, p_93658_, p_93659_, p_93660_);
            }
        };
        this.rightclickMenu.addContent(channelButton);

        AdvancedButton loopButton = new AdvancedButton(0, 0, 0, 0, "", (press) -> {
            if (i.loop) {
                i.loop = false;
            } else {
                i.loop = true;
            }
        }) {
            @Override
            public void render(PoseStack p_93657_, int p_93658_, int p_93659_, float p_93660_) {
                if (i.oncePerSession) {
                    this.active = false;
                } else {
                    this.active = true;
                }
                if (i.loop) {
                    this.setMessage(Locals.localize("drippyloadingscreen.audio.loop.on"));
                } else {
                    this.setMessage(Locals.localize("drippyloadingscreen.audio.loop.off"));
                }
                super.render(p_93657_, p_93658_, p_93659_, p_93660_);
            }
        };
        this.rightclickMenu.addContent(loopButton);

        AdvancedButton oncePerSessionButton = new AdvancedButton(0, 0, 0, 0, "", (press) -> {
            if (i.oncePerSession) {
                i.oncePerSession = false;
            } else {
                i.oncePerSession = true;
            }
        }) {
            @Override
            public void render(PoseStack p_93657_, int p_93658_, int p_93659_, float p_93660_) {
                if (i.oncePerSession) {
                    this.setMessage(Locals.localize("drippyloadingscreen.audio.once_per_session.on"));
                } else {
                    this.setMessage(Locals.localize("drippyloadingscreen.audio.once_per_session.off"));
                }
                super.render(p_93657_, p_93658_, p_93659_, p_93660_);
            }
        };
        oncePerSessionButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.audio.once_per_session.desc"), "%n%"));
        this.rightclickMenu.addContent(oncePerSessionButton);

    }

    @Override
    public SimplePropertiesSection serializeItem() {

        AudioCustomizationItem i = (AudioCustomizationItem) this.object;

        SimplePropertiesSection sec = new SimplePropertiesSection();
        sec.addEntry("channel", i.channel.getName());
        sec.addEntry("loop", "" + i.loop);
        sec.addEntry("once_per_session", "" + i.oncePerSession);
        for (AudioCustomizationItem.MenuAudio m : i.audios) {
            if ((m.path != null) && (m.soundType != null)) {
                sec.addEntry("audio_source:" + m.audioIdentifier, m.path + ";" + m.soundType.name() + ";" + m.volume + ";" + m.index);
            }
        }

        return sec;

    }

}
