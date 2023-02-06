package de.keksuccino.drippyloadingscreen.mixin.mixins.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayMenuHandler;
import de.keksuccino.drippyloadingscreen.customization.deepcustomization.DeepCustomizationLayers;
import de.keksuccino.drippyloadingscreen.customization.items.Items;
import de.keksuccino.drippyloadingscreen.customization.placeholders.Placeholders;
import de.keksuccino.drippyloadingscreen.mixin.MixinCache;
import de.keksuccino.fancymenu.api.item.CustomizationItemContainer;
import de.keksuccino.fancymenu.api.item.CustomizationItemRegistry;
import de.keksuccino.fancymenu.menu.animation.AnimationHandler;
import de.keksuccino.fancymenu.menu.button.ButtonCachedEvent;
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
import net.minecraft.client.gui.ResourceLoadProgressGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.resources.IAsyncReloader;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(ResourceLoadProgressGui.class)
public class MixinLoadingOverlay {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final File CHECKED_FOR_OLD_LAYOUTS_FILE = new File(DrippyLoadingScreen.MOD_DIR.getPath(), "/.checked_for_old_layouts");
    private static final File LEGACY_LAYOUT_DIR = new File(DrippyLoadingScreen.MOD_DIR.getPath(), "/customization");

    @Shadow private float currentProgress;
    @Shadow @Final private static int BRAND_BACKGROUND;

