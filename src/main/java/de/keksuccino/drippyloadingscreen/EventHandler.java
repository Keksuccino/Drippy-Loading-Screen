package de.keksuccino.drippyloadingscreen;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.UIBase;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.content.AdvancedImageButton;
import de.keksuccino.konkrete.input.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandler {

    private static final ResourceLocation EDIT_BUTTON_TEXTURE = new ResourceLocation("drippyloadingscreen", "textures/edit_button.png");

    private static AdvancedButton editLoadingScreenButton = new AdvancedImageButton(-30, 40, 80, 40, EDIT_BUTTON_TEXTURE, true, (press) -> {
        Minecraft.getInstance().setScreen(new DrippyOverlayScreen());
    }) {
        @Override
        public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
            this.setDescription(StringUtils.splitLines(I18n.get("drippyloadingscreen.edit_loading_screen.desc"), "\n"));
            UIBase.colorizeButton(this);
            if (this.isHovered()) {
                this.setX(-20);
            } else {
                this.setX(-30);
            }
            super.render(matrix, mouseX, mouseY, partialTicks);
        }
    };

    @SubscribeEvent
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post e) {
        if (e.getGui() instanceof MainMenuScreen) {
            if (FancyMenu.config.getOrDefault("showcustomizationbuttons", true)) {
                editLoadingScreenButton.render(e.getMatrixStack(), e.getMouseX(), e.getMouseY(), e.getRenderPartialTicks());
            }
        }
    }

}
