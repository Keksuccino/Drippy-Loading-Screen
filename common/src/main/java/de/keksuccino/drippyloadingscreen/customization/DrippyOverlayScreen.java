package de.keksuccino.drippyloadingscreen.customization;

import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.drippyloadingscreen.mixin.mixins.common.client.IMixinLoadingOverlay;
import de.keksuccino.fancymenu.customization.ScreenCustomization;
import de.keksuccino.fancymenu.customization.layer.ScreenCustomizationLayer;
import de.keksuccino.fancymenu.customization.layer.ScreenCustomizationLayerHandler;
import de.keksuccino.fancymenu.events.screen.RenderedScreenBackgroundEvent;
import de.keksuccino.fancymenu.mixin.mixins.common.client.IMixinAbstractWidget;
import de.keksuccino.fancymenu.util.event.acara.EventHandler;
import de.keksuccino.fancymenu.util.rendering.RenderingUtils;
import de.keksuccino.fancymenu.util.rendering.ui.widget.RendererWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import de.keksuccino.drippyloadingscreen.mixin.MixinCache;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import java.util.function.IntSupplier;

public class DrippyOverlayScreen extends Screen {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/mojangstudios.png");

    public float backgroundOpacity = 1.0F;

    public DrippyOverlayScreen() {
        super(Component.empty());
        MixinCache.cachedCurrentLoadingScreenProgress = 0.5F;
        this.forceEnableCustomizations();
    }

    protected void forceEnableCustomizations() {
        if (!ScreenCustomization.isCustomizationEnabledForScreen(this)) {
            LOGGER.info("[DRIPPY LOADING SCREEN] Force-enabling customizations for DrippyOverlayScreen..");
            ScreenCustomization.setCustomizationForScreenEnabled(this, true);
        }
    }

    @Override
    protected void init() {

        this.addRenderableWidget(buildLogoWidget());

        this.addRenderableWidget(buildProgressBarWidget());

    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partial);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics) {
        ScreenCustomizationLayer layer = ScreenCustomizationLayerHandler.getLayerOfScreen(this);
        boolean shouldRenderDefaultBackground = (layer == null) || (layer.layoutBase.menuBackground == null);
        IntSupplier supplier = IMixinLoadingOverlay.getBrandBackgroundDrippy();
        int color = (supplier != null) ? supplier.getAsInt() : 0;
        if (shouldRenderDefaultBackground) {
            RenderingUtils.resetShaderColor(graphics);
            graphics.fill(RenderType.guiOverlay(), 0, 0, this.width, this.height, replaceAlpha(color, (int)(this.backgroundOpacity * 255.0F)));
            RenderingUtils.resetShaderColor(graphics);
        }
        EventHandler.INSTANCE.postEvent(new RenderedScreenBackgroundEvent(this, graphics));
    }

    private static int replaceAlpha(int color, int alpha) {
        if (alpha > 255) alpha = 255;
        if (alpha < 0) alpha = 0;
        return color & 16777215 | alpha << 24;
    }

    public static RendererWidget buildLogoWidget() {

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int centerX = (int) ((double) screenWidth * 0.5D);
        int centerY = (int) ((double) screenHeight * 0.5D);

        double logoHeight = Math.min((double) mc.getWindow().getGuiScaledWidth() * 0.75D, mc.getWindow().getGuiScaledHeight()) * 0.25D;
        int logoHeightHalf = (int) (logoHeight * 0.5D);
        double logoWidth = logoHeight * 4.0D;
        int logoWidthHalf = (int) (logoWidth * 0.5D);
        int logoPosX = centerX - logoWidthHalf;
        int logoPosY = centerY - logoHeightHalf;

        return new RendererWidget(logoPosX, logoPosY, logoWidthHalf * 2, logoHeightHalf * 2,
                (graphics, mouseX, mouseY, partial, x, y, width, height, widget) -> {
                    RenderSystem.disableDepthTest();
                    RenderSystem.depthMask(false);
                    RenderSystem.enableBlend();
                    RenderSystem.blendFunc(770, 1);
                    graphics.setColor(1.0F, 1.0F, 1.0F, ((IMixinAbstractWidget)widget).getAlphaFancyMenu());
                    graphics.blit(MOJANG_STUDIOS_LOGO_LOCATION, x, y, width / 2, height, -0.0625F, 0.0F, 120, 60, 120, 120);
                    graphics.blit(MOJANG_STUDIOS_LOGO_LOCATION, x + (width / 2), y, (width / 2), height, 0.0625F, 60.0F, 120, 60, 120, 120);
                    RenderingUtils.resetShaderColor(graphics);
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.depthMask(true);
                    RenderSystem.enableDepthTest();
                }
        ).setWidgetIdentifierFancyMenu("mojang_logo");

    }

    public static RendererWidget buildProgressBarWidget() {

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        double someDouble1 = Math.min((double)screenWidth * 0.75D, screenHeight) * 0.25D;
        double someDouble2 = someDouble1 * 4.0D;
        int someInt1 = (int)(someDouble2 * 0.5D);
        int someInt2 = (int)((double)screenHeight * 0.8325D);
        int barPosX = screenWidth / 2 - someInt1;
        int barPosY = someInt2 - 5;
        int barWidth = someInt1 * 2;
        int barHeight = 10;

        return new RendererWidget(barPosX, barPosY, barWidth, barHeight,
                (graphics, mouseX, mouseY, partial, x, y, width, height, widget) -> {
                    float currentProgress = 0.5F;
                    if (Minecraft.getInstance().getOverlay() instanceof LoadingOverlay) {
                        currentProgress = ((IMixinLoadingOverlay)Minecraft.getInstance().getOverlay()).getCurrentProgressDrippy();
                    }
                    RenderingUtils.resetShaderColor(graphics);
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.enableBlend();
                    RenderSystem.depthMask(true);
                    RenderSystem.enableDepthTest();
                    drawProgressBar(graphics, x, y, x + width, y + height, ((IMixinAbstractWidget)widget).getAlphaFancyMenu(), currentProgress);
                    RenderingUtils.resetShaderColor(graphics);
                }
        ).setWidgetIdentifierFancyMenu("progress_bar");

    }

    private static void drawProgressBar(GuiGraphics graphics, int xMin, int yMin, int xMax, int yMax, float opacity, float currentProgress) {
        int i = Mth.ceil((float)(xMax - xMin - 2) * currentProgress);
        int j = Math.round(opacity * 255.0F);
        int k = FastColor.ARGB32.color(j, 255, 255, 255);
        graphics.fill(xMin + 2, yMin + 2, xMin + i, yMax - 2, k);
        graphics.fill(xMin + 1, yMin, xMax - 1, yMin + 1, k);
        graphics.fill(xMin + 1, yMax, xMax - 1, yMax - 1, k);
        graphics.fill(xMin, yMin, xMin + 1, yMax, k);
        graphics.fill(xMax, yMin, xMax - 1, yMax, k);
    }

}
