package de.keksuccino.drippyloadingscreen;

import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayMenuHandler;
import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.drippyloadingscreen.customization.deepcustomization.DeepCustomizationLayers;
import de.keksuccino.drippyloadingscreen.customization.items.Items;
import de.keksuccino.drippyloadingscreen.customization.placeholders.Placeholders;
import de.keksuccino.drippyloadingscreen.mixin.MixinCache;
import de.keksuccino.drippyloadingscreen.mixin.mixins.client.IMixinLoadingOverlay;
import de.keksuccino.drippyloadingscreen.mixin.mixins.client.IMixinMinecraft;
import de.keksuccino.fancymenu.api.item.CustomizationItemContainer;
import de.keksuccino.fancymenu.api.item.CustomizationItemRegistry;
import de.keksuccino.fancymenu.events.InitOrResizeScreenEvent;
import de.keksuccino.fancymenu.menu.button.ButtonCachedEvent;
import de.keksuccino.fancymenu.menu.fancy.gameintro.GameIntroHandler;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.popup.FMNotificationPopup;
import de.keksuccino.fancymenu.menu.fancy.item.CustomizationItemBase;
import de.keksuccino.fancymenu.menu.fancy.item.items.ticker.TickerCustomizationItemContainer;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerBase;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerRegistry;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.StringUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.FastColor;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.earlydisplay.ColourScheme;
import net.minecraftforge.fml.earlydisplay.DisplayWindow;
import net.minecraftforge.fml.loading.FMLConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

public interface CustomizableLoadingOverlay {

    Logger LOGGER = LogManager.getLogger();

    File CHECKED_FOR_OLD_LAYOUTS_FILE = new File(DrippyLoadingScreen.MOD_DIR.getPath(), "/.checked_for_old_layouts");
    File LEGACY_LAYOUT_DIR = new File(DrippyLoadingScreen.MOD_DIR.getPath(), "/customization");

    SharedLoadingOverlayData DATA = new SharedLoadingOverlayData();

    default void onConstruct() {
        if (!SharedLoadingOverlayData.initialized) {
            DrippyLoadingScreen.initConfig();
            LOGGER.info("[DRIPPY LOADING SCREEN] Initializing fonts for text rendering..");
            //This makes text rendering work in the game loading screen
            this.loadFonts();
            //Register custom placeholders
            Placeholders.registerAll();
            //Register custom element types
            Items.registerAll();
            //Register deep customization elements
            DeepCustomizationLayers.registerAll();
            SharedLoadingOverlayData.initialized = true;
        }
        DATA.reset();
        this.handleInitOverlay();
    }

    default void onRenderPre(GuiGraphics graphics, int mouseX, int mouseY, float partial, float currentProgress) {
        MixinCache.cachedCurrentLoadingScreenProgress = currentProgress;
        this.handleInitOverlay();
        this.scaleOverlayStart(graphics);
        if (SharedLoadingOverlayData.drippyOverlayScreen != null) {
            this.runMenuHandlerTask(() -> {
                SharedLoadingOverlayData.drippyOverlayHandler.onRenderPre(new ScreenEvent.Render.Pre(SharedLoadingOverlayData.drippyOverlayScreen, graphics, mouseX, mouseY, partial));
            });
        }
        this.scaleOverlayEnd(graphics);
    }

    default void onRenderPost(GuiGraphics graphics, int mouseX, int mouseY, float partial, float currentProgress) {
        this.scaleOverlayStart(graphics);
        if (SharedLoadingOverlayData.drippyOverlayScreen != null) {
            this.runMenuHandlerTask(() -> {
                SharedLoadingOverlayData.drippyOverlayHandler.onRenderPost(new ScreenEvent.Render.Post(SharedLoadingOverlayData.drippyOverlayScreen, graphics, mouseX, mouseY, partial));
            });
        }
        this.scaleOverlayEnd(graphics);
        MixinCache.cachedCurrentLoadingScreenProgress = currentProgress;
    }

