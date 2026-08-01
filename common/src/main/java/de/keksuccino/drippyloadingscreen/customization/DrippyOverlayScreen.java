package de.keksuccino.drippyloadingscreen.customization;

import de.keksuccino.drippyloadingscreen.DrippyUtils;
import de.keksuccino.drippyloadingscreen.mixin.mixins.common.client.AccessorMixinLoadingOverlay;
import de.keksuccino.fancymenu.customization.ScreenCustomization;
import de.keksuccino.fancymenu.customization.layer.ScreenCustomizationLayer;
import de.keksuccino.fancymenu.customization.layer.ScreenCustomizationLayerHandler;
import de.keksuccino.fancymenu.customization.overlay.CustomizationOverlay;
import de.keksuccino.fancymenu.events.screen.RenderedScreenBackgroundEvent;
import de.keksuccino.fancymenu.mixin.mixins.common.client.IMixinAbstractWidget;
import de.keksuccino.fancymenu.util.event.acara.EventHandler;
import de.keksuccino.fancymenu.util.rendering.ui.widget.RendererWidget;
import net.minecraft.client.Minecraft;
import de.keksuccino.drippyloadingscreen.mixin.MixinCache;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import java.util.function.IntSupplier;

public class DrippyOverlayScreen extends Screen {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Identifier MOJANG_STUDIOS_LOGO_LOCATION = Identifier.parse("textures/gui/title/mojangstudios.png");
    private static final Component CUSTOMIZATION_HINT = Component.translatable("drippyloadingscreen.overlay.customization_hint");
    private static final int CUSTOMIZATION_HINT_COLOR = 0xFFFFFFFF;
    private static final int CUSTOMIZATION_HINT_SHADOW_COLOR = 0xA0000000;
    private static final float CUSTOMIZATION_HINT_SHADOW_OFFSET = 2.0F;
    private static final float CUSTOMIZATION_HINT_ARROW_TIP_X = 45.0F;
    private static final float CUSTOMIZATION_HINT_ARROW_TIP_Y = 40.0F;
    private static final int CUSTOMIZATION_HINT_ARROW_LENGTH = 66;
    // GUI coordinates grow downward, so a negative rotation turns an upward-facing arrow toward the upper-left.
    private static final float CUSTOMIZATION_HINT_ARROW_ROTATION = (float)Math.toRadians(-20.0D);
    private static final float CUSTOMIZATION_HINT_TEXT_SCALE = 2.0F;
    private static final float CUSTOMIZATION_HINT_TEXT_GAP = 8.0F;
    private static final float CUSTOMIZATION_HINT_TEXT_Y_OFFSET = 15.0F;

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
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractRenderState(graphics, mouseX, mouseY, partial);
        ScreenCustomizationLayer layer = ScreenCustomizationLayerHandler.getLayerOfScreen(this);
        // The hint target only exists in FancyMenu's customization overlay, and the hint is unnecessary once a layout is active.
        if (DrippyUtils.drippyCustomizationEntered && DrippyUtils.isDrippyRendering() && !DrippyUtils.isLoadingOverlayActive() && CustomizationOverlay.isOverlayVisible(this) && ((layer == null) || layer.activeLayouts.isEmpty())) {
            drawCustomizationHint(graphics);
        }
    }

    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        ScreenCustomizationLayer layer = ScreenCustomizationLayerHandler.getLayerOfScreen(this);
        boolean shouldRenderDefaultBackground = (layer == null) || layer.layoutBase.menuBackgrounds.isEmpty();
        IntSupplier supplier = AccessorMixinLoadingOverlay.getBrandBackgroundDrippy();
        int color = (supplier != null) ? supplier.getAsInt() : 0;
        if (shouldRenderDefaultBackground) {
            graphics.fill(0, 0, this.width, this.height, replaceAlpha(color, (int)(this.backgroundOpacity * 255.0F)));
        }
        EventHandler.INSTANCE.postEvent(new RenderedScreenBackgroundEvent(this, graphics, mouseX, mouseY, partial));
    }

    private static int replaceAlpha(int color, int alpha) {
        if (alpha > 255) alpha = 255;
        if (alpha < 0) alpha = 0;
        return color & 16777215 | alpha << 24;
    }

    private void drawCustomizationHint(GuiGraphicsExtractor graphics) {
        drawCustomizationHintArrow(graphics, CUSTOMIZATION_HINT_ARROW_TIP_X + CUSTOMIZATION_HINT_SHADOW_OFFSET, CUSTOMIZATION_HINT_ARROW_TIP_Y + CUSTOMIZATION_HINT_SHADOW_OFFSET, CUSTOMIZATION_HINT_SHADOW_COLOR);
        drawCustomizationHintArrow(graphics, CUSTOMIZATION_HINT_ARROW_TIP_X, CUSTOMIZATION_HINT_ARROW_TIP_Y, CUSTOMIZATION_HINT_COLOR);

        float arrowTailX = CUSTOMIZATION_HINT_ARROW_TIP_X - Mth.sin(CUSTOMIZATION_HINT_ARROW_ROTATION) * CUSTOMIZATION_HINT_ARROW_LENGTH;
        float arrowTailY = CUSTOMIZATION_HINT_ARROW_TIP_Y + Mth.cos(CUSTOMIZATION_HINT_ARROW_ROTATION) * CUSTOMIZATION_HINT_ARROW_LENGTH;

        graphics.pose().pushMatrix();
        graphics.pose().translate(arrowTailX + CUSTOMIZATION_HINT_TEXT_GAP, arrowTailY - this.font.lineHeight * CUSTOMIZATION_HINT_TEXT_SCALE * 0.5F + CUSTOMIZATION_HINT_TEXT_Y_OFFSET);
        graphics.pose().scale(CUSTOMIZATION_HINT_TEXT_SCALE, CUSTOMIZATION_HINT_TEXT_SCALE);
        graphics.text(this.font, CUSTOMIZATION_HINT, 0, 0, CUSTOMIZATION_HINT_COLOR, true);
        graphics.pose().popMatrix();
    }

    private static void drawCustomizationHintArrow(GuiGraphicsExtractor graphics, float tipX, float tipY, int color) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(tipX, tipY);
        graphics.pose().rotate(CUSTOMIZATION_HINT_ARROW_ROTATION);
        graphics.fill(-2, 0, 2, 4, color);
        graphics.fill(-6, 4, 6, 8, color);
        graphics.fill(-10, 8, 10, 12, color);
        graphics.fill(-4, 12, 4, CUSTOMIZATION_HINT_ARROW_LENGTH, color);
        graphics.pose().popMatrix();
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
                    int v = ARGB.white(((IMixinAbstractWidget)widget).getAlphaFancyMenu());
                    graphics.blit(RenderPipelines.MOJANG_LOGO, MOJANG_STUDIOS_LOGO_LOCATION, x, y, -0.0625F, 0.0F, width / 2, height, 120, 60, 120, 120, v);
                    graphics.blit(RenderPipelines.MOJANG_LOGO, MOJANG_STUDIOS_LOGO_LOCATION, x + (width / 2), y, 0.0625F, 60.0F, width / 2, height, 120, 60, 120, 120, v);
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
                    if (Minecraft.getInstance().gui.overlay() instanceof LoadingOverlay) {
                        currentProgress = ((AccessorMixinLoadingOverlay)Minecraft.getInstance().gui.overlay()).getCurrentProgressDrippy();
                    }
                    drawProgressBar(graphics, x, y, x + width, y + height, ((IMixinAbstractWidget)widget).getAlphaFancyMenu(), currentProgress);
                }
        ).setWidgetIdentifierFancyMenu("progress_bar");

    }

    private static void drawProgressBar(GuiGraphicsExtractor graphics, int xMin, int yMin, int xMax, int yMax, float opacity, float currentProgress) {
        int i = Mth.ceil((float)(xMax - xMin - 2) * currentProgress);
        int j = Math.round(opacity * 255.0F);
        int k = ARGB.color(j, 255, 255, 255);
        graphics.fill(xMin + 2, yMin + 2, xMin + i, yMax - 2, k);
        graphics.fill(xMin + 1, yMin, xMax - 1, yMin + 1, k);
        graphics.fill(xMin + 1, yMax, xMax - 1, yMax - 1, k);
        graphics.fill(xMin, yMin, xMin + 1, yMax, k);
        graphics.fill(xMax, yMin, xMax - 1, yMax, k);
    }

}
