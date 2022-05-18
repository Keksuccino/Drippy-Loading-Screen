package de.keksuccino.drippyloadingscreen.customization.items.v2.audio.editor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.UIBase;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.FHYesNoPopup;
import de.keksuccino.drippyloadingscreen.customization.items.v2.audio.AudioCustomizationItem;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

import java.awt.*;

public class ManageAudiosScreen extends Screen {

    protected static final Color BACKGROUND_COLOR = new Color(38, 38, 38);

    protected LayoutEditorScreen handler;
    protected AudioLayoutEditorElement element;

    protected AdvancedButton addAudioButton;
    protected AdvancedButton removeAudioButton;
    protected AdvancedButton editAudioButton;
    protected AdvancedButton backButton;

    public ManageAudiosScreen(LayoutEditorScreen handler, AudioLayoutEditorElement element) {

        super(new StringTextComponent(""));

        this.handler = handler;
        this.element = element;

        this.addAudioButton = new AdvancedButton(0, 0, 200, 20, Locals.localize("drippyloadingscreen.audio.add"), true, (press) -> {
            EditAudioScreen s = new EditAudioScreen(this, this.element, null, (call) -> {
                if (call != null) {
                    if ((call.path != null) && (call.soundType != null)) {
                        this.getItem().audios.add(call);
                    }
                }
            });
            Minecraft.getInstance().displayGuiScreen(s);
        });
        UIBase.colorizeButton(this.addAudioButton);

        this.removeAudioButton = new AdvancedButton(0, 0, 200, 20, Locals.localize("drippyloadingscreen.audio.remove"), true, (press) -> {
            SelectAudioScreen s = new SelectAudioScreen(this, this.element, (call) -> {
                if (call != null) {
                    FHYesNoPopup p = new FHYesNoPopup(300, new Color(0, 0, 0, 0), 240, (call2) -> {
                        if (call2) {
                            this.getItem().audios.remove(call);
                            Minecraft.getInstance().displayGuiScreen(this);
                        }
                    }, StringUtils.splitLines(Locals.localize("drippyloadingscreen.audio.remove.confirm"), "%n%"));
                    PopupHandler.displayPopup(p);
                }
            });
            Minecraft.getInstance().displayGuiScreen(s);
        });
        UIBase.colorizeButton(this.removeAudioButton);

        this.editAudioButton = new AdvancedButton(0, 0, 200, 20, Locals.localize("drippyloadingscreen.audio.edit"), true, (press) -> {
            SelectAudioScreen s = new SelectAudioScreen(this, this.element, (call) -> {
                if (call != null) {
                    //Do nothing with callback, since audio is already part of the element
                    EditAudioScreen s2 = new EditAudioScreen(this, this.element, call, (call2) -> {});
                    Minecraft.getInstance().displayGuiScreen(s2);
                }
            });
            Minecraft.getInstance().displayGuiScreen(s);
        });
        UIBase.colorizeButton(this.editAudioButton);

        this.backButton = new AdvancedButton(0, 0, 200, 20, Locals.localize("drippyloadingscreen.back"), true, (press) -> {
            Minecraft.getInstance().displayGuiScreen(this.handler);
            this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
        });
        UIBase.colorizeButton(this.backButton);

    }

    protected AudioCustomizationItem getItem() {
        return (AudioCustomizationItem) this.element.object;
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {

        int xCenter = this.width / 2;
        int yCenter = this.height / 2;

        RenderSystem.enableBlend();

        fill(matrix, 0, 0, this.width, this.height, BACKGROUND_COLOR.getRGB());

        drawCenteredString(matrix, font, Locals.localize("drippyloadingscreen.audio.manage"), this.width / 2, 20, -1);

        this.addAudioButton.setX(xCenter - (this.addAudioButton.getWidth() / 2));
        this.addAudioButton.setY(yCenter - 35);
        this.addAudioButton.render(matrix, mouseX, mouseY, partialTicks);

        this.removeAudioButton.setX(xCenter - (this.removeAudioButton.getWidth() / 2));
        this.removeAudioButton.setY(yCenter - 10);
        this.removeAudioButton.render(matrix, mouseX, mouseY, partialTicks);

        this.editAudioButton.setX(xCenter - (this.editAudioButton.getWidth() / 2));
        this.editAudioButton.setY(yCenter + 15);
        this.editAudioButton.render(matrix, mouseX, mouseY, partialTicks);

        this.backButton.setX(xCenter - (this.backButton.getWidth() / 2));
        this.backButton.setY(this.height - 35);
        this.backButton.render(matrix, mouseX, mouseY, partialTicks);

    }

    @Override
    public void closeScreen() {
        Minecraft.getInstance().displayGuiScreen(this.handler);
        this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
    }

}