    default void onBackgroundRendered(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        this.scaleOverlayStart(graphics);
        if (SharedLoadingOverlayData.drippyOverlayScreen != null) {
            this.runMenuHandlerTask(() -> {
                SharedLoadingOverlayData.drippyOverlayHandler.drawToBackground(new ScreenEvent.BackgroundRendered(SharedLoadingOverlayData.drippyOverlayScreen, graphics));
            });
        }
        this.scaleOverlayEnd(graphics);
    }

    default void renderCustomizableInstanceOfLogo(GuiGraphics graphics) {
        if ((SharedLoadingOverlayData.drippyOverlayHandler != null) && (SharedLoadingOverlayData.drippyOverlayHandler.logoItem != null) && (SharedLoadingOverlayData.drippyOverlayScreen != null)) {
            if (!SharedLoadingOverlayData.drippyOverlayHandler.logoItem.useOriginalSizeAndPosCalculation) {
                this.scaleOverlayStart(graphics);
            }
            SharedLoadingOverlayData.drippyOverlayHandler.logoItem.render(graphics, SharedLoadingOverlayData.drippyOverlayScreen);
            this.scaleOverlayEnd(graphics);
        }
    }

    default void renderCustomizableInstanceOfProgressBar(GuiGraphics graphics, float opacity) {
        if (DrippyLoadingScreen.config.getOrDefault("early_fade_out_elements", false)) {
            this.setOverlayOpacity(opacity);
        }
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        if ((SharedLoadingOverlayData.drippyOverlayHandler != null) && (SharedLoadingOverlayData.drippyOverlayHandler.progressBarItem != null) && (SharedLoadingOverlayData.drippyOverlayScreen != null)) {
            if (!SharedLoadingOverlayData.drippyOverlayHandler.progressBarItem.useOriginalSizeAndPosCalculation) {
                this.scaleOverlayStart(graphics);
            }
            SharedLoadingOverlayData.drippyOverlayHandler.progressBarItem.render(graphics, SharedLoadingOverlayData.drippyOverlayScreen);
            this.scaleOverlayEnd(graphics);
        }
    }

    /** Fires when the loading screen gets closed (final finishing stage) **/
    default void onCloseOverlay() {
        if (Minecraft.getInstance().screen != null) {
            this.checkForOldLayouts();
        }
    }