    private static boolean initialized = false;
    private static boolean backgroundColorCached = false;
    private static DrippyOverlayScreen drippyOverlayScreen = null;
    private static DrippyOverlayMenuHandler drippyOverlayHandler = null;
    private int lastScreenWidth = 0;
    private int lastScreenHeight = 0;
    private double lastGuiScale = 0;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(Minecraft mc, IAsyncReloader reloader, Consumer<Optional<Throwable>> consumer, boolean b, CallbackInfo info) {
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
            initialized = true;
        }
        this.handleInitOverlay();
        if (!backgroundColorCached) {
            MixinCache.originalLoadingScreenBackgroundColorSupplier = () -> BRAND_BACKGROUND;
            backgroundColorCached = true;
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderPre(MatrixStack matrix, int mouseX, int mouseY, float partial, CallbackInfo info) {
        MixinCache.cachedCurrentLoadingScreenProgress = this.currentProgress;
        this.handleInitOverlay();
        if (drippyOverlayScreen != null) {
            this.runMenuHandlerTask(() -> {
                drippyOverlayHandler.onRenderPre(new GuiScreenEvent.DrawScreenEvent.Pre(drippyOverlayScreen, matrix, mouseX, mouseY, partial));
            });
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderPost(MatrixStack matrix, int mouseX, int mouseY, float partial, CallbackInfo info) {
        if (drippyOverlayScreen != null) {
            this.runMenuHandlerTask(() -> {
                drippyOverlayHandler.onRenderPost(new GuiScreenEvent.DrawScreenEvent.Post(drippyOverlayScreen, matrix, mouseX, mouseY, partial));
            });
        }
        MixinCache.cachedCurrentLoadingScreenProgress = this.currentProgress;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;bind(Lnet/minecraft/util/ResourceLocation;)V"))
    private void onBackgroundRendered(MatrixStack matrix, int mouseX, int mouseY, float partial, CallbackInfo info) {
        if (drippyOverlayScreen != null) {
            this.runMenuHandlerTask(() -> {
                drippyOverlayHandler.drawToBackground(new GuiScreenEvent.BackgroundDrawnEvent(drippyOverlayScreen, matrix));
            });
        }
    }

    //Fires before the actual finishing process starts (first finishing stage)
//    @Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", shift = At.Shift.AFTER))
//    private void onPrepareFinishing(PoseStack p_96178_, int p_96179_, int p_96180_, float p_96181_, CallbackInfo ci) {
//    }

    //Fires after the finishing process is completed (second finishing stage)
//    @Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", shift = At.Shift.AFTER))
//    private void onFinished(PoseStack p_96178_, int p_96179_, int p_96180_, float p_96181_, CallbackInfo ci) {
//    }

    //Fires when the loading screen gets closed (final finishing stage)
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/LoadingGui;)V"))
    private void onClose(MatrixStack matrix, int mouseX, int mouseY, float partial, CallbackInfo info) {
        AnimationHandler.resetAnimations();
        AnimationHandler.stopAnimationSounds();
        if (Minecraft.getInstance().screen != null) {
            Minecraft.getInstance().screen.init(Minecraft.getInstance(), Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
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

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ResourceLoadProgressGui;fill(Lcom/mojang/blaze3d/matrix/MatrixStack;IIIII)V"), index = 5)
    private int overrideBackgroundColor(int originalColor) {
        if ((drippyOverlayHandler != null) && (drippyOverlayHandler.customBackgroundColor != null)) {
            return drippyOverlayHandler.customBackgroundColor.getRGB();
        }
        return BRAND_BACKGROUND;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;color4f(FFFF)V"), index = 3)
    private float overrideLogoOpacity(float originalOpacity) {
        if ((drippyOverlayHandler != null) && !drippyOverlayHandler.showLogo) {
            //Render at 0% opacity if hidden via layout
            return 0.0F;
        }
        //Force the logo opacity to 100% so it can't fade in/out
        return 1.0F;
    }

    @ModifyArg(method = "drawProgressBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ColorHelper$PackedColor;color(IIII)I"), index = 0)
    private int overrideProgressBarOpacity(int originalOpacity) {
        if ((drippyOverlayHandler != null) && !drippyOverlayHandler.showProgressBar) {
            //Render at 0% opacity if hidden via layout
            return 0;
        }
        //Force the progress bar opacity to 100% so it can't fade in/out
        return 255;
    }

    //Progress bar *R*GB
    @ModifyArg(method = "drawProgressBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ColorHelper$PackedColor;color(IIII)I"), index = 1)
    private int overrideProgressBarColorRed(int originalRed) {
        if ((drippyOverlayHandler != null) && (drippyOverlayHandler.customProgressBarColor != null)) {
            return drippyOverlayHandler.customProgressBarColor.getRed();
        }
        return originalRed;
    }

    //Progress bar R*G*B
    @ModifyArg(method = "drawProgressBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ColorHelper$PackedColor;color(IIII)I"), index = 2)
    private int overrideProgressBarColorGreen(int originalGreen) {
        if ((drippyOverlayHandler != null) && (drippyOverlayHandler.customProgressBarColor != null)) {
            return drippyOverlayHandler.customProgressBarColor.getGreen();
        }
        return originalGreen;
    }

    //Progress bar RG*B*
    @ModifyArg(method = "drawProgressBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ColorHelper$PackedColor;color(IIII)I"), index = 3)
    private int overrideProgressBarColorBlue(int originalBlue) {
        if ((drippyOverlayHandler != null) && (drippyOverlayHandler.customProgressBarColor != null)) {
            return drippyOverlayHandler.customProgressBarColor.getBlue();
        }
        return originalBlue;
    }

    private void setOverlayOpacity(float opacity) {
        if (drippyOverlayHandler != null) {
            List<CustomizationItemBase> l = new ArrayList<>();
            l.addAll(drippyOverlayHandler.frontRenderItems);
            l.addAll(drippyOverlayHandler.backgroundRenderItems);
            for (CustomizationItemBase i : l) {
                i.opacity = opacity;
            }
            for (IAnimationRenderer a : drippyOverlayHandler.backgroundAnimations()) {
                a.setOpacity(opacity);
            }
        }
    }

    private void loadFonts() {
        try {
            MixinCache.gameThreadRunnables.add(() -> {
                Object m = ((IMixinReloadListener)((IMixinFontResourceManager)((IMixinMinecraft)Minecraft.getInstance()).getFontManagerDrippy()).getReloadListenerDrippy()).invokePrepareDrippy(Minecraft.getInstance().getResourceManager(), EmptyProfiler.INSTANCE);
                ((IMixinReloadListener)((IMixinFontResourceManager)((IMixinMinecraft)Minecraft.getInstance()).getFontManagerDrippy()).getReloadListenerDrippy()).invokeApplyDrippy(m, Minecraft.getInstance().getResourceManager(), EmptyProfiler.INSTANCE);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleInitOverlay() {

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
            drippyOverlayScreen.width = screenWidth;
            drippyOverlayScreen.height = screenHeight;
            drippyOverlayHandler.onInitPre(new GuiScreenEvent.InitGuiEvent.Pre(drippyOverlayScreen, new ArrayList<>(), (c) -> {}, (c) -> {}));
            drippyOverlayHandler.onButtonsCached(new ButtonCachedEvent(drippyOverlayScreen, new ArrayList<>(), false));
        });
    }

    private void runMenuHandlerTask(Runnable run) {

        boolean gameIntroDisplayed = GameIntroHandler.introDisplayed;
        GameIntroHandler.introDisplayed = true;
        MenuHandlerBase menuHandler = MenuHandlerRegistry.getLastActiveHandler();
        MenuHandlerRegistry.setActiveHandler(DrippyOverlayScreen.class.getName());

        Screen s = Minecraft.getInstance().screen;
        if ((s == null) || !(s instanceof DrippyOverlayScreen)) {
            Minecraft.getInstance().screen = drippyOverlayScreen;
            run.run();
            Minecraft.getInstance().screen = s;
        }

        GameIntroHandler.introDisplayed = gameIntroDisplayed;
        if (menuHandler != null) {
            MenuHandlerRegistry.setActiveHandler(menuHandler.getMenuIdentifier());
        }

    }

}
