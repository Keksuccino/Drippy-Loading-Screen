package de.keksuccino.drippyloadingscreen.customization.rendering.splash;

import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.placeholdervalues.PlaceholderTextValueHelper;
import de.keksuccino.konkrete.reflection.ReflectionHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Consumer;

public class CustomizableLoadingOverlay extends SplashOverlay {

    private ResourceReload reload;
    private Consumer<Optional<Throwable>> exceptionHandler;
    private boolean reloading;
    private float progress;
    private long reloadCompleteTime;
    private long reloadStartTime;

    protected boolean isUpdated = false;
    protected int lastWidth = 0;
    protected int lastHeight = 0;

    public CustomizableLoadingOverlay(SplashOverlay parent) {
        super(MinecraftClient.getInstance(), getReloadInstance(parent), getOnFinish(parent), getFadeIn(parent));
        this.reload = getReloadInstance(parent);
        this.exceptionHandler = getOnFinish(parent);
        this.reloading = getFadeIn(parent);
        this.progress = getCurrentProgress(parent);
        this.reloadCompleteTime = getFadeOutStart(parent);
        this.reloadStartTime = getFadeInStart(parent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {

        MinecraftClient mc = MinecraftClient.getInstance();
        SplashCustomizationLayer handler = SplashCustomizationLayer.getInstance();

        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();
        long time = Util.getMeasuringTimeMs();

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
            if (mc.currentScreen != null) {
                if (!DrippyLoadingScreen.isFancyMenuLoaded() && handler.fadeOut) {
                    mc.currentScreen.render(matrices, 0, 0, delta);
                }
            }

        } else if (this.reloading) {
            if (mc.currentScreen != null && g < 1.0F) {
                if (!DrippyLoadingScreen.isFancyMenuLoaded() && handler.fadeOut) {
                    mc.currentScreen.render(matrices, mouseX, mouseY, delta);
                }
            }

        }

        float y = this.reload.getProgress();
        this.progress = MathHelper.clamp(this.progress * 0.95F + y * 0.050000012F, 0.0F, 1.0F);

        if (f >= 2.0F) {
            resetScale(handler);
            mc.setOverlay(null);
        }

        if (this.reloadCompleteTime == -1L && this.reload.isComplete() && (!this.reloading || g >= 2.0F)) {
            try {
                this.reload.throwException();
                this.exceptionHandler.accept(Optional.empty());
            } catch (Throwable var23) {
                this.exceptionHandler.accept(Optional.of(var23));
            }

            this.reloadCompleteTime = Util.getMeasuringTimeMs();
            if (mc.currentScreen != null) {
                mc.currentScreen.init(mc, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
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

            MinecraftClient mc = MinecraftClient.getInstance();
            Window w = mc.getWindow();
            int mcScale = w.calculateScaleFactor(mc.options.guiScale, mc.forcesUnicodeFont());

            w.setScaleFactor((double)mcScale);

            int screenWidth = w.getScaledWidth();
            int screenHeight = w.getScaledHeight();

            mc.currentScreen.init(mc, screenWidth, screenHeight);

            handler.scaled = false;

        }
    }

    private static ResourceReload getReloadInstance(SplashOverlay from) {
        try {
            Field f = ReflectionHelper.findField(SplashOverlay.class, "field_17767", "reload");
            return (ResourceReload) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Consumer<Optional<Throwable>> getOnFinish(SplashOverlay from) {
        try {
            Field f = ReflectionHelper.findField(SplashOverlay.class, "field_18218", "exceptionHandler");
            return (Consumer<Optional<Throwable>>) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean getFadeIn(SplashOverlay from) {
        try {
            Field f = ReflectionHelper.findField(SplashOverlay.class, "field_18219", "reloading");
            return (boolean) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static float getCurrentProgress(SplashOverlay from) {
        try {
            Field f = ReflectionHelper.findField(SplashOverlay.class, "field_17770", "progress");
            return (float) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0F;
    }

    private static long getFadeOutStart(SplashOverlay from) {
        try {
            Field f = ReflectionHelper.findField(SplashOverlay.class, "field_17771", "reloadCompleteTime");
            return (long) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    private static long getFadeInStart(SplashOverlay from) {
        try {
            Field f = ReflectionHelper.findField(SplashOverlay.class, "field_18220", "reloadStartTime");
            return (long) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

}
