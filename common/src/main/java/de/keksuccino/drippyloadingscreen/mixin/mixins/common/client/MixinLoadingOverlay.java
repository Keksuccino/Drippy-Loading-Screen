package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.drippyloadingscreen.mixin.MixinCache;
import de.keksuccino.fancymenu.customization.ScreenCustomization;
import de.keksuccino.fancymenu.customization.element.AbstractElement;
import de.keksuccino.fancymenu.customization.layer.ScreenCustomizationLayer;
import de.keksuccino.fancymenu.customization.layer.ScreenCustomizationLayerHandler;
import de.keksuccino.fancymenu.events.screen.*;
import de.keksuccino.fancymenu.util.event.acara.EventHandler;
import de.keksuccino.fancymenu.util.rendering.RenderingUtils;
import de.keksuccino.fancymenu.util.rendering.ui.UIBase;
import de.keksuccino.fancymenu.util.threading.MainThreadTaskExecutor;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.InactiveProfiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Objects;
import java.util.function.Consumer;

@Mixin(LoadingOverlay.class)
public class MixinLoadingOverlay {

    @Unique private static final Logger LOGGER_DRIPPY = LogManager.getLogger();

    @Unique private static boolean initializedDrippy = false;
    @Unique private static DrippyOverlayScreen drippyOverlayScreen = new DrippyOverlayScreen();

    @Unique private int lastScreenWidthDrippy = 0;
    @Unique private int lastScreenHeightDrippy = 0;
    @Unique private float cachedBackgroundOpacityDrippy = 1.0F;
    @Unique private float cachedElementOpacityDrippy = 1.0F;
    @Unique private double cachedOverlayScaleDrippy = 1.0D;

    @Shadow private float currentProgress;
    @Shadow private long fadeInStart;
    @Shadow @Final private boolean fadeIn;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void afterConstructDrippy(Minecraft mc, ReloadInstance reload, Consumer<?> consumer, boolean b, CallbackInfo info) {

        if (!initializedDrippy) {
            //This makes text rendering work in the game loading screen
            LOGGER_DRIPPY.info("[DRIPPY LOADING SCREEN] Initializing fonts for text rendering..");
            this.loadFontsDrippy();
            initializedDrippy = true;
        }

        this.setNewOverlayScreenDrippy();
        this.lastScreenWidthDrippy = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        this.lastScreenHeightDrippy = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        this.initOverlayScreenDrippy(false);
        this.setBackgroundOpacityDrippy(1.0F);
        this.setElementsOpacityDrippy(1.0F);
        this.tickOverlayUpdateDrippy();

    }

