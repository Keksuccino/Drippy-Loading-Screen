package de.keksuccino.drippyloadingscreen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.events.screen.InitOrResizeScreenCompletedEvent;
import de.keksuccino.fancymenu.util.LocalizationUtils;
import de.keksuccino.fancymenu.util.event.acara.EventListener;
import de.keksuccino.fancymenu.util.rendering.RenderingUtils;
import de.keksuccino.fancymenu.util.rendering.ui.UIBase;
import de.keksuccino.fancymenu.util.rendering.ui.tooltip.Tooltip;
import de.keksuccino.fancymenu.util.rendering.ui.tooltip.TooltipHandler;
import de.keksuccino.fancymenu.util.rendering.ui.widget.button.ExtendedButton;
import net.minecraft.client.gui.GuiGraphics;
import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DrippyEvents {

    private static final ResourceLocation EDIT_BUTTON_TEXTURE = new ResourceLocation("drippyloadingscreen", "textures/edit_button.png");

    @EventListener
    public void onInitOrResizeScreenCompleted(InitOrResizeScreenCompletedEvent e) {

        if ((e.getScreen() instanceof TitleScreen) && FancyMenu.getOptions().showCustomizationOverlay.getValue()) {

            ExtendedButton editButton = new ExtendedButton(-30, 40, 80, 40, Component.empty(), (button) -> {
                Minecraft.getInstance().setScreen(new DrippyOverlayScreen());
            }) {
                @Override
                public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial) {
                    TooltipHandler.INSTANCE.addTooltip(Tooltip.of(LocalizationUtils.splitLocalizedLines("drippyloadingscreen.edit_loading_screen.desc")).setDefaultStyle(), () -> this.isHovered, false, true);
                    if (this.isHoveredOrFocused()) {
                        this.setX(-20);
                    } else {
                        this.setX(-30);
                    }
                    super.render(graphics, mouseX, mouseY, partial);
                    RenderSystem.enableBlend();
                    RenderingUtils.resetShaderColor(graphics);
                    graphics.blit(EDIT_BUTTON_TEXTURE, this.getX(), this.getY(), 0.0f, 0.0f, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
                    RenderingUtils.resetShaderColor(graphics);
                }
            };
            e.addRenderableWidget(editButton);
            UIBase.applyDefaultWidgetSkinTo(editButton);

        }

    }

}
