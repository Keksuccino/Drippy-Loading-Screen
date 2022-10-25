package de.keksuccino.drippyloadingscreen.customization.rendering.splash;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.items.v2.audio.ACIHandler;
import de.keksuccino.drippyloadingscreen.customization.placeholdervalues.PlaceholderTextValueHelper;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Consumer;

public class CustomizableLoadingOverlay extends LoadingOverlay {

    private ReloadInstance reload;
    private Consumer<Optional<Throwable>> onFinish;
    private boolean fadeIn;
    private float currentProgress;
    private long fadeOutStart;
    private long fadeInStart;

    protected boolean isUpdated = false;
    protected int lastWidth = 0;
    protected int lastHeight = 0;

    public CustomizableLoadingOverlay(LoadingOverlay parent) {
        super(Minecraft.getInstance(), getReloadInstance(parent), getOnFinish(parent), getFadeIn(parent));
        this.reload = getReloadInstance(parent);
        this.onFinish = getOnFinish(parent);
        this.fadeIn = getFadeIn(parent);
        this.currentProgress = getCurrentProgress(parent);
        this.fadeOutStart = getFadeOutStart(parent);
        this.fadeInStart = getFadeInStart(parent);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {

        Minecraft mc = Minecraft.getInstance();
        SplashCustomizationLayer handler = SplashCustomizationLayer.getInstance();

        //TODO übernehmen (if)
        if (DrippyLoadingScreen.isAuudioLoaded()) {
            ACIHandler.onRenderOverlay(handler);
        }

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

        if (this.fadeIn && this.fadeInStart == -1L) {
            this.fadeInStart = time;
        }

        float f = this.fadeOutStart > -1L ? (float)(time - this.fadeOutStart) / 1000.0F : -1.0F;
        float g = this.fadeInStart > -1L ? (float)(time - this.fadeInStart) / 500.0F : -1.0F;
        if (f >= 1.0F) {
            if (mc.screen != null) {
                if (!DrippyLoadingScreen.isFancyMenuLoaded() && handler.fadeOut) {
                    mc.screen.render(matrices, 0, 0, delta);
                }
            }

        } else if (this.fadeIn) {
            if (mc.screen != null && g < 1.0F) {
                if (!DrippyLoadingScreen.isFancyMenuLoaded() && handler.fadeOut) {
                    mc.screen.render(matrices, mouseX, mouseY, delta);
                }
            }

        }

        float y = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + y * 0.050000012F, 0.0F, 1.0F);

        if (f >= 2.0F) {
            this.resetScale(handler);
            mc.setOverlay(null);
        }

        if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || g >= 2.0F)) {
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable var23) {
                this.onFinish.accept(Optional.of(var23));
            }

            this.fadeOutStart = Util.getMillis();
            if (mc.screen != null) {
                mc.screen.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
            }
        }

        //---------------------------------

        //Give all important fields to the handler so elements can use them (only as getter ofc)
        handler.reload = this.reload;
        handler.onFinish = this.onFinish;
        handler.fadeIn = this.fadeIn;
        handler.currentProgress = this.currentProgress;
        handler.fadeOutStart = this.fadeOutStart;
        handler.fadeInStart = this.fadeInStart;

        PlaceholderTextValueHelper.currentLoadingProgressValue = "" + (int)(this.currentProgress * 100.0F);

        //Render the actual loading screen and all customization items
        handler.renderLayer();

    }

    private static void resetScale(SplashCustomizationLayer handler) {
        if (handler.scaled) {

            Minecraft mc = Minecraft.getInstance();
            Window w = mc.getWindow();
            int mcScale = w.calculateScale(mc.options.guiScale().get(), mc.isEnforceUnicode());

            w.setGuiScale((double)mcScale);

            int screenWidth = w.getGuiScaledWidth();
            int screenHeight = w.getGuiScaledHeight();

            if (mc.screen != null) {
                mc.screen.init(mc, screenWidth, screenHeight);
            }

            handler.scaled = false;

        }
    }

    private static ReloadInstance getReloadInstance(LoadingOverlay from) {
        try {
            Field f = ObfuscationReflectionHelper.findField(LoadingOverlay.class, "f_96164_");
            return (ReloadInstance) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Consumer<Optional<Throwable>> getOnFinish(LoadingOverlay from) {
        try {
            Field f = ObfuscationReflectionHelper.findField(LoadingOverlay.class, "f_96165_");
            return (Consumer<Optional<Throwable>>) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean getFadeIn(LoadingOverlay from) {
        try {
            Field f = ObfuscationReflectionHelper.findField(LoadingOverlay.class, "f_96166_");
            return (boolean) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static float getCurrentProgress(LoadingOverlay from) {
        try {
            Field f = ObfuscationReflectionHelper.findField(LoadingOverlay.class, "f_96167_");
            return (float) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0F;
    }

    private static long getFadeOutStart(LoadingOverlay from) {
        try {
            Field f = ObfuscationReflectionHelper.findField(LoadingOverlay.class, "f_96168_");
            return (long) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    private static long getFadeInStart(LoadingOverlay from) {
        try {
            Field f = ObfuscationReflectionHelper.findField(LoadingOverlay.class, "f_96169_");
            return (long) f.get(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

}
