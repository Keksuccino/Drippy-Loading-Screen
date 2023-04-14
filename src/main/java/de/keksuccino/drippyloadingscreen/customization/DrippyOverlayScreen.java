package de.keksuccino.drippyloadingscreen.customization;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.DrippyConfigScreen;
import de.keksuccino.drippyloadingscreen.mixin.MixinCache;
import de.keksuccino.drippyloadingscreen.mixin.mixins.client.IMixinLoadingOverlay;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.helper.CustomizationHelperUI;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.FMContextMenu;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerRegistry;
import de.keksuccino.konkrete.gui.content.ContextMenu;
import de.keksuccino.konkrete.input.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DrippyOverlayScreen extends Screen {

    private static final Logger LOGGER = LogManager.getLogger();

    private static Button.OnPress configButtonOnPress = (press) -> {
        Minecraft.getInstance().setScreen(new DrippyConfigScreen(Minecraft.getInstance().screen));
    };

    private PoseStack cachedStack = null;

    public DrippyOverlayScreen() {
        super(Component.literal(""));
        MixinCache.cachedCurrentLoadingScreenProgress = 0.5F;
        if (!MenuCustomization.isMenuCustomizable(this)) {
            LOGGER.info("[DRIPPY LOADING SCREEN] Force-enabling customizations for DrippyOverlayScreen..");
            MenuCustomization.enableCustomizationForMenu(this);
        }
    }

    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY, float partial) {
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
        this.cachedStack = matrix;
        this.renderBackground(matrix);
        super.render(matrix, mouseX, mouseY, partial);
    }

    @Override
    public void renderDirtBackground(PoseStack matrix) {
        DrippyOverlayMenuHandler handler = (DrippyOverlayMenuHandler) MenuHandlerRegistry.getHandlerFor(this);
        if (handler.customBackgroundColor != null) {
            fill(this.cachedStack, 0, 0, this.width, this.height, handler.customBackgroundColor.getRGB());
        } else {
            fill(this.cachedStack, 0, 0, this.width, this.height, IMixinLoadingOverlay.getBrandBackgroundDrippy().getAsInt());
        }
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ScreenEvent.BackgroundRendered(this, new PoseStack()));
    }

}
