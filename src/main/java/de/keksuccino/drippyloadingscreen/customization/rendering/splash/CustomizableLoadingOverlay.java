package de.keksuccino.drippyloadingscreen.customization.rendering.splash;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.items.v2.audio.ACIHandler;
import de.keksuccino.drippyloadingscreen.customization.placeholdervalues.PlaceholderTextValueHelper;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ResourceLoadProgressGui;
import net.minecraft.resources.IAsyncReloader;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Consumer;

public class CustomizableLoadingOverlay extends ResourceLoadProgressGui {

    private IAsyncReloader asyncReloader;
    private Consumer<Optional<Throwable>> completedCallback;
    private boolean reloading;
    private float progress;
    private long fadeOutStart;
    private long fadeInStart;

    protected boolean isUpdated = false;
    protected int lastWidth = 0;
    protected int lastHeight = 0;

    public CustomizableLoadingOverlay(ResourceLoadProgressGui parent) {
        super(Minecraft.getInstance(), getReloadInstance(parent), getOnFinish(parent), getFadeIn(parent));
        this.asyncReloader = getReloadInstance(parent);
        this.completedCallback = getOnFinish(parent);
        this.reloading = getFadeIn(parent);
        this.progress = getCurrentProgress(parent);
        this.fadeOutStart = getFadeOutStart(parent);
        this.fadeInStart = getFadeInStart(parent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {

        Minecraft mc = Minecraft.getInstance();
        SplashCustomizationLayer handler = SplashCustomizationLayer.getInstance();

        ACIHandler.onRenderOverlay(handler);

        int screenWidth = mc.getMainWindow().getScaledWidth();
        int screenHeight = mc.getMainWindow().getScaledHeight();
        long time = Util.milliTime();

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

        //--------------------------------------

        if (this.reloading && (this.asyncReloader.asyncPartDone() || mc.currentScreen != null) && this.fadeInStart == -1L) {
            this.fadeInStart = time;
        }

        float f = this.fadeOutStart > -1L ? (float)(time - this.fadeOutStart) / 1000.0F : -1.0F;
        float f1 = this.fadeInStart > -1L ? (float)(time - this.fadeInStart) / 500.0F : -1.0F;
        if (f >= 1.0F) {
            if (mc.currentScreen != null) {
                if (!DrippyLoadingScreen.isFancyMenuLoaded() && handler.fadeOut) {
                    mc.currentScreen.render(matrices, 0, 0, delta);
                }
            }
        } else if (this.reloading) {
            if (mc.currentScreen != null && f1 < 1.0F) {
                if (!DrippyLoadingScreen.isFancyMenuLoaded() && handler.fadeOut) {
                    mc.currentScreen.render(matrices, mouseX, mouseY, delta);
                }
            }
        }

        float f3 = this.asyncReloader.estimateExecutionSpeed();
        this.progress = MathHelper.clamp(this.progress * 0.95F + f3 * 0.050000012F, 0.0F, 1.0F);

        if (f >= 2.0F) {
            this.resetScale(handler);
            mc.setLoadingGui(null);
        }

        if (this.fadeOutStart == -1L && this.asyncReloader.fullyDone() && (!this.reloading || f1 >= 2.0F)) {
            this.fadeOutStart = Util.milliTime();
            try {
                this.asyncReloader.join();
                this.completedCallback.accept(Optional.empty());
            } catch (Throwable throwable) {
                this.completedCallback.accept(Optional.of(throwable));
            }

            if (mc.currentScreen != null) {
                mc.currentScreen.init(mc, screenWidth, screenHeight);
            }
        }

        //---------------------------------

        //Give all important fields to the handler so elements can use them (only as getter ofc)
        handler.asyncReloader = this.asyncReloader;
        handler.completedCallback = this.completedCallback;
        handler.reloading = this.reloading;
        handler.fadeOutStart = this.fadeOutStart;
        handler.fadeInStart = this.fadeInStart;
        handler.progress = this.progress;

        PlaceholderTextValueHelper.currentLoadingProgressValue = "" + (int)(this.progress * 100.0F);

        //Render the actual loading screen and all customization items
        handler.renderLayer();

    }

    private static void resetScale(SplashCustomizationLayer handler) {
        if (handler.scaled) {

            Minecraft mc = Minecraft.getInstance();
            MainWindow w = mc.getMainWindow();
            int mcScale = w.calcGuiScale(mc.gameSettings.guiScale, mc.getForceUnicodeFont());

            w.setGuiScale((double)mcScale);

            int screenWidth = w.getScaledWidth();
            int screenHeight = w.getScaledHeight();

            mc.currentScreen.init(mc, screenWidth, screenHeight);

            handler.scaled = false;

        }
    }

    private static IAsyncReloader getReloadInstance(ResourceLoadProgressGui from) {
        try {
            Field f = ObfuscationReflectionHelper.findField(ResourceLoadProgressGui.class, "field_212975_c");
            return (IAsyncReloader) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Consumer<Optional<Throwable>> getOnFinish(ResourceLoadProgressGui from) {
        try {
            Field f = ObfuscationReflectionHelper.findField(ResourceLoadProgressGui.class, "field_212976_d");
            return (Consumer<Optional<Throwable>>) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean getFadeIn(ResourceLoadProgressGui from) {
        try {
            Field f = ObfuscationReflectionHelper.findField(ResourceLoadProgressGui.class, "field_212977_e");
            return (boolean) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static float getCurrentProgress(ResourceLoadProgressGui from) {
        try {
            Field f = ObfuscationReflectionHelper.findField(ResourceLoadProgressGui.class, "field_212978_f");
            return (float) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0F;
    }

    private static long getFadeOutStart(ResourceLoadProgressGui from) {
        try {
            Field f = ObfuscationReflectionHelper.findField(ResourceLoadProgressGui.class, "field_212979_g");
            return (long) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    private static long getFadeInStart(ResourceLoadProgressGui from) {
        try {
            Field f = ObfuscationReflectionHelper.findField(ResourceLoadProgressGui.class, "field_212980_h");
            return (long) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

}
