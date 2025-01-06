//package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;
//
//import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
//import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//import com.mojang.blaze3d.platform.GlStateManager;
//import com.mojang.blaze3d.systems.RenderSystem;
//import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
//import de.keksuccino.drippyloadingscreen.FMAnimationUtils;
//import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
//import de.keksuccino.drippyloadingscreen.mixin.MixinCache;
//import de.keksuccino.fancymenu.customization.ScreenCustomization;
//import de.keksuccino.fancymenu.customization.animation.AnimationHandler;
//import de.keksuccino.fancymenu.customization.element.AbstractElement;
//import de.keksuccino.fancymenu.customization.layer.ScreenCustomizationLayer;
//import de.keksuccino.fancymenu.customization.layer.ScreenCustomizationLayerHandler;
//import de.keksuccino.fancymenu.events.screen.*;
//import de.keksuccino.fancymenu.util.event.acara.EventHandler;
//import de.keksuccino.fancymenu.util.rendering.RenderingUtils;
//import de.keksuccino.fancymenu.util.rendering.ui.UIBase;
//import de.keksuccino.fancymenu.util.threading.MainThreadTaskExecutor;
//import net.minecraft.Util;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.client.gui.font.FontManager;
//import net.minecraft.client.gui.screens.LoadingOverlay;
//import net.minecraft.client.gui.screens.Screen;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.packs.resources.ReloadInstance;
//import net.minecraft.util.Mth;
//import net.minecraft.util.profiling.InactiveProfiler;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.jetbrains.annotations.Nullable;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.*;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import java.util.Optional;
//import java.util.function.Consumer;
//
//@Mixin(LoadingOverlay.class)
//public class MixinLoadingOverlayExperimental {
//
//    @Unique private static final Logger LOGGER_DRIPPY = LogManager.getLogger();
//
//    @Unique private static boolean initializedDrippy = false;
//    @Unique private static DrippyOverlayScreen drippyOverlayScreen = null;
//
//    @Unique private int lastScreenWidthDrippy = 0;
//    @Unique private int lastScreenHeightDrippy = 0;
//    @Unique private float cachedBackgroundOpacityDrippy = 1.0F;
//    @Unique private float cachedElementOpacityDrippy = 1.0F;
//    @Unique private double cachedOverlayScaleDrippy = 1.0D;
//
//    @Shadow private float currentProgress;
//    @Shadow @Final private ReloadInstance reload;
//    @Shadow @Final private Consumer<Optional<Throwable>> onFinish;
//    @Shadow @Final private boolean fadeIn;
//    @Shadow private long fadeInStart;
//    @Shadow private long fadeOutStart;
//    @Shadow @Final private Minecraft minecraft;
//    @Shadow @Final public static ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION;
//
//    @Inject(method = "<init>", at = @At("RETURN"))
//    private void afterConstructDrippy(Minecraft mc, ReloadInstance reload, Consumer<?> consumer, boolean b, CallbackInfo info) {
//
//        if (!initializedDrippy) {
//            //This makes text rendering work in the game loading screen
//            LOGGER_DRIPPY.info("[DRIPPY LOADING SCREEN] Initializing fonts for text rendering..");
//            this.loadFontsDrippy();
//            //Setup FancyMenu animation sizes
//            LOGGER_DRIPPY.info("[DRIPPY LOADING SCREEN] Calculating animation sizes for FancyMenu..");
//            FMAnimationUtils.initAnimationEngine();
//            AnimationHandler.updateAnimationSizes();
//            initializedDrippy = true;
//        }
//
//        this.setNewOverlayScreenDrippy();
//        this.lastScreenWidthDrippy = Minecraft.getInstance().getWindow().getGuiScaledWidth();
//        this.lastScreenHeightDrippy = Minecraft.getInstance().getWindow().getGuiScaledHeight();
//        this.initOverlayScreenDrippy(false);
//        this.setBackgroundOpacityDrippy(1.0F);
//        this.setElementsOpacityDrippy(1.0F);
//        this.tickOverlayUpdateDrippy();
//
//    }
//
//    @WrapMethod(method = "render")
//    private void wrap_render_method_Drippy(GuiGraphics graphics, int mouseX, int mouseY, float partial, Operation<Void> original) {
//
//        if (this.shouldRenderVanillaDrippy()) {
//
//            original.call(graphics, mouseX, mouseY, partial);
//
//        } else {
//
//            long currentMillis = Util.getMillis();
//            float fadeOutAlphaRaw = (this.fadeOutStart > -1L) ? ((float)(currentMillis - this.fadeOutStart) / 1000.0F) : -1.0F;
//            float fadeOutAlpha = 1.0F - Mth.clamp(fadeOutAlphaRaw, 0.0F, 1.0F);
//            float fadeInAlphaRaw = (this.fadeInStart > -1L) ? ((float)(currentMillis - this.fadeInStart) / 500.0F) : -1.0F;
//            float backgroundAlpha = 1.0F;
//
//            if (this.fadeIn && (this.fadeInStart == -1L)) {
//                this.fadeInStart = currentMillis;
//            }
//
//            //Render screen behind loading overlay and handle some fading stuff
//            if (fadeOutAlphaRaw >= 1.0F) {
//                if ((this.minecraft.screen != null) && DrippyLoadingScreen.getOptions().fadeOutLoadingScreen.getValue()) {
//                    this.minecraft.screen.render(graphics, 0, 0, partial);
//                }
//                backgroundAlpha = 1.0F - Mth.clamp(fadeOutAlphaRaw - 1.0F, 0.0F, 1.0F);
//            } else if (this.fadeIn) {
//                if ((this.minecraft.screen != null) && (fadeInAlphaRaw < 1.0F) && DrippyLoadingScreen.getOptions().fadeOutLoadingScreen.getValue()) {
//                    this.minecraft.screen.render(graphics, mouseX, mouseY, partial);
//                }
//                backgroundAlpha = Mth.clamp(fadeInAlphaRaw, 0.15F, 1.0F);
//            } else {
//                GlStateManager._clearColor(1.0F, 1.0F, 1.0F, 1.0F);
//                GlStateManager._clear(16384);
//            }
//
//            //Update opacity stuff for fading
//            this.cachedElementOpacityDrippy = DrippyLoadingScreen.getOptions().fadeOutLoadingScreen.getValue() ? fadeOutAlpha : 1.0F;
//            this.cachedBackgroundOpacityDrippy = DrippyLoadingScreen.getOptions().fadeOutLoadingScreen.getValue() ? backgroundAlpha : 1.0F;
//
//            float actualProgress = this.reload.getActualProgress();
//            this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + actualProgress * 0.050000012F, 0.0F, 1.0F);
//
//            //Render Drippy stuff
//            this.renderDrippyToOverlay(graphics, mouseX, mouseY, partial);
//
//            //Seems to be important to not make the loading screen look glitched, so lets just keep this here..
//            graphics.blit((loc) -> RenderType.mojangLogo(), MOJANG_STUDIOS_LOGO_LOCATION, 0, 0, 0F, 0F, 0, 0, 0, 0, 0, 0, -1);
//
//            RenderSystem.defaultBlendFunc();
//            RenderSystem.disableBlend();
//
//            if (fadeOutAlphaRaw >= 2.0F) {
//                EventHandler.INSTANCE.postEvent(new CloseScreenEvent(getDrippyOverlayScreen()));
//                this.minecraft.setOverlay(null);
//            }
//
//            if ((this.fadeOutStart == -1L) && this.reload.isDone() && (!this.fadeIn || (fadeInAlphaRaw >= 2.0F))) {
//
//                try {
//                    this.reload.checkExceptions();
//                    this.onFinish.accept(Optional.empty());
//                } catch (Throwable throwable) {
//                    this.onFinish.accept(Optional.of(throwable));
//                }
//
//                this.fadeOutStart = Util.getMillis();
//
//                //Init screen behind loading overlay
//                if (this.minecraft.screen != null) {
//                    this.minecraft.screen.init(this.minecraft, graphics.guiWidth(), graphics.guiHeight());
//                }
//
//            }
//
//        }
//
//    }
//
//    @Unique
//    private void renderDrippyToOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
//
//        if (this.shouldRenderVanillaDrippy()) return;
//
//        MixinCache.cachedCurrentLoadingScreenProgress = this.currentProgress;
//        this.tickOverlayUpdateDrippy();
//        this.setBackgroundOpacityDrippy(this.cachedBackgroundOpacityDrippy);
//        this.setElementsOpacityDrippy(DrippyLoadingScreen.getOptions().earlyFadeOutElements.getValue() ? this.cachedElementOpacityDrippy : this.cachedBackgroundOpacityDrippy);
//        this.runMenuHandlerTaskDrippy(() -> {
//
//            EventHandler.INSTANCE.postEvent(new ScreenTickEvent.Pre(getDrippyOverlayScreen()));
//            getDrippyOverlayScreen().tick();
//            EventHandler.INSTANCE.postEvent(new ScreenTickEvent.Post(getDrippyOverlayScreen()));
//
////            this.restoreRenderDefaultsDrippy(graphics);
//
//            //This is to render the overlay in its own scale while still rendering the actual current screen under it in the current screen's scale
//            //It's important to calculate the fixed scale BEFORE updating the window GUI scale
//            float renderScale = UIBase.calculateFixedScale((float)this.cachedOverlayScaleDrippy);
//            double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
//            Minecraft.getInstance().getWindow().setGuiScale(this.cachedOverlayScaleDrippy);
//            graphics.pose().pushPose();
//            graphics.pose().scale(renderScale, renderScale, renderScale);
//
//            EventHandler.INSTANCE.postEvent(new RenderScreenEvent.Pre(getDrippyOverlayScreen(), graphics, mouseX, mouseY, partial));
//            getDrippyOverlayScreen().render(graphics, mouseX, mouseY, partial);
////            this.restoreRenderDefaultsDrippy(graphics);
//            EventHandler.INSTANCE.postEvent(new RenderScreenEvent.Post(getDrippyOverlayScreen(), graphics, mouseX, mouseY, partial));
//
//            //Reset scale after rendering
//            graphics.pose().scale(1.0F, 1.0F, 1.0F);
//            graphics.pose().popPose();
//            Minecraft.getInstance().getWindow().setGuiScale(guiScale);
//
////            this.restoreRenderDefaultsDrippy(graphics);
//
//        });
//
//    }
//
//    @Unique
//    private boolean shouldRenderVanillaDrippy() {
//        return (getDrippyOverlayScreen() == null) || (this.getLayerDrippy() == null);
//    }
//
//    @Unique
//    private void setBackgroundOpacityDrippy(float opacity) {
//        if (this.getLayerDrippy() == null) return;
//        this.getLayerDrippy().backgroundOpacity = opacity;
//        getDrippyOverlayScreen().backgroundOpacity = opacity;
//    }
//
//    @Unique
//    private void setElementsOpacityDrippy(float opacity) {
//        if (opacity < 0.02F) {
//            opacity = 0.02F;
//        }
//        if (this.getLayerDrippy() != null) {
//            for (AbstractElement i : this.getLayerDrippy().allElements) {
//                i.opacity = opacity;
//                if (i.opacity <= 0.02F) {
//                    i.visible = false;
//                }
//            }
//        }
//    }
//
//    @Unique
//    @Nullable
//    private ScreenCustomizationLayer getLayerDrippy() {
//        if (getDrippyOverlayScreen() == null) return null;
//        ScreenCustomizationLayer l = ScreenCustomizationLayerHandler.getLayerOfScreen(getDrippyOverlayScreen());
//        if (l != null) l.loadEarly = true;
//        return l;
//    }
//
//    @Unique
//    private void loadFontsDrippy() {
//        MainThreadTaskExecutor.executeInMainThread(() -> {
//            try {
//                FontManager fontManager = ((IMixinMinecraft)Minecraft.getInstance()).getFontManagerDrippy();
//                fontManager.apply(fontManager.prepare(Minecraft.getInstance().getResourceManager(), Util.backgroundExecutor()).get(), InactiveProfiler.INSTANCE);
//            } catch (Exception ex) {
//                LOGGER_DRIPPY.error("[DRIPPY LOADING SCREEN] Failed to load fonts!", ex);
//            }
//        }, MainThreadTaskExecutor.ExecuteTiming.POST_CLIENT_TICK);
//    }
//
//    @Unique
//    private void tickOverlayUpdateDrippy() {
//        try {
//            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
//            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
//            //Re-init overlay on window size change
//            if ((screenWidth != this.lastScreenWidthDrippy) || (screenHeight != this.lastScreenHeightDrippy)) {
//                this.initOverlayScreenDrippy(true);
//            }
//            this.lastScreenWidthDrippy = screenWidth;
//            this.lastScreenHeightDrippy = screenHeight;
//        } catch (Exception ex) {
//            LOGGER_DRIPPY.error("[DRIPPY LOADING SCREEN] Error while ticking overlay!", ex);
//        }
//    }
//
//    @Unique
//    private void setNewOverlayScreenDrippy() {
//        drippyOverlayScreen = null;
//        ScreenCustomizationLayerHandler.registerScreen(getDrippyOverlayScreen());
//        this.getLayerDrippy(); //dummy call to let the method set loadEarly to true
//    }
//
//    @Unique
//    private void initOverlayScreenDrippy(boolean resize) {
//        this.runMenuHandlerTaskDrippy(() -> {
//            try {
//                double scale = Minecraft.getInstance().getWindow().getGuiScale();
//                RenderingUtils.resetGuiScale();
//                if (!resize) {
//                    EventHandler.INSTANCE.postEvent(new OpenScreenEvent(getDrippyOverlayScreen()));
//                }
//                getDrippyOverlayScreen().width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
//                getDrippyOverlayScreen().height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
//                EventHandler.INSTANCE.postEvent(new InitOrResizeScreenStartingEvent(getDrippyOverlayScreen(), resize ? InitOrResizeScreenEvent.InitializationPhase.RESIZE : InitOrResizeScreenEvent.InitializationPhase.INIT));
//                EventHandler.INSTANCE.postEvent(new InitOrResizeScreenEvent.Pre(getDrippyOverlayScreen(), resize ? InitOrResizeScreenEvent.InitializationPhase.RESIZE : InitOrResizeScreenEvent.InitializationPhase.INIT));
//                getDrippyOverlayScreen().init(Minecraft.getInstance(), getDrippyOverlayScreen().width, getDrippyOverlayScreen().height);
//                EventHandler.INSTANCE.postEvent(new InitOrResizeScreenEvent.Post(getDrippyOverlayScreen(), resize ? InitOrResizeScreenEvent.InitializationPhase.RESIZE : InitOrResizeScreenEvent.InitializationPhase.INIT));
//                EventHandler.INSTANCE.postEvent(new InitOrResizeScreenCompletedEvent(getDrippyOverlayScreen(), resize ? InitOrResizeScreenEvent.InitializationPhase.RESIZE : InitOrResizeScreenEvent.InitializationPhase.INIT));
//                if (!resize) {
//                    EventHandler.INSTANCE.postEvent(new OpenScreenPostInitEvent(getDrippyOverlayScreen()));
//                }
//                this.cachedOverlayScaleDrippy = Minecraft.getInstance().getWindow().getGuiScale();
//                MixinCache.cachedLoadingOverlayScale = this.cachedOverlayScaleDrippy;
//                Minecraft.getInstance().getWindow().setGuiScale(scale);
//            } catch (Exception ex) {
//                LOGGER_DRIPPY.error("[DRIPPY LOADING SCREEN] Error while trying to init overlay screen!", ex);
//            }
//        });
//    }
//
//    @Unique
//    private void runMenuHandlerTaskDrippy(Runnable run) {
//        try {
//            boolean customizationEnabled = ScreenCustomization.isScreenCustomizationEnabled();
//            ScreenCustomization.setScreenCustomizationEnabled(true);
//            Screen current = Minecraft.getInstance().screen;
//            if (!(current instanceof DrippyOverlayScreen)) {
//                Minecraft.getInstance().screen = getDrippyOverlayScreen();
//                run.run();
//                Minecraft.getInstance().screen = current;
//            }
//            ScreenCustomization.setScreenCustomizationEnabled(customizationEnabled);
//        } catch (Exception ex) {
//            LOGGER_DRIPPY.error("[DRIPPY LOADING SCREEN] Error while trying to run menu handler task!", ex);
//        }
//    }
//
//    @Unique
//    private static DrippyOverlayScreen getDrippyOverlayScreen() {
//        if (drippyOverlayScreen == null) {
//            LOGGER_DRIPPY.info("[DRIPPY LOADING SCREEN] Creating DrippyOverlayScreen instance..");
//            drippyOverlayScreen = new DrippyOverlayScreen();
//        }
//        return drippyOverlayScreen;
//    }
//
//}
