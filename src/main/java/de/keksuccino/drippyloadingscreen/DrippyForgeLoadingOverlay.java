package de.keksuccino.drippyloadingscreen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.drippyloadingscreen.mixin.mixins.client.IMixinLoadingOverlay;
import de.keksuccino.fancymenu.events.SoftMenuReloadEvent;
import de.keksuccino.fancymenu.menu.animation.AnimationHandler;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerBase;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerRegistry;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.custom.MainMenuHandler;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.loading.ForgeLoadingOverlay;
import net.minecraftforge.fml.earlydisplay.DisplayWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;

import static com.mojang.blaze3d.platform.GlConst.GL_COLOR_BUFFER_BIT;

@OnlyIn(Dist.CLIENT)
public class DrippyForgeLoadingOverlay extends ForgeLoadingOverlay implements CustomizableLoadingOverlay {

    private static final Logger LOGGER = LogManager.getLogger();

    protected DisplayWindow displayWindow;
    protected ReloadInstance reload;
    protected final Consumer<Optional<Throwable>> onFinish;
    protected Minecraft minecraft;
    protected float currentProgress;
    protected long fadeOutStart = -1L;

    protected static boolean firstScreenInit = true;
    protected MenuHandlerBase menuHandler = null;

    public DrippyForgeLoadingOverlay(Minecraft mc, ReloadInstance reloader, Consumer<Optional<Throwable>> errorConsumer, DisplayWindow displayWindow) {
        super(mc, reloader, errorConsumer, displayWindow);
        this.displayWindow = displayWindow;
        this.reload = reloader;
        this.onFinish = errorConsumer;
        this.minecraft = mc;
    }

    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial) {

        this.onRenderPre(graphics, mouseX, mouseY, partial, this.currentProgress);

        int colorInt = SharedLoadingOverlayData.getBackgroundColorInt(this.displayWindow);
        int i = graphics.guiWidth();
        int j = graphics.guiHeight();
        long k = Util.getMillis();
        float f = this.fadeOutStart > -1L ? (float)(k - this.fadeOutStart) / 1000.0F : -1.0F;
        if (f >= 1.0F) {
            if (this.minecraft.screen != null) {
                this.beforeRenderScreenForFancyMenuInDrippy(graphics, mouseX, mouseY, partial);
                this.minecraft.screen.render(graphics, 0, 0, partial);
                this.afterRenderScreenForFancyMenuInDrippy(graphics, mouseX, mouseY, partial);
            }
            int l = Mth.ceil((1.0F - Mth.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.fill(RenderType.guiOverlay(), 0, 0, i, j, CustomizableLoadingOverlay.replaceAlpha(colorInt, grabAndHandleBackgroundAlpha(l)));
        } else {
            float f3 = (float)(colorInt >> 16 & 255) / 255.0F;
            float f4 = (float)(colorInt >> 8 & 255) / 255.0F;
            float f5 = (float)(colorInt & 255) / 255.0F;
            GlStateManager._clearColor(f3, f4, f5, 1.0F);
            GlStateManager._clear(GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        this.onBackgroundRendered(graphics, mouseX, mouseY, partial);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        this.renderCustomizableInstanceOfLogo(graphics);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        float f6 = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + f6 * 0.050000012F, 0.0F, 1.0F);
        ((IMixinLoadingOverlay)this).setCurrentProgressDrippy(this.currentProgress);
        if (f < 1.0F) {
            this.renderCustomizableInstanceOfProgressBar(graphics, 1.0F - Mth.clamp(f, 0.0F, 1.0F));
        }
        if (f >= 2.0F) {
            this.beforeClosingOverlayForFancyMenuInDrippy();
            this.minecraft.setOverlay(null);
            this.onCloseOverlay();
            this.displayWindow.close();
            this.handleSetForgeEarlyLoadingConfigOption();
        }

        if ((this.fadeOutStart == -1L) && this.reload.isDone()) {
            this.fadeOutStart = Util.getMillis();
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable throwable) {
                this.onFinish.accept(Optional.of(throwable));
            }
            if (this.minecraft.screen != null) {
                this.minecraft.screen.init(this.minecraft, graphics.guiWidth(), graphics.guiHeight());
                this.afterInitScreenForFancyMenuInDrippy();
            }
        }

        this.onRenderPost(graphics, mouseX, mouseY, partial, this.currentProgress);

    }

    private int grabAndHandleBackgroundAlpha(int alpha) {
        float opacity = Math.max(0.0F, Math.min(1.0F, (float)alpha / 255.0F));
        this.setCustomBackgroundOpacity(opacity);
        if (!DrippyLoadingScreen.config.getOrDefault("early_fade_out_elements", false)) {
            this.setOverlayOpacity(opacity);
        }
        return alpha;
    }

    @SuppressWarnings("all")
    private void beforeRenderScreenForFancyMenuInDrippy(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        if ((this.menuHandler != null) && MenuCustomization.isMenuCustomizable(Minecraft.getInstance().screen)) {
            //Manually call onRenderPre of the screen's menu handler, because it doesn't get called automatically in the loading screen
            this.menuHandler.onRenderPre(new ScreenEvent.Render.Pre(Minecraft.getInstance().screen, graphics, mouseX, mouseY, partial));
        }
    }

    @SuppressWarnings("all")
    private void afterRenderScreenForFancyMenuInDrippy(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        if ((this.menuHandler != null) && MenuCustomization.isMenuCustomizable(Minecraft.getInstance().screen)) {
            //This is to correctly render the title menu
            if (this.menuHandler instanceof MainMenuHandler) {
                try {
                    Method m = MainMenuHandler.class.getDeclaredMethod("renderBackground", GuiGraphics.class, int.class, int.class, float.class);
                    m.setAccessible(true);
                    m.invoke(this.menuHandler, graphics, mouseX, mouseY, partial);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            //Manually call onRenderPost of the screen's menu handler, because it doesn't get called automatically in the loading screen
            this.menuHandler.onRenderPost(new ScreenEvent.Render.Post(Minecraft.getInstance().screen, graphics, mouseX, mouseY, partial));
        }
    }

    private void afterInitScreenForFancyMenuInDrippy() {
        if (Minecraft.getInstance().screen != null) {
            //Update resource pack animation sizes after reloading textures and when starting the game
            LOGGER.info("[FANCYMENU] Updating animation sizes..");
            AnimationHandler.updateAnimationSizes();
            //Cache the menu handler of the screen to be able to call some of its render events
            this.menuHandler = MenuHandlerRegistry.getHandlerFor(Minecraft.getInstance().screen);
            //If it's the first time a screen gets initialized, soft-reload the screen's handler, so first-time stuff works when fading to the Title menu
            if ((this.menuHandler != null) && firstScreenInit) {
                this.menuHandler.onSoftReload(new SoftMenuReloadEvent(Minecraft.getInstance().screen));
            }
            firstScreenInit = false;
            //Reset isNewMenu, so first-time stuff and on-load stuff works correctly, because the menu got initialized already (this is after screen init)
            MenuCustomization.setIsNewMenu(true);
            //Set the screen again to cover all customization init stages
            MenuCustomization.reInitCurrentScreen();
        }
    }

    private void beforeClosingOverlayForFancyMenuInDrippy() {
        if (Minecraft.getInstance().screen == null) {
            //Update resource pack animation sizes after reloading textures if fading to no screen (while in-game)
            LOGGER.info("[FANCYMENU] Updating animation sizes..");
            AnimationHandler.updateAnimationSizes();
        }
    }

}
