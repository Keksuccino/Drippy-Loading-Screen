package de.keksuccino.drippyloadingscreen.mixin.mixins.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayMenuHandler;
import de.keksuccino.drippyloadingscreen.customization.deepcustomization.DeepCustomizationLayers;
import de.keksuccino.drippyloadingscreen.customization.items.Items;
import de.keksuccino.drippyloadingscreen.customization.placeholders.Placeholders;
import de.keksuccino.drippyloadingscreen.mixin.MixinCache;
import de.keksuccino.fancymenu.api.item.CustomizationItemContainer;
import de.keksuccino.fancymenu.api.item.CustomizationItemRegistry;
import de.keksuccino.fancymenu.events.InitOrResizeScreenEvent;
import de.keksuccino.fancymenu.menu.animation.AnimationHandler;
import de.keksuccino.fancymenu.menu.button.ButtonCachedEvent;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.gameintro.GameIntroHandler;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.popup.FMNotificationPopup;
import de.keksuccino.fancymenu.menu.fancy.item.CustomizationItemBase;
import de.keksuccino.fancymenu.menu.fancy.item.items.ticker.TickerCustomizationItemContainer;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerBase;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerRegistry;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.rendering.animation.IAnimationRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

//TODO übernehmen
@Mixin(LoadingOverlay.class)
public class MixinLoadingOverlay {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final File CHECKED_FOR_OLD_LAYOUTS_FILE = new File(DrippyLoadingScreen.MOD_DIR.getPath(), "/.checked_for_old_layouts");
    private static final File LEGACY_LAYOUT_DIR = new File(DrippyLoadingScreen.MOD_DIR.getPath(), "/customization");

    @Shadow private float currentProgress;

    private static boolean initialized = false;
    private static DrippyOverlayScreen drippyOverlayScreen = null;
    private static DrippyOverlayMenuHandler drippyOverlayHandler = null;
    private int lastScreenWidth = 0;
    private int lastScreenHeight = 0;
    private double lastGuiScale = 0;