    @Inject(method = "render", at = @At("RETURN"))
    private void afterRenderDrippy(GuiGraphics graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {

        if (this.shouldRenderVanillaDrippy()) return;

        MixinCache.cachedCurrentLoadingScreenProgress = this.currentProgress;
        this.tickOverlayUpdateDrippy();
        this.updateFadeInOpacityCacheDrippy();
        this.setBackgroundOpacityDrippy(this.cachedBackgroundOpacityDrippy);
        this.setElementsOpacityDrippy(DrippyLoadingScreen.getOptions().earlyFadeOutElements.getValue() ? this.cachedElementOpacityDrippy : this.cachedBackgroundOpacityDrippy);
        this.runMenuHandlerTaskDrippy(() -> {

            EventHandler.INSTANCE.postEvent(new ScreenTickEvent.Pre(drippyOverlayScreen));
            drippyOverlayScreen.tick();
            EventHandler.INSTANCE.postEvent(new ScreenTickEvent.Post(drippyOverlayScreen));

            this.restoreRenderDefaultsDrippy(graphics);

            //This is to render the overlay in its own scale while still rendering the actual current screen under it in the current screen's scale
            //It's important to calculate the fixed scale BEFORE updating the window GUI scale
            float renderScale = UIBase.calculateFixedScale((float)this.cachedOverlayScaleDrippy);
            double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
            Minecraft.getInstance().getWindow().setGuiScale(this.cachedOverlayScaleDrippy);
            graphics.pose().pushPose();
            graphics.pose().scale(renderScale, renderScale, renderScale);

            EventHandler.INSTANCE.postEvent(new RenderScreenEvent.Pre(drippyOverlayScreen, graphics, mouseX, mouseY, partial));
            drippyOverlayScreen.render(graphics, mouseX, mouseY, partial);
            this.restoreRenderDefaultsDrippy(graphics);
            EventHandler.INSTANCE.postEvent(new RenderScreenEvent.Post(drippyOverlayScreen, graphics, mouseX, mouseY, partial));

            //Reset scale after rendering
            graphics.pose().scale(1.0F, 1.0F, 1.0F);
            graphics.pose().popPose();
            Minecraft.getInstance().getWindow().setGuiScale(guiScale);

            this.restoreRenderDefaultsDrippy(graphics);

        });

    }

    /**
     * @reason This replaces the outdated render() call with the new renderWithTooltip() call that FancyMenu hooks into for rendering events.
     */
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    private void wrap_Screen_render_in_render_Drippy(Screen instance, GuiGraphics graphics, int mouseX, int mouseY, float partial, Operation<Void> original) {
        if (!DrippyLoadingScreen.getOptions().fadeInOutLoadingScreen.getValue()) return;
        RenderingUtils.setTooltipRenderingBlocked(true);
        // Keep depth test disabled during the whole screen rendering to make stuff from the screen not shine through the loading screen
        RenderSystem.disableDepthTest();
        RenderingUtils.setDepthTestLocked(true);
        Objects.requireNonNull(Minecraft.getInstance().screen).renderWithTooltip(graphics, mouseX, mouseY, partial);
        RenderingUtils.setDepthTestLocked(false);
        RenderSystem.enableDepthTest();
        RenderingUtils.setTooltipRenderingBlocked(false);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"))
    private void beforeCloseOverlayDrippy(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo info) {
        EventHandler.INSTANCE.postEvent(new CloseScreenEvent(drippyOverlayScreen, null));
    }

    @Inject(method = "drawProgressBar", at = @At("HEAD"), cancellable = true)
    private void cancelOriginalProgressBarRenderingDrippy(GuiGraphics graphics, int minX, int minY, int maxX, int maxY, float opacity, CallbackInfo info) {
        if (!this.shouldRenderVanillaDrippy()) {
            info.cancel();
            this.cachedElementOpacityDrippy = DrippyLoadingScreen.getOptions().fadeInOutLoadingScreen.getValue() ? opacity : 1.0F;
            RenderingUtils.resetShaderColor(graphics);
        }
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V"))
    private boolean cancelOriginalLogoRenderingDrippy(GuiGraphics instance, ResourceLocation atlasLocation, int x, int y, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        return this.shouldRenderVanillaDrippy();
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(Lnet/minecraft/client/renderer/RenderType;IIIII)V"))
    private boolean cancelBackgroundRenderingDrippy(GuiGraphics instance, RenderType renderType, int minX, int minY, int maxX, int maxY, int color) {
        this.cachedBackgroundOpacityDrippy = DrippyLoadingScreen.getOptions().fadeInOutLoadingScreen.getValue() ? Math.min(1.0F, Math.max(0.0F, (float)FastColor.ARGB32.alpha(color) / 255.0F)) : 1.0F;
        return this.shouldRenderVanillaDrippy();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_clear(IZ)V", shift = At.Shift.AFTER, remap = false))
    private void clearColorAfterBackgroundRenderingDrippy(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo info) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderingUtils.resetShaderColor(graphics);
    }

    @Unique
    private void restoreRenderDefaultsDrippy(GuiGraphics graphics) {
        RenderingUtils.resetShaderColor(graphics);
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
    }

    @Unique
    private boolean shouldRenderVanillaDrippy() {
        return (drippyOverlayScreen == null) || (this.getLayerDrippy() == null);
    }

    @Unique
    private void setBackgroundOpacityDrippy(float opacity) {
        if (this.getLayerDrippy() == null) return;
        this.getLayerDrippy().backgroundOpacity = opacity;
        drippyOverlayScreen.backgroundOpacity = opacity;
    }

    @Unique
    private void setElementsOpacityDrippy(float opacity) {
        if (opacity < 0.02F) {
            opacity = 0.02F;
        }
        boolean shouldBeVisible = opacity > 0.02F;
        if (this.getLayerDrippy() != null) {
            for (AbstractElement i : this.getLayerDrippy().allElements) {
                i.opacity = opacity;
                i.visible = shouldBeVisible;
            }
        }
    }

    @Unique
    @Nullable
    private ScreenCustomizationLayer getLayerDrippy() {
        if (drippyOverlayScreen == null) return null;
        ScreenCustomizationLayer l = ScreenCustomizationLayerHandler.getLayerOfScreen(drippyOverlayScreen);
        if (l != null) l.loadEarly = true;
        return l;
    }

    @Unique
    private void loadFontsDrippy() {
        MainThreadTaskExecutor.executeInMainThread(() -> {
            try {
                FontManager fontManager = ((IMixinMinecraft)Minecraft.getInstance()).getFontManagerDrippy();
                fontManager.apply(fontManager.prepare(Minecraft.getInstance().getResourceManager(), Util.backgroundExecutor()).get(), InactiveProfiler.INSTANCE);
            } catch (Exception ex) {
                LOGGER_DRIPPY.error("[DRIPPY LOADING SCREEN] Failed to load fonts!", ex);
            }
        }, MainThreadTaskExecutor.ExecuteTiming.POST_CLIENT_TICK);
    }

    @Unique
    private void tickOverlayUpdateDrippy() {
        try {
            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            //Re-init overlay on window size change
            if ((screenWidth != this.lastScreenWidthDrippy) || (screenHeight != this.lastScreenHeightDrippy)) {
                this.initOverlayScreenDrippy(true);
            }
            this.lastScreenWidthDrippy = screenWidth;
            this.lastScreenHeightDrippy = screenHeight;
        } catch (Exception ex) {
            LOGGER_DRIPPY.error("[DRIPPY LOADING SCREEN] Error while updating overlay!", ex);
        }
    }

    @Unique
    private void updateFadeInOpacityCacheDrippy() {
        if (!DrippyLoadingScreen.getOptions().fadeInOutLoadingScreen.getValue()) {
            return;
        }
        float fadeInOpacity = this.getFadeInOpacityDrippy();
        if (fadeInOpacity >= 0.0F && fadeInOpacity < 1.0F) {
            this.cachedBackgroundOpacityDrippy = fadeInOpacity;
            this.cachedElementOpacityDrippy = fadeInOpacity;
        }
    }

    @Unique
    private float getFadeInOpacityDrippy() {
        if (!this.fadeIn) {
            return -1.0F;
        }
        if (this.fadeInStart < 0L) {
            return -1.0F;
        }
        long now = Util.getMillis();
        float progress = (float)(now - this.fadeInStart) / (float)LoadingOverlay.FADE_IN_TIME;
        return Mth.clamp(progress, 0.02F, 1.0F);
    }

    @Unique
    private void setNewOverlayScreenDrippy() {
        drippyOverlayScreen = new DrippyOverlayScreen();
        ScreenCustomizationLayerHandler.registerScreen(drippyOverlayScreen);
        this.getLayerDrippy(); //dummy call to let the method set loadEarly to true
    }

    @Unique
    private void initOverlayScreenDrippy(boolean resize) {
        this.runMenuHandlerTaskDrippy(() -> {
            try {
                double scale = Minecraft.getInstance().getWindow().getGuiScale();
                RenderingUtils.resetGuiScale();
                if (!resize) {
                    EventHandler.INSTANCE.postEvent(new OpenScreenEvent(drippyOverlayScreen));
                }
                drippyOverlayScreen.width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
                drippyOverlayScreen.height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
                EventHandler.INSTANCE.postEvent(new InitOrResizeScreenStartingEvent(drippyOverlayScreen, resize ? InitOrResizeScreenEvent.InitializationPhase.RESIZE : InitOrResizeScreenEvent.InitializationPhase.INIT));
                EventHandler.INSTANCE.postEvent(new InitOrResizeScreenEvent.Pre(drippyOverlayScreen, resize ? InitOrResizeScreenEvent.InitializationPhase.RESIZE : InitOrResizeScreenEvent.InitializationPhase.INIT));
                drippyOverlayScreen.init(Minecraft.getInstance(), drippyOverlayScreen.width, drippyOverlayScreen.height);
                EventHandler.INSTANCE.postEvent(new InitOrResizeScreenEvent.Post(drippyOverlayScreen, resize ? InitOrResizeScreenEvent.InitializationPhase.RESIZE : InitOrResizeScreenEvent.InitializationPhase.INIT));
                EventHandler.INSTANCE.postEvent(new InitOrResizeScreenCompletedEvent(drippyOverlayScreen, resize ? InitOrResizeScreenEvent.InitializationPhase.RESIZE : InitOrResizeScreenEvent.InitializationPhase.INIT));
                if (!resize) {
                    EventHandler.INSTANCE.postEvent(new OpenScreenPostInitEvent(drippyOverlayScreen));
                }
                this.cachedOverlayScaleDrippy = Minecraft.getInstance().getWindow().getGuiScale();
                MixinCache.cachedLoadingOverlayScale = this.cachedOverlayScaleDrippy;
                Minecraft.getInstance().getWindow().setGuiScale(scale);
            } catch (Exception ex) {
                LOGGER_DRIPPY.error("[DRIPPY LOADING SCREEN] Error while initializing Drippy's overlay screen!", ex);
            }
        });
    }

    @Unique
    private void runMenuHandlerTaskDrippy(Runnable run) {
        try {
            boolean customizationEnabled = ScreenCustomization.isScreenCustomizationEnabled();
            ScreenCustomization.setScreenCustomizationEnabled(true);
            Screen current = Minecraft.getInstance().screen;
            if (!(current instanceof DrippyOverlayScreen)) {
                Minecraft.getInstance().screen = drippyOverlayScreen;
                run.run();
                Minecraft.getInstance().screen = current;
            }
            ScreenCustomization.setScreenCustomizationEnabled(customizationEnabled);
        } catch (Exception ex) {
            LOGGER_DRIPPY.error("[DRIPPY LOADING SCREEN] Error while running menu handler task!", ex);
        }
    }

}
