package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Window;
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
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.ARGB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

@Mixin(LoadingOverlay.class)
public class MixinLoadingOverlay {

    @Unique private static final Logger LOGGER_DRIPPY = LogManager.getLogger();

    @Unique private static DrippyOverlayScreen drippyOverlayScreen = null;

    @Unique private int lastScreenWidthDrippy = 0;
    @Unique private int lastScreenHeightDrippy = 0;
    @Unique private float cachedBackgroundOpacityDrippy = 1.0F;
    @Unique private float cachedElementOpacityDrippy = 1.0F;
    @Unique private double cachedOverlayScaleDrippy = 1.0D;
    @Unique private boolean fontsReloadedDrippy = false;

    @Shadow private float currentProgress;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void afterConstructDrippy(Minecraft mc, ReloadInstance reload, Consumer<?> consumer, boolean b, CallbackInfo info) {

        this.setNewOverlayScreenDrippy();
        this.lastScreenWidthDrippy = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        this.lastScreenHeightDrippy = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        this.initOverlayScreenDrippy(false);
        this.setBackgroundOpacityDrippy(1.0F);
        this.setElementsOpacityDrippy(1.0F);
        this.tickOverlayUpdateDrippy();

    }

    @Inject(method = "render", at = @At("RETURN"))
    private void after_render_Drippy(GuiGraphics graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {

        if (this.shouldRenderVanillaDrippy()) return;

        if (!fontsReloadedDrippy) {
            //This makes text rendering work in the game loading screen
            LOGGER_DRIPPY.info("[DRIPPY LOADING SCREEN] Reloading fonts for text rendering..");
            this.loadFontsDrippy();
            fontsReloadedDrippy = true;
        }

        if (!this.fontsReadyDrippy(graphics)) return;

        RenderSystem.enableBlend();

        MixinCache.cachedCurrentLoadingScreenProgress = this.currentProgress;
        this.tickOverlayUpdateDrippy();
        this.setBackgroundOpacityDrippy(this.cachedBackgroundOpacityDrippy);
        this.setElementsOpacityDrippy(DrippyLoadingScreen.getOptions().earlyFadeOutElements.getValue() ? this.cachedElementOpacityDrippy : this.cachedBackgroundOpacityDrippy);
        this.runMenuHandlerTaskDrippy(() -> {

            EventHandler.INSTANCE.postEvent(new ScreenTickEvent.Pre(getDrippyOverlayScreen()));
            getDrippyOverlayScreen().tick();
            EventHandler.INSTANCE.postEvent(new ScreenTickEvent.Post(getDrippyOverlayScreen()));

            //This is to render the overlay in its own scale while still rendering the actual current screen under it in the current screen's scale
            //It's important to calculate the fixed scale BEFORE updating the window GUI scale
            Window window = Minecraft.getInstance().getWindow();
            double guiScale = window.getGuiScale();
            this.setGuiScaleDrippy(this.cachedOverlayScaleDrippy);

            EventHandler.INSTANCE.postEvent(new RenderScreenEvent.Pre(getDrippyOverlayScreen(), graphics, mouseX, mouseY, partial));
            getDrippyOverlayScreen().render(graphics, mouseX, mouseY, partial);
            EventHandler.INSTANCE.postEvent(new RenderScreenEvent.Post(getDrippyOverlayScreen(), graphics, mouseX, mouseY, partial));

            //Reset scale after rendering
            this.setGuiScaleDrippy(guiScale);

        });

    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    private boolean cancelScreenRenderingDrippy(Screen instance, GuiGraphics guiGraphics, int i, int j, float f) {
        return DrippyLoadingScreen.getOptions().fadeOutLoadingScreen.getValue();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"))
    private void beforeCloseOverlayDrippy(GuiGraphics $$0, int $$1, int $$2, float $$3, CallbackInfo ci) {
        EventHandler.INSTANCE.postEvent(new CloseScreenEvent(getDrippyOverlayScreen(), null));
    }

    @Inject(method = "drawProgressBar", at = @At("HEAD"), cancellable = true)
    private void cancelOriginalProgressBarRenderingDrippy(GuiGraphics graphics, int p_96184_, int p_96185_, int p_96186_, int p_96187_, float opacity, CallbackInfo info) {
        if (!this.shouldRenderVanillaDrippy()) {
            info.cancel();
            this.cachedElementOpacityDrippy = DrippyLoadingScreen.getOptions().fadeOutLoadingScreen.getValue() ? Math.min(1.0F, Math.max(0.05F, opacity)) : 1.0F;
        }
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIFFIIIIIII)V"))
    private boolean cancelOriginalLogoRenderingDrippy(GuiGraphics instance, Function<ResourceLocation, RenderType> $$0, ResourceLocation $$1, int $$2, int $$3, float $$4, float $$5, int $$6, int $$7, int $$8, int $$9, int $$10, int $$11, int $$12) {
        return this.shouldRenderVanillaDrippy();
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(Lnet/minecraft/client/renderer/RenderType;IIIII)V"))
    private boolean cancelBackgroundRenderingDrippy(GuiGraphics instance, RenderType renderType, int i, int j, int k, int l, int color) {
        this.cachedBackgroundOpacityDrippy = DrippyLoadingScreen.getOptions().fadeOutLoadingScreen.getValue() ? Math.min(1.0F, Math.max(0.05F, (float) ARGB.alpha(color) / 255.0F)) : 1.0F;
        return this.shouldRenderVanillaDrippy();
    }

    @Unique
    private void setGuiScaleDrippy(double scale) {
        Window window = Minecraft.getInstance().getWindow();
        window.setGuiScale(scale);
        Minecraft.getInstance().gameRenderer.resize(window.getWidth(), window.getHeight());
        RenderSystem.viewport(0, 0, window.getWidth(), window.getHeight());
        Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, (float)((double)window.getWidth() / window.getGuiScale()), (float)((double)window.getHeight() / window.getGuiScale()), 0.0F, 1000.0F, 21000.0F);
        RenderSystem.setProjectionMatrix(matrix4f, ProjectionType.ORTHOGRAPHIC);
        RenderSystem.clear(256);
    }

    @Unique
    private boolean shouldRenderVanillaDrippy() {
        return (getDrippyOverlayScreen() == null) || (this.getLayerDrippy() == null);
    }

    @Unique
    private void setBackgroundOpacityDrippy(float opacity) {
        if (this.getLayerDrippy() == null) return;
        this.getLayerDrippy().backgroundOpacity = opacity;
        getDrippyOverlayScreen().backgroundOpacity = opacity;
    }

    @Unique
    private void setElementsOpacityDrippy(float opacity) {
        if (opacity < 0.02F) {
            opacity = 0.02F;
        }
        if (this.getLayerDrippy() != null) {
            for (AbstractElement i : this.getLayerDrippy().allElements) {
                i.opacity = opacity;
                if (i.opacity <= 0.02F) {
                    i.visible = false;
                }
            }
        }
    }

    @Unique
    @Nullable
    private ScreenCustomizationLayer getLayerDrippy() {
        if (getDrippyOverlayScreen() == null) return null;
        ScreenCustomizationLayer l = ScreenCustomizationLayerHandler.getLayerOfScreen(getDrippyOverlayScreen());
        if (l != null) l.loadEarly = true;
        return l;
    }

    @Unique
    private void loadFontsDrippy() {
        try {
            FontManager fontManager = ((IMixinMinecraft)Minecraft.getInstance()).getFontManagerDrippy();
            fontManager.reload(new PreparableReloadListener.PreparationBarrier() {
                @Override
                public <T> CompletableFuture<T> wait(T t) {
                    return new CompletableFuture<>();
                }
            }, Minecraft.getInstance().getResourceManager(), Minecraft.getInstance(), Util.backgroundExecutor());
        } catch (Exception ex) {
            LOGGER_DRIPPY.error("[DRIPPY LOADING SCREEN] Failed to load fonts!", ex);
        }
    }

    @Unique
    private boolean fontsReadyDrippy(GuiGraphics graphics) {
        try {
            Minecraft.getInstance().getShaderManager().getProgramForLoading(CoreShaders.RENDERTYPE_TEXT);
        } catch (Throwable t) {
            return false;
        }
        return true;
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
            ex.printStackTrace();
        }
    }

    @Unique
    private void setNewOverlayScreenDrippy() {
        drippyOverlayScreen = null;
        ScreenCustomizationLayerHandler.registerScreen(getDrippyOverlayScreen());
        this.getLayerDrippy(); //dummy call to let the method set loadEarly to true
    }

    @Unique
    private void initOverlayScreenDrippy(boolean resize) {
        this.runMenuHandlerTaskDrippy(() -> {
            try {
                double scale = Minecraft.getInstance().getWindow().getGuiScale();
                RenderingUtils.resetGuiScale();
                if (!resize) {
                    EventHandler.INSTANCE.postEvent(new OpenScreenEvent(getDrippyOverlayScreen()));
                }
                getDrippyOverlayScreen().width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
                getDrippyOverlayScreen().height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
                EventHandler.INSTANCE.postEvent(new InitOrResizeScreenStartingEvent(getDrippyOverlayScreen(), resize ? InitOrResizeScreenEvent.InitializationPhase.RESIZE : InitOrResizeScreenEvent.InitializationPhase.INIT));
                EventHandler.INSTANCE.postEvent(new InitOrResizeScreenEvent.Pre(getDrippyOverlayScreen(), resize ? InitOrResizeScreenEvent.InitializationPhase.RESIZE : InitOrResizeScreenEvent.InitializationPhase.INIT));
                getDrippyOverlayScreen().init(Minecraft.getInstance(), getDrippyOverlayScreen().width, getDrippyOverlayScreen().height);
                EventHandler.INSTANCE.postEvent(new InitOrResizeScreenEvent.Post(getDrippyOverlayScreen(), resize ? InitOrResizeScreenEvent.InitializationPhase.RESIZE : InitOrResizeScreenEvent.InitializationPhase.INIT));
                EventHandler.INSTANCE.postEvent(new InitOrResizeScreenCompletedEvent(getDrippyOverlayScreen(), resize ? InitOrResizeScreenEvent.InitializationPhase.RESIZE : InitOrResizeScreenEvent.InitializationPhase.INIT));
                if (!resize) {
                    EventHandler.INSTANCE.postEvent(new OpenScreenPostInitEvent(getDrippyOverlayScreen()));
                }
                this.cachedOverlayScaleDrippy = Minecraft.getInstance().getWindow().getGuiScale();
                MixinCache.cachedLoadingOverlayScale = this.cachedOverlayScaleDrippy;
                Minecraft.getInstance().getWindow().setGuiScale(scale);
            } catch (Exception ex) {
                ex.printStackTrace();
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
                Minecraft.getInstance().screen = getDrippyOverlayScreen();
                run.run();
                Minecraft.getInstance().screen = current;
            }
            ScreenCustomization.setScreenCustomizationEnabled(customizationEnabled);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Unique
    private static DrippyOverlayScreen getDrippyOverlayScreen() {
        if (drippyOverlayScreen == null) {
            LOGGER_DRIPPY.info("[DRIPPY LOADING SCREEN] Creating DrippyOverlayScreen instance..");
            drippyOverlayScreen = new DrippyOverlayScreen();
        }
        return drippyOverlayScreen;
    }

}
