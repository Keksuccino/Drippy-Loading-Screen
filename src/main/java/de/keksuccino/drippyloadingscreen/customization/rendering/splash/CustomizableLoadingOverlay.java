package de.keksuccino.drippyloadingscreen.customization.rendering.splash;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.placeholdervalues.PlaceholderTextValueHelper;
import de.keksuccino.drippyloadingscreen.mixin.client.IMixinLoadingOverlay;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;

public class CustomizableLoadingOverlay extends LoadingOverlay {

    private ReloadInstance reload;
    private Consumer<Optional<Throwable>> exceptionHandler;
    private boolean reloading;
    private float progress;
    private long reloadCompleteTime;
    private long reloadStartTime;

    protected boolean isUpdated = false;
    protected int lastWidth = 0;
    protected int lastHeight = 0;

    public CustomizableLoadingOverlay(LoadingOverlay parent) {
        super(Minecraft.getInstance(), getReloadInstance(parent), getOnFinish(parent), getFadeIn(parent));
        this.reload = getReloadInstance(parent);
        this.exceptionHandler = getOnFinish(parent);
        this.reloading = getFadeIn(parent);
        this.progress = getCurrentProgress(parent);
        this.reloadCompleteTime = getFadeOutStart(parent);
        this.reloadStartTime = getFadeInStart(parent);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {

        Minecraft mc = Minecraft.getInstance();
        SplashCustomizationLayer handler = SplashCustomizationLayer.getInstance();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        long time = Util.getMillis();

        //Handle customization update on window resize
        if ((lastWidth != screenWidth) || (lastHeight != screenHeight)) {
            isUpdated = false;
        }
        lastWidth = screenWidth;
        lastHeight = screenHeight;
        if (!isUpdated) {
            handler.updateCustomizations();
            isUpdated = true;
        }

        //-------------------------------------

        if (this.reloading && this.reloadStartTime == -1L) {
            this.reloadStartTime = time;
        }

        float f = this.reloadCompleteTime > -1L ? (float)(time - this.reloadCompleteTime) / 1000.0F : -1.0F;
        float g = this.reloadStartTime > -1L ? (float)(time - this.reloadStartTime) / 500.0F : -1.0F;
        if (f >= 1.0F) {
            if (mc.screen != null) {
                if (!DrippyLoadingScreen.isFancyMenuLoaded() && handler.fadeOut) {
                    mc.screen.render(matrices, 0, 0, delta);
                }
            }

        } else if (this.reloading) {
            if (mc.screen != null && g < 1.0F) {
                if (!DrippyLoadingScreen.isFancyMenuLoaded() && handler.fadeOut) {
                    mc.screen.render(matrices, mouseX, mouseY, delta);
                }
            }

        }

        float y = this.reload.getActualProgress();
        this.progress = Mth.clamp(this.progress * 0.95F + y * 0.050000012F, 0.0F, 1.0F);

        if (f >= 2.0F) {
            resetScale(handler);
            mc.setOverlay(null);
        }

        if (this.reloadCompleteTime == -1L && this.reload.isDone() && (!this.reloading || g >= 2.0F)) {
            try {
                this.reload.checkExceptions();
                this.exceptionHandler.accept(Optional.empty());
            } catch (Throwable var23) {
                this.exceptionHandler.accept(Optional.of(var23));
            }

            this.reloadCompleteTime = Util.getMillis();
            if (mc.screen != null) {
                mc.screen.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
            }
        }

        //---------------------------------

        //Give all important fields to the handler so elements can use them (only as getter ofc)
        handler.reload = this.reload;
        handler.exceptionHandler = this.exceptionHandler;
        handler.reloading = this.reloading;
        handler.progress = this.progress;
        handler.reloadCompleteTime = this.reloadCompleteTime;
        handler.reloadStartTime = this.reloadStartTime;

        PlaceholderTextValueHelper.currentLoadingProgressValue = "" + (int)(this.progress * 100.0F);

        //Render the actual loading screen and all customization items
        handler.renderLayer();

    }

    private static void resetScale(SplashCustomizationLayer handler) {
        if (handler.scaled) {

            Minecraft mc = Minecraft.getInstance();
            Window w = mc.getWindow();
            int mcScale = w.calculateScale(mc.options.guiScale, mc.isEnforceUnicode());

            w.setGuiScale((double)mcScale);

            int screenWidth = w.getGuiScaledWidth();
            int screenHeight = w.getGuiScaledHeight();

            mc.screen.init(mc, screenWidth, screenHeight);

            handler.scaled = false;

        }
    }

    private static ReloadInstance getReloadInstance(LoadingOverlay from) {
        return ((IMixinLoadingOverlay)from).getReloadDrippy();
    }

    private static Consumer<Optional<Throwable>> getOnFinish(LoadingOverlay from) {
        return ((IMixinLoadingOverlay)from).getOnFinishDrippy();
    }

    private static boolean getFadeIn(LoadingOverlay from) {
        return ((IMixinLoadingOverlay)from).getFadeInDrippy();
    }

    private static float getCurrentProgress(LoadingOverlay from) {
        return ((IMixinLoadingOverlay)from).getCurrentProgressDrippy();
    }

    private static long getFadeOutStart(LoadingOverlay from) {
        return ((IMixinLoadingOverlay)from).getFadeOutStartDrippy();
    }

    private static long getFadeInStart(LoadingOverlay from) {
        return ((IMixinLoadingOverlay)from).getFadeInStartDrippy();
    }

}