    @SuppressWarnings("all")
    default void checkForOldLayouts() {
        if (LEGACY_LAYOUT_DIR.isDirectory()) {
            String[] layoutFilesList = LEGACY_LAYOUT_DIR.list((dir, name) -> {
                if (name.toLowerCase().endsWith(".dllayout")) {
                    return true;
                }
                return false;
            });
            if (layoutFilesList.length > 0) {
                if (!CHECKED_FOR_OLD_LAYOUTS_FILE.isFile()) {
                    try {
                        CHECKED_FOR_OLD_LAYOUTS_FILE.createNewFile();
                        PopupHandler.displayPopup(new FMNotificationPopup(300, new Color(0,0,0,0), 240, null, StringUtils.splitLines(I18n.get("drippyloadingscreen.legacy_support.old_layouts"), "\n")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    default void setCustomBackgroundOpacity(float opacity) {
        if (SharedLoadingOverlayData.drippyOverlayHandler != null) {
            SharedLoadingOverlayData.drippyOverlayHandler.backgroundOpacity = opacity;
        }
    }

    default void setOverlayOpacity(float opacity) {
        if (opacity < 0.02F) {
            opacity = 0.02F;
        }
        if (SharedLoadingOverlayData.drippyOverlayHandler != null) {
            java.util.List<CustomizationItemBase> l = new ArrayList<>();
            l.addAll(SharedLoadingOverlayData.drippyOverlayHandler.frontRenderItems);
            l.addAll(SharedLoadingOverlayData.drippyOverlayHandler.backgroundRenderItems);
            for (CustomizationItemBase i : l) {
                i.opacity = opacity;
                if (i.opacity <= 0.02F) {
                    i.visible = false;
                }
            }
            if (SharedLoadingOverlayData.drippyOverlayHandler.logoItem != null) {
                SharedLoadingOverlayData.drippyOverlayHandler.logoItem.opacity = opacity;
                if (SharedLoadingOverlayData.drippyOverlayHandler.logoItem.opacity <= 0.02F) {
                    SharedLoadingOverlayData.drippyOverlayHandler.logoItem.hidden = true;
                }
            }
            if (SharedLoadingOverlayData.drippyOverlayHandler.progressBarItem != null) {
                SharedLoadingOverlayData.drippyOverlayHandler.progressBarItem.opacity = opacity;
                if (SharedLoadingOverlayData.drippyOverlayHandler.progressBarItem.opacity <= 0.02F) {
                    SharedLoadingOverlayData.drippyOverlayHandler.progressBarItem.hidden = true;
                }
            }
        }
    }

    @SuppressWarnings("all")
    default void loadFonts() {
        try {
            MixinCache.gameThreadRunnables.add(() -> {
                try {
                    FontManager fontManager = ((IMixinMinecraft)Minecraft.getInstance()).getFontManagerDrippy();
                    fontManager.apply(fontManager.prepare(Minecraft.getInstance().getResourceManager(), Util.backgroundExecutor()).get(), InactiveProfiler.INSTANCE);
                } catch (Exception ex) {
                    LOGGER.info("[DRIPPY LOADING SCREEN] Failed to load fonts!");
                    ex.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    default void handleInitOverlay() {

        try {

            //Manually run clientTick method of FM's Ticker item to clear old async ticker elements in game loading screen
            CustomizationItemContainer tickerItem = CustomizationItemRegistry.getItem("fancymenu_customization_item_ticker");
            if (tickerItem != null) {
                ((TickerCustomizationItemContainer)tickerItem).onClientTick(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
            }

            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            //Setup overlay
            if (SharedLoadingOverlayData.drippyOverlayScreen == null) {
                SharedLoadingOverlayData.drippyOverlayScreen = new DrippyOverlayScreen();
                MenuHandlerBase b = MenuHandlerRegistry.getHandlerFor(SharedLoadingOverlayData.drippyOverlayScreen);
                if (b != null) {
                    Map<String, MenuHandlerBase> m = this.getMenuHandlerRegistryMap();
                    if (m != null) {
                        m.remove(DrippyOverlayScreen.class.getName());
                    }
                }
                b = new DrippyOverlayMenuHandler();
                MenuHandlerRegistry.registerHandler(b);
                SharedLoadingOverlayData.drippyOverlayHandler = (DrippyOverlayMenuHandler) b;
                this.initOverlay(screenWidth, screenHeight);
                DATA.lastScreenWidth = screenWidth;
                DATA.lastScreenHeight = screenHeight;
            }

            //Re-init overlay on window size change
            if ((screenWidth != DATA.lastScreenWidth) || (screenHeight != DATA.lastScreenHeight)) {
                this.initOverlay(screenWidth, screenHeight);
            }
            DATA.lastScreenWidth = screenWidth;
            DATA.lastScreenHeight = screenHeight;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Nullable
    default Map<String, MenuHandlerBase> getMenuHandlerRegistryMap() {
        try {
            Field f = MenuHandlerRegistry.class.getDeclaredField("handlers");
            f.setAccessible(true);
            return (Map<String, MenuHandlerBase>) f.get(MenuHandlerRegistry.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    default void initOverlay(int screenWidth, int screenHeight) {
        this.runMenuHandlerTask(() -> {
            try {
                SharedLoadingOverlayData.drippyOverlayScreen.width = screenWidth;
                SharedLoadingOverlayData.drippyOverlayScreen.height = screenHeight;
                double oriScale = Minecraft.getInstance().getWindow().getGuiScale();
                SharedLoadingOverlayData.drippyOverlayHandler.onInitPre(new InitOrResizeScreenEvent.Pre(SharedLoadingOverlayData.drippyOverlayScreen));
                SharedLoadingOverlayData.drippyOverlayHandler.onButtonsCached(new ButtonCachedEvent(SharedLoadingOverlayData.drippyOverlayScreen, new ArrayList<>(), false));
                DATA.renderScale = Minecraft.getInstance().getWindow().getGuiScale();
                Minecraft.getInstance().getWindow().setGuiScale(oriScale);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    default void scaleOverlayStart(GuiGraphics graphics) {
        DATA.overlayScaled = true;
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        float scale = (float)(1.0D * (1.0D / guiScale) * DATA.renderScale);
        if (SharedLoadingOverlayData.drippyOverlayHandler != null) {
            List<CustomizationItemBase> l = new ArrayList<>();
            l.addAll(SharedLoadingOverlayData.drippyOverlayHandler.frontRenderItems);
            l.addAll(SharedLoadingOverlayData.drippyOverlayHandler.backgroundRenderItems);
            for (CustomizationItemBase i : l) {
                i.customGuiScale = (float)DATA.renderScale;
            }
        }
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, scale);
    }

    default void scaleOverlayEnd(GuiGraphics graphics) {
        if (DATA.overlayScaled) {
            graphics.pose().popPose();
            DATA.overlayScaled = false;
        }
    }

    default void runMenuHandlerTask(Runnable run) {

        try {

            boolean gameIntroDisplayed = GameIntroHandler.introDisplayed;
            GameIntroHandler.introDisplayed = true;
            MenuHandlerBase menuHandler = MenuHandlerRegistry.getLastActiveHandler();
            MenuHandlerRegistry.setActiveHandler(DrippyOverlayScreen.class.getName());

            Screen s = Minecraft.getInstance().screen;
            if (!(s instanceof DrippyOverlayScreen)) {
                Minecraft.getInstance().screen = SharedLoadingOverlayData.drippyOverlayScreen;
                run.run();
                Minecraft.getInstance().screen = s;
            }

            GameIntroHandler.introDisplayed = gameIntroDisplayed;
            if (menuHandler != null) {
                MenuHandlerRegistry.setActiveHandler(menuHandler.getMenuIdentifier());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    default void handleSetForgeEarlyLoadingConfigOption() {
        DrippyLoadingScreen.initConfig();
        if (!DrippyLoadingScreen.config.getOrDefault("enable_early_loading", true)) {
            if (FMLConfig.getBoolConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_CONTROL)) {
                LOGGER.info("[DRIPPY LOADING SCREEN] Force-disabling Forge early loading screen! The changes will be visible on next game launch.");
                FMLConfig.updateConfig(FMLConfig.ConfigValue.EARLY_WINDOW_CONTROL, false);
            }
        } else {
            if (!FMLConfig.getBoolConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_CONTROL)) {
                LOGGER.info("[DRIPPY LOADING SCREEN] Enabling Forge early loading screen! The changes will be visible on next game launch.");
                FMLConfig.updateConfig(FMLConfig.ConfigValue.EARLY_WINDOW_CONTROL, true);
            }
        }
    }

    static int replaceAlpha(int p_169325_, int p_169326_) {
        return p_169325_ & 16777215 | p_169326_ << 24;
    }

    class SharedLoadingOverlayData {

        public static boolean initialized = false;
        public static DrippyOverlayScreen drippyOverlayScreen = null;
        public static DrippyOverlayMenuHandler drippyOverlayHandler = null;

        private static final IntSupplier BACKGROUND_COLOR = () -> {
            if ((drippyOverlayHandler != null) && (drippyOverlayHandler.customBackgroundColor != null)) {
                return drippyOverlayHandler.customBackgroundColor.getRGB();
            }
            return -1;
        };

        public int lastScreenWidth = 0;
        public int lastScreenHeight = 0;
        public double renderScale = 0;
        public boolean overlayScaled = false;

        public void reset() {
            lastScreenWidth = 0;
            lastScreenHeight = 0;
            renderScale = 0;
            overlayScaled = false;
        }

        public static int getBackgroundColorInt(@Nullable DisplayWindow displayWindow) {
            int i = BACKGROUND_COLOR.getAsInt();
            if (i == -1) {
                if (displayWindow != null) {
                    ColourScheme.Colour color = displayWindow.context().colourScheme().background();
                    i = FastColor.ARGB32.color(255, color.red(), color.green(), color.blue());
                } else {
                    IntSupplier supplier = IMixinLoadingOverlay.getBrandBackgroundDrippy();
                    if (supplier == null) return -1;
                    return supplier.getAsInt();
                }
            }
            return i;
        }

    }

}
