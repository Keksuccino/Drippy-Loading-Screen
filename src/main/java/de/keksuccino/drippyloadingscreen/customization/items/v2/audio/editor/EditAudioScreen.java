//TODO übernehmen
package de.keksuccino.drippyloadingscreen.customization.items.v2.audio.editor;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.auudio.audio.AudioClip;
import de.keksuccino.auudio.util.UrlUtils;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.UIBase;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.ChooseFilePopup;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.FHNotificationPopup;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.FHTextInputPopup;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.screens.ScrollableScreen;
import de.keksuccino.drippyloadingscreen.customization.items.v2.audio.AudioCustomizationItem;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.content.AdvancedTextField;
import de.keksuccino.konkrete.gui.content.scrollarea.ScrollAreaEntry;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.CharacterFilter;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.konkrete.math.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EditAudioScreen extends ScrollableScreen {

    protected AudioCustomizationItem.MenuAudio audio;
    protected AudioLayoutEditorElement parentElement;
    protected Consumer<AudioCustomizationItem.MenuAudio> callback;

    protected boolean isNewAudio = false;

    protected AdvancedButton doneButton;
    protected AdvancedButton cancelButton;

    public EditAudioScreen(Screen parent, AudioLayoutEditorElement parentElement, @Nullable AudioCustomizationItem.MenuAudio audioToEdit, Consumer<AudioCustomizationItem.MenuAudio> callback) {

        super(parent, Locals.localize("drippyloadingscreen.audio.add_or_edit"));

        this.parentElement = parentElement;
        this.audio = audioToEdit;
        if (this.audio == null) {
            this.isNewAudio = true;
            this.audio = new AudioCustomizationItem.MenuAudio(null, AudioClip.SoundType.EXTERNAL_LOCAL, (AudioCustomizationItem) this.parentElement.object);
        }
        this.callback = callback;

    }

    @Override
    public boolean isOverlayButtonHovered() {
        if (this.doneButton != null) {
            if (this.doneButton.isHoveredOrFocused()) {
                return true;
            }
        }
        if (this.cancelButton != null) {
            if (this.cancelButton.isHoveredOrFocused()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void init() {

        super.init();

        //Clear entry list on init
        List<ScrollAreaEntry> oldEntries = new ArrayList<>();
        oldEntries.addAll(this.scrollArea.getEntries());
        for (ScrollAreaEntry e : oldEntries) {
            this.scrollArea.removeEntry(e);
        }

        this.scrollArea.addEntry(new EmptySpaceEntry(this.scrollArea, 10));

        // SOURCE ------------------------------
        AdvancedButton chooseSourceButton = new AdvancedButton(0, 0, 200, 20, Locals.localize("drippyloadingscreen.audio.choosesource"), true, (press) -> {
            if (audio.soundType == AudioClip.SoundType.EXTERNAL_LOCAL) {
                ChooseFilePopup p = new ChooseFilePopup((call) -> {
                    if (call != null) {
                        File f = new File(call);
                        if (f.isFile() && f.getPath().toLowerCase().endsWith(".ogg")) {
                            audio.path = call;
                        } else {
                            FHNotificationPopup p2 = new FHNotificationPopup(300, new Color(0,0,0,0), 240, null, StringUtils.splitLines(Locals.localize("drippyloadingscreen.audio.choosesource.file.invalid"), "%n%"));
                            PopupHandler.displayPopup(p2);
                        }
                    }
                }, "ogg");
                if (audio.path != null) {
                    p.setText(audio.path);
                }
                PopupHandler.displayPopup(p);
            } else if (audio.soundType == AudioClip.SoundType.EXTERNAL_WEB) {
                FHTextInputPopup p = new FHTextInputPopup(new Color(0,0,0,0), Locals.localize("drippyloadingscreen.audio.choosesource.url"), null, 240, (call) -> {
                    if (call != null) {
                        if (UrlUtils.isValidUrl(call)) {
                            audio.path = call;
                        } else {
                            FHNotificationPopup p2 = new FHNotificationPopup(300, new Color(0,0,0,0), 240, null, StringUtils.splitLines(Locals.localize("drippyloadingscreen.audio.choosesource.url.invalid"), "%n%"));
                            PopupHandler.displayPopup(p2);
                        }
                    }
                });
                if (audio.path != null) {
                    p.setText(audio.path);
                }
                PopupHandler.displayPopup(p);
            }
        });
        this.scrollArea.addEntry(new ButtonEntry(this.scrollArea, chooseSourceButton));
        //--------------------------------------

        // INDEX --------------------------------
        TextEntry indexLabelEntry = new TextEntry(this.scrollArea, Locals.localize("drippyloadingscreen.audio.index"), true);
        indexLabelEntry.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.audio.index.desc"), "%n%"));
        this.scrollArea.addEntry(indexLabelEntry);
        AdvancedTextField indexTextField = new AdvancedTextField(Minecraft.getInstance().font, 0, 0, 200, 20, true, CharacterFilter.getIntegerCharacterFiler()) {
            @Override
            public void render(PoseStack p_93657_, int p_93658_, int p_93659_, float p_93660_) {
                super.render(p_93657_, p_93658_, p_93659_, p_93660_);
                if (MathUtils.isInteger(this.getValue().replace(" ", ""))) {
                    audio.index = Integer.parseInt(this.getValue().replace(" ", ""));
                }
            }
        };
        indexTextField.setMaxLength(10000);
        indexTextField.setValue("" + audio.index);
        TextFieldEntry indexFieldEntry = new TextFieldEntry(this.scrollArea, indexTextField);
        indexFieldEntry.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.audio.index.desc"), "%n%"));
        this.scrollArea.addEntry(indexFieldEntry);
        //--------------------------------------

        // VOLUME ------------------------------
        TextEntry volumeLabelEntry = new TextEntry(this.scrollArea, Locals.localize("drippyloadingscreen.audio.volume"), true);
        volumeLabelEntry.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.audio.volume.desc"), "%n%"));
        this.scrollArea.addEntry(volumeLabelEntry);
        AdvancedTextField volumeTextField = new AdvancedTextField(Minecraft.getInstance().font, 0, 0, 200, 20, true, CharacterFilter.getIntegerCharacterFiler()) {
            @Override
            public void render(PoseStack p_93657_, int p_93658_, int p_93659_, float p_93660_) {
                super.render(p_93657_, p_93658_, p_93659_, p_93660_);
                if (MathUtils.isInteger(this.getValue().replace(" ", ""))) {
                    audio.volume = Integer.parseInt(this.getValue().replace(" ", ""));
                }
            }
        };
        volumeTextField.setMaxLength(10000);
        volumeTextField.setValue("" + audio.volume);
        TextFieldEntry volumeFieldEntry = new TextFieldEntry(this.scrollArea, volumeTextField);
        volumeFieldEntry.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.audio.volume.desc"), "%n%"));
        this.scrollArea.addEntry(volumeFieldEntry);
        //--------------------------------------

        this.doneButton = new AdvancedButton(0, 0, 95, 20, Locals.localize("drippyloadingscreen.done"), true, (press) -> {
            this.onDone();
            Minecraft.getInstance().setScreen(this.parent);
        });
        UIBase.colorizeButton(this.doneButton);

        this.cancelButton = new AdvancedButton(0, 0, 95, 20, Locals.localize("drippyloadingscreen.cancel"), true, (press) -> {
            this.onCancel();
            Minecraft.getInstance().setScreen(this.parent);
        });
        UIBase.colorizeButton(this.cancelButton);

    }

    protected AudioCustomizationItem getItem() {
        return (AudioCustomizationItem) this.parentElement.object;
    }

    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {

        int xCenter = this.width / 2;

        super.render(matrix, mouseX, mouseY, partialTicks);

        //Save Button
        if (!this.isNewAudio) {
            this.doneButton.setX(xCenter - (this.doneButton.getWidth() / 2));
        } else {
            this.doneButton.setX(xCenter + 5);
        }
        this.doneButton.setY(this.height - 35);
        this.doneButton.render(matrix, mouseX, mouseY, partialTicks);

        if (this.isNewAudio) {
            this.cancelButton.setX(xCenter - this.cancelButton.getWidth() - 5);
            this.cancelButton.setY(this.height - 35);
            this.cancelButton.render(matrix, mouseX, mouseY, partialTicks);
        }

    }

    @Override
    public void onClose() {
        if (!PopupHandler.isPopupActive()) {
            if (this.isNewAudio) {
                this.onCancel();
            } else {
                this.onDone();
            }
            super.onClose();
        }
    }

    protected void onDone() {
        if (this.callback != null) {
            this.callback.accept(this.audio);
        }
    }

    protected void onCancel() {
        if (this.callback != null) {
            this.callback.accept(null);
        }
    }

    public AudioCustomizationItem.MenuAudio getAudio() {
        return this.audio;
    }

}
