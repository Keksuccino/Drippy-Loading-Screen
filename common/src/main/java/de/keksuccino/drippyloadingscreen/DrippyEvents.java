package de.keksuccino.drippyloadingscreen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.fancymenu.customization.overlay.CustomizationOverlay;
import de.keksuccino.fancymenu.events.screen.InitOrResizeScreenCompletedEvent;
import de.keksuccino.fancymenu.events.screen.RenderScreenEvent;
import de.keksuccino.fancymenu.util.LocalizationUtils;
import de.keksuccino.fancymenu.util.cycle.CommonCycles;
import de.keksuccino.fancymenu.util.event.acara.EventListener;
import de.keksuccino.fancymenu.util.rendering.RenderingUtils;
import de.keksuccino.fancymenu.util.rendering.ui.UIBase;
import de.keksuccino.fancymenu.util.rendering.ui.contextmenu.v2.ContextMenu;
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

    private static final ResourceLocation EDIT_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath("drippyloadingscreen", "textures/edit_button.png");

    private ContextMenu drippyMenu;
    private ExtendedButton drippyButton;

    @EventListener
    public void onInitOrResizeScreenCompleted(InitOrResizeScreenCompletedEvent e) {

        if ((e.getScreen() instanceof TitleScreen) && CustomizationOverlay.isOverlayVisible(e.getScreen())) {

            this.drippyMenu = new ContextMenu()
                    .setForceDefaultTooltipStyle(true)
                    .setForceUIScale(true);

            this.drippyMenu.addClickableEntry("customize_loading_screen", Component.translatable("drippyloadingscreen.settings.customize_loading_screen"), (menu, entry) -> {
                Minecraft.getInstance().setScreen(new DrippyOverlayScreen());
            }).setIcon(ContextMenu.IconFactory.getIcon("edit"));

            this.drippyMenu.addSeparatorEntry("separator_after_customize");

            this.drippyMenu.addValueCycleEntry("allow_universal_layouts",
                            CommonCycles.cycleEnabledDisabled("drippyloadingscreen.settings.allow_universal", DrippyLoadingScreen.getOptions().allowUniversalLayouts.getValue())
                                    .addCycleListener(value -> DrippyLoadingScreen.getOptions().allowUniversalLayouts.setValue(value.getAsBoolean())))
                    .setTooltipSupplier((menu, entry) -> Tooltip.of(LocalizationUtils.splitLocalizedLines("drippyloadingscreen.settings.allow_universal.desc")))
                    .setIcon(ContextMenu.IconFactory.getIcon("layout"));

            this.drippyMenu.addValueCycleEntry("wait_for_textures_in_loading",
                            CommonCycles.cycleEnabledDisabled("drippyloadingscreen.settings.wait_for_textures_in_loading", DrippyLoadingScreen.getOptions().waitForTexturesInLoading.getValue())
                                    .addCycleListener(value -> DrippyLoadingScreen.getOptions().waitForTexturesInLoading.setValue(value.getAsBoolean())))
                    .setTooltipSupplier((menu, entry) -> Tooltip.of(LocalizationUtils.splitLocalizedLines("drippyloadingscreen.settings.wait_for_textures_in_loading.desc")))
                    .setIcon(ContextMenu.IconFactory.getIcon("timer"));

            this.drippyMenu.addValueCycleEntry("early_fade_out_elements",
                            CommonCycles.cycleEnabledDisabled("drippyloadingscreen.settings.early_fade_out_elements", DrippyLoadingScreen.getOptions().earlyFadeOutElements.getValue())
                                    .addCycleListener(value -> DrippyLoadingScreen.getOptions().earlyFadeOutElements.setValue(value.getAsBoolean())))
                    .setTooltipSupplier((menu, entry) -> Tooltip.of(LocalizationUtils.splitLocalizedLines("drippyloadingscreen.settings.early_fade_out_elements.desc")));

            this.drippyMenu.addValueCycleEntry("fade_out_loading_screen",
                            CommonCycles.cycleEnabledDisabled("drippyloadingscreen.settings.fade_out_loading_screen", DrippyLoadingScreen.getOptions().fadeInOutLoadingScreen.getValue())
                                    .addCycleListener(value -> DrippyLoadingScreen.getOptions().fadeInOutLoadingScreen.setValue(value.getAsBoolean())))
                    .setTooltipSupplier((menu, entry) -> Tooltip.of(LocalizationUtils.splitLocalizedLines("drippyloadingscreen.settings.fade_out_loading_screen.desc")));

            //------------------------------

            ExtendedButton editButton = new ExtendedButton(-30, 40, 80, 40, Component.empty(), (button) -> {
                drippyMenu.openMenuAt(10, 80 - 10);
            }) {

                @Override
                public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial) {

                    var m = CustomizationOverlay.getCurrentMenuBarInstance();
                    if ((m == null) || (!m.isUserNavigatingInMenuBar() && !drippyMenu.isUserNavigatingInMenu())) {
                        TooltipHandler.INSTANCE.addTooltip(Tooltip.of(LocalizationUtils.splitLocalizedLines("drippyloadingscreen.edit_loading_screen.desc")).setDefaultStyle().setScale(UIBase.getUIScale()), () -> this.isHovered, false, true);
                    }
                    if (this.isHoveredOrFocused() || drippyMenu.isOpen()) {
                        this.setX(-20);
                    } else {
                        this.setX(-30);
                    }
                    super.render(graphics, mouseX, mouseY, partial);
                    RenderSystem.enableBlend();
                    RenderingUtils.resetShaderColor(graphics);
                    graphics.blit(EDIT_BUTTON_TEXTURE, this.getX(), this.getY(), 0.0f, 0.0f, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
                    RenderingUtils.resetShaderColor(graphics);

                    drippyMenu.render(graphics, mouseX, mouseY, partial);

                }

                @Override
                protected void renderBackground(@NotNull GuiGraphics graphics) {
                    boolean b = this.isHovered;
                    if (drippyMenu.isOpen()) this.isHovered = true;
                    super.renderBackground(graphics);
                    this.isHovered = b;
                }

                @Override
                public boolean mouseClicked(double $$0, double $$1, int $$2) {
                    if (drippyMenu.isOpen() && !drippyMenu.isUserNavigatingInMenu() && !this.isHovered()) {
                        drippyMenu.closeMenu();
                        this.setFocused(false);
                    }
                    return super.mouseClicked($$0, $$1, $$2);
                }

            };
            UIBase.applyDefaultWidgetSkinTo(editButton);

            // 2 because MenuBar and DebugOverlay need to be at pos 0 and 1
            e.getWidgets().add(2, editButton);
            this.drippyButton = editButton;
            e.getWidgets().add(2, this.drippyMenu);

        }

    }

    @EventListener(priority = 0) //FM is -1
    public void onScreenRenderPost(RenderScreenEvent.Post e) {

        if ((e.getScreen() instanceof TitleScreen) && CustomizationOverlay.isOverlayVisible(e.getScreen())) {
            if (this.drippyButton != null) {
                this.drippyButton.render(e.getGraphics(), e.getMouseX(), e.getMouseY(), e.getPartial());
            }
        }

    }

}