    private static final IntSupplier BACKGROUND_COLOR = () -> {
        if ((drippyOverlayHandler != null) && (drippyOverlayHandler.customBackgroundColor != null)) {
            return drippyOverlayHandler.customBackgroundColor.getRGB();
        }
        return IMixinLoadingOverlay.getBrandBackgroundDrippy().getAsInt();
    };

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(Minecraft mc, ReloadInstance reload, Consumer consumer, boolean b, CallbackInfo info) {
        if (!initialized) {
            LOGGER.info("[DRIPPY LOADING SCREEN] Initializing fonts for text rendering..");
            //This makes text rendering work in the game loading screen
            this.loadFonts();
            //Register custom placeholders
            Placeholders.registerAll();
            //Register custom element types
            Items.registerAll();
            //Register deep customization elements
            DeepCustomizationLayers.registerAll();
            LOGGER.info("[DRIPPY LOADING SCREEN] Calculating animation sizes for FancyMenu..");
            //Setup FancyMenu animation sizes
            AnimationHandler.setupAnimationSizes();
            initialized = true;
        }
        this.handleInitOverlay();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderPre(PoseStack matrix, int mouseX, int mouseY, float partial, CallbackInfo info) {
        MixinCache.cachedCurrentLoadingScreenProgress = this.currentProgress;
        this.handleInitOverlay();
        if (drippyOverlayScreen != null) {
            this.runMenuHandlerTask(() -> {
                drippyOverlayHandler.onRenderPre(new ScreenEvent.Render.Pre(drippyOverlayScreen, matrix, mouseX, mouseY, partial));
            });
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderPost(PoseStack matrix, int mouseX, int mouseY, float partial, CallbackInfo info) {
        if (drippyOverlayScreen != null) {
            this.runMenuHandlerTask(() -> {
                drippyOverlayHandler.onRenderPost(new ScreenEvent.Render.Post(drippyOverlayScreen, matrix, mouseX, mouseY, partial));
            });
        }
        MixinCache.cachedCurrentLoadingScreenProgress = this.currentProgress;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"))
    private void onBackgroundRendered(PoseStack matrix, int mouseX, int mouseY, float partial, CallbackInfo info) {
        if (drippyOverlayScreen != null) {
            this.runMenuHandlerTask(() -> {
                drippyOverlayHandler.drawToBackground(new ScreenEvent.BackgroundRendered(drippyOverlayScreen, matrix));
            });
        }
    }

    //Fires when the loading screen gets closed (final finishing stage)
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"))
    private void onClose(PoseStack matrix, int mouseX, int mouseY, float partial, CallbackInfo info) {
//        AnimationHandler.resetAnimations();
//        AnimationHandler.stopAnimationSounds();
        if (Minecraft.getInstance().screen != null) {
            Screen s = Minecraft.getInstance().screen;
            Minecraft.getInstance().screen = drippyOverlayScreen;
            Minecraft.getInstance().setScreen(s);
            this.checkForOldLayouts();
        }
    }

    private void checkForOldLayouts() {
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

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_clearColor(FFFF)V"), index = 0)
    private float overrideBackgroundColorInClearColor0(float f) {
        int i2 = BACKGROUND_COLOR.getAsInt();
        return (float)(i2 >> 16 & 255) / 255.0F;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_clearColor(FFFF)V"), index = 1)
    private float overrideBackgroundColorInClearColor1(float f) {
        int i2 = BACKGROUND_COLOR.getAsInt();
        return (float)(i2 >> 8 & 255) / 255.0F;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_clearColor(FFFF)V"), index = 2)
    private float overrideBackgroundColorInClearColor2(float f) {
        int i2 = BACKGROUND_COLOR.getAsInt();
        return (float)(i2 & 255) / 255.0F;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;replaceAlpha(II)I"), index = 0)
    private int overrideBackgroundColorInReplaceAlpha(int originalColor) {
        return BACKGROUND_COLOR.getAsInt();
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;replaceAlpha(II)I"), index = 1)
    private int setCustomBackgroundOpacityInReplaceAlpha(int alpha) {
        float opacity = Math.max(0.0F, Math.min(1.0F, (float)alpha / 255.0F));
        this.setCustomBackgroundOpacity(opacity);
        if (!DrippyLoadingScreen.config.getOrDefault("early_fade_out_elements", false)) {
            this.setOverlayOpacity(opacity);
        }
        return alpha;
    }

    @Inject(method = "drawProgressBar", at = @At("HEAD"), cancellable = true)
    private void replaceOriginalProgressBar(PoseStack matrix, int p_96184_, int p_96185_, int p_96186_, int p_96187_, float opacity, CallbackInfo info) {
        info.cancel();
        if (DrippyLoadingScreen.config.getOrDefault("early_fade_out_elements", false)) {
            this.setOverlayOpacity(opacity);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if ((drippyOverlayHandler != null) && (drippyOverlayHandler.progressBarItem != null) && (drippyOverlayScreen != null)) {
            drippyOverlayHandler.progressBarItem.render(matrix, drippyOverlayScreen);
        }
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIFFIIII)V"), index = 1)
    private int renderOriginalLogoOffscreenSetXMin(int xMinOriginal) {
        return -1000000;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIFFIIII)V"), index = 2)
    private int renderOriginalLogoOffscreenSetYMin(int yMinOriginal) {
        return -1000000;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIFFIIII)V"), index = 3)
    private int renderOriginalLogoOffscreenSetXMax(int xMaxOriginal) {
        return -1000000;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIFFIIII)V"), index = 4)
    private int renderOriginalLogoOffscreenSetYMax(int yMaxOriginal) {
        return -1000000;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V", shift = At.Shift.AFTER))
    private void renderCustomizableInstanceOfLogo(PoseStack matrix, int p_96179_, int p_96180_, float p_96181_, CallbackInfo info) {
        if ((drippyOverlayHandler != null) && (drippyOverlayHandler.logoItem != null) && (drippyOverlayScreen != null)) {
            drippyOverlayHandler.logoItem.render(matrix, drippyOverlayScreen);
        }
    }

    private void setCustomBackgroundOpacity(float opacity) {
        if (drippyOverlayHandler != null) {
            drippyOverlayHandler.backgroundOpacity = opacity;
        }
    }

    private void setOverlayOpacity(float opacity) {
        if (drippyOverlayHandler != null) {
            List<CustomizationItemBase> l = new ArrayList<>();
            l.addAll(drippyOverlayHandler.frontRenderItems);
            l.addAll(drippyOverlayHandler.backgroundRenderItems);
            for (CustomizationItemBase i : l) {
                i.opacity = opacity;
            }
            if (drippyOverlayHandler.logoItem != null) {
                drippyOverlayHandler.logoItem.opacity = opacity;
            }
            if (drippyOverlayHandler.progressBarItem != null) {
                drippyOverlayHandler.progressBarItem.opacity = opacity;
            }
        }
    }

    private void loadFonts() {
        try {
            MixinCache.gameThreadRunnables.add(() -> {
                Object m = ((IMixinSimplePreparableReloadListener)((IMixinFontManager)((IMixinMinecraft)Minecraft.getInstance()).getFontManagerDrippy()).getReloadListenerDrippy()).invokePrepareDrippy(Minecraft.getInstance().getResourceManager(), InactiveProfiler.INSTANCE);
                ((IMixinSimplePreparableReloadListener)((IMixinFontManager)((IMixinMinecraft)Minecraft.getInstance()).getFontManagerDrippy()).getReloadListenerDrippy()).invokeApplyDrippy(m, Minecraft.getInstance().getResourceManager(), InactiveProfiler.INSTANCE);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleInitOverlay() {

        try {

            //Manually run clientTick method of FM's Ticker item to clear old async ticker elements in game loading screen
            CustomizationItemContainer tickerItem = CustomizationItemRegistry.getItem("fancymenu_customization_item_ticker");
            if (tickerItem != null) {
                ((TickerCustomizationItemContainer)tickerItem).onClientTick(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
            }

            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            //Setup overlay
            if (drippyOverlayScreen == null) {
                drippyOverlayScreen = new DrippyOverlayScreen();
                MenuHandlerBase b = MenuHandlerRegistry.getHandlerFor(drippyOverlayScreen);
                if (b != null) {
                    Map<String, MenuHandlerBase> m = this.getMenuHandlerRegistryMap();
                    if (m != null) {
                        m.remove(DrippyOverlayScreen.class.getName());
                    }
                }
                b = new DrippyOverlayMenuHandler();
                MenuHandlerRegistry.registerHandler(b);
                drippyOverlayHandler = (DrippyOverlayMenuHandler) b;
                this.initOverlay(screenWidth, screenHeight);
                this.lastScreenWidth = screenWidth;
                this.lastScreenHeight = screenHeight;
                this.lastGuiScale = Minecraft.getInstance().getWindow().getGuiScale();
            }

            //Re-init overlay on window size change and GUI scale change
            if ((screenWidth != this.lastScreenWidth) || (screenHeight != this.lastScreenHeight)) {
                this.initOverlay(screenWidth, screenHeight);
            } else if (this.lastGuiScale != Minecraft.getInstance().getWindow().getGuiScale()) {
                this.initOverlay(Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
            }
            this.lastScreenWidth = screenWidth;
            this.lastScreenHeight = screenHeight;
            this.lastGuiScale = Minecraft.getInstance().getWindow().getGuiScale();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Nullable
    private Map<String, MenuHandlerBase> getMenuHandlerRegistryMap() {
        try {
            Field f = MenuHandlerRegistry.class.getDeclaredField("handlers");
            f.setAccessible(true);
            return (Map<String, MenuHandlerBase>) f.get(MenuHandlerRegistry.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initOverlay(int screenWidth, int screenHeight) {
        this.runMenuHandlerTask(() -> {
            try {
                drippyOverlayScreen.width = screenWidth;
                drippyOverlayScreen.height = screenHeight;
                drippyOverlayHandler.onInitPre(new InitOrResizeScreenEvent.Pre(drippyOverlayScreen));
                drippyOverlayHandler.onButtonsCached(new ButtonCachedEvent(drippyOverlayScreen, new ArrayList<>(), false));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void runMenuHandlerTask(Runnable run) {

       try {

           boolean gameIntroDisplayed = GameIntroHandler.introDisplayed;
           GameIntroHandler.introDisplayed = true;
           MenuHandlerBase menuHandler = MenuHandlerRegistry.getLastActiveHandler();
           MenuHandlerRegistry.setActiveHandler(DrippyOverlayScreen.class.getName());
           boolean allowCustomizations = MenuCustomization.allowScreenCustomization;
           MenuCustomization.allowScreenCustomization = true;
           boolean animationsReady = AnimationHandler.isReady();
           AnimationHandler.setReady(true);

           Screen s = Minecraft.getInstance().screen;
           if ((s == null) || !(s instanceof DrippyOverlayScreen)) {
               Minecraft.getInstance().screen = drippyOverlayScreen;
               run.run();
               Minecraft.getInstance().screen = s;
           }

           GameIntroHandler.introDisplayed = gameIntroDisplayed;
           MenuCustomization.allowScreenCustomization = allowCustomizations;
           AnimationHandler.setReady(animationsReady);
           if (menuHandler != null) {
               MenuHandlerRegistry.setActiveHandler(menuHandler.getMenuIdentifier());
           }

       } catch (Exception e) {
           e.printStackTrace();
       }

    }

}
