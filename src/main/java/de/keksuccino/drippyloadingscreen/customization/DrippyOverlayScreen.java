package de.keksuccino.drippyloadingscreen.customization;

import net.minecraft.client.gui.GuiGraphics;
import de.keksuccino.drippyloadingscreen.DrippyConfigScreen;
import de.keksuccino.drippyloadingscreen.mixin.MixinCache;
import de.keksuccino.drippyloadingscreen.mixin.mixins.client.IMixinLoadingOverlay;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.helper.CustomizationHelperUI;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.FMContextMenu;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerRegistry;
import de.keksuccino.konkrete.Konkrete;
import de.keksuccino.konkrete.events.client.GuiScreenEvent;
import de.keksuccino.konkrete.gui.content.ContextMenu;
import de.keksuccino.konkrete.input.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class DrippyOverlayScreen extends Screen {

    private static final Logger LOGGER = LogManager.getLogger();

    private static Button.OnPress configButtonOnPress = (press) -> {
        Minecraft.getInstance().setScreen(new DrippyConfigScreen(Minecraft.getInstance().screen));
    };

    private GuiGraphics cachedStack = null;

    public DrippyOverlayScreen() {
        super(Component.literal(""));
        MixinCache.cachedCurrentLoadingScreenProgress = 0.5F;
        if (!MenuCustomization.isMenuCustomizable(this)) {
            LOGGER.info("[DRIPPY LOADING SCREEN] Force-enabling customizations for DrippyOverlayScreen..");
            MenuCustomization.enableCustomizationForMenu(this);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        try {
            if (CustomizationHelperUI.bar != null) {
                ContextMenu con = CustomizationHelperUI.bar.getChild("fm.ui.tab.current");
                if ((con != null) && !((FMContextMenu)con).getContent().isEmpty()) {
                    ((FMContextMenu)con).getContent().get(0).setMessage(I18n.get("drippyloadingscreen.config.button"));
                    ((FMContextMenu)con).getContent().get(0).setDescription(StringUtils.splitLines(I18n.get("drippyloadingscreen.config.button.desc"), "\n"));
                    ((FMContextMenu)con).getContent().get(0).setPressAction(configButtonOnPress);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.cachedStack = graphics;
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partial);
    }

    @Override
    public void renderDirtBackground(@NotNull GuiGraphics graphics) {
        DrippyOverlayMenuHandler handler = (DrippyOverlayMenuHandler) MenuHandlerRegistry.getHandlerFor(this);
        if (handler.customBackgroundColor != null) {
            graphics.fill(0, 0, this.width, this.height, handler.customBackgroundColor.getRGB());
        } else if (IMixinLoadingOverlay.getBrandBackgroundDrippy() != null) {
            graphics.fill(0, 0, this.width, this.height, IMixinLoadingOverlay.getBrandBackgroundDrippy().getAsInt());
        }
        Konkrete.getEventHandler().callEventsFor(new GuiScreenEvent.BackgroundDrawnEvent(this, graphics));
    }

}
