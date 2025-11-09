package de.keksuccino.drippyloadingscreen.neoforge;

import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.DrippyUtils;
import de.keksuccino.drippyloadingscreen.Options;
import de.keksuccino.fancymenu.util.file.type.FileMediaType;
import de.keksuccino.fancymenu.util.rendering.ui.UIBase;
import de.keksuccino.fancymenu.util.resource.ResourceSource;
import de.keksuccino.fancymenu.util.resource.ResourceSourceType;
import de.keksuccino.fancymenu.util.resource.ResourceSupplier;
import de.keksuccino.fancymenu.util.resource.resources.texture.ITexture;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mirrors the early-window renderer inside an in-game {@link Screen} so users can preview their setup without leaving
 * Minecraft. Rendering intentionally follows {@link de.keksuccino.drippyloadingscreen.earlywindow.DrippyEarlyWindowProvider}.
 */
public class EarlyLoadingPreviewScreen extends Screen {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Component TITLE = Component.translatable("drippyloadingscreen.screen.early_loading_preview");
    private static final ResourceLocation MOJANG_LOGO_LOCATION = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/title/mojangstudios.png");
    private static final ResourceSupplier<ITexture> MOJANG_LOGO_SUPPLIER = createBundledSupplier(MOJANG_LOGO_LOCATION);

    private static final String[] PLACEHOLDER_TEXTURE_VALUES = {
            "/config/fancymenu/assets/some_image.png",
            "/config/fancymenu/assets/some_bar_background_image.png",
            "/config/fancymenu/assets/some_bar_progress_image.png"
    };

    private static final int DEFAULT_REFERENCE_WIDTH = 854;
    private static final int DEFAULT_REFERENCE_HEIGHT = 480;

    private static final float INDETERMINATE_SEGMENT_WIDTH = 0.3f;
    private static final float LOGGER_BASE_TEXT_SCALE = 1.35f;
    private static final float LOGGER_LINE_HEIGHT = 12.0f;
    private static final float LOGGER_MARGIN = 10.0f;
    private static final float LOGGER_MIN_UI_SCALE = 0.75f;
    private static final int LOGGER_MAX_VISIBLE_LINES = 6;
    private static final int LOGGER_MAX_MESSAGE_LENGTH = 256;
    private static final long LOGGER_DEBUG_MESSAGE_INTERVAL_NANOS = 2_000_000_000L;
    private static final String[] LOGGER_DEBUG_MESSAGE_POOL = {
            "Initializing Drippy Early Window renderer...",
            "Connecting to loading pipeline",
            "Registering FancyMenu compatibility hooks",
            "Preparing APNG decoder",
            "Waiting for Minecraft bootstrap",
            "Completing early window handoff"
    };

    private final EarlyLoadingVisualOptions visualOptions;
    private final TextureSuppliers textureSuppliers;
    private ColorScheme colorScheme;

    private float baseWidth;
    private float baseHeight;

    private float displayedProgress;
    private float simulationProgress;
    private float simulationStateTimer;
    private boolean simulationDeterminate = true;
    private boolean progressIndeterminate;
    private float indeterminateOffset;
    private long lastProgressSampleNanos;

    private final List<DebugLoggerMessage> loggerMessages = new ArrayList<>();
    private long lastLoggerMessageNanos;

    public EarlyLoadingPreviewScreen() {
        super(TITLE);
        this.visualOptions = EarlyLoadingVisualOptions.from(DrippyLoadingScreen.getOptions());
        this.textureSuppliers = new TextureSuppliers(this.visualOptions);
        this.colorScheme = resolveColorScheme();
    }

    @Override
    protected void init() {
        super.init();
        this.baseWidth = resolveReferenceWidth();
        this.baseHeight = resolveReferenceHeight();
    }

    @Override
    public void tick() {
        super.tick();
        this.colorScheme = resolveColorScheme();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        updateProgressMetrics();
        RenderMetrics metrics = captureRenderMetrics();
        renderBackgroundLayer(graphics, metrics);
        float uiScale = computeUiScale(metrics);
        float logoBottom = renderLogoLayer(graphics, metrics, uiScale);
        renderProgressBar(graphics, metrics, logoBottom, uiScale);
        renderWatermarks(graphics, metrics, uiScale);
        renderLoggerOverlay(graphics, metrics, uiScale);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Background is fully controlled by render().
    }

    private void renderBackgroundLayer(GuiGraphics graphics, RenderMetrics metrics) {
        graphics.fill(0, 0, this.width, this.height, toArgb(this.colorScheme.background(), 1.0f));
        TextureInfo background = fetchTexture(this.textureSuppliers.background());
        if (!background.isValid()) {
            return;
        }
        float drawWidth = metrics.absoluteWidth();
        float drawHeight = metrics.absoluteHeight();
        float x = 0.0f;
        float y = 0.0f;
        if (this.visualOptions.backgroundPreserveAspectRatio() && background.width() > 0 && background.height() > 0) {
            float textureRatio = (float) background.width() / (float) background.height();
            float windowRatio = metrics.absoluteWidth() / metrics.absoluteHeight();
            if (windowRatio > textureRatio) {
                drawWidth = metrics.absoluteWidth();
                drawHeight = drawWidth / textureRatio;
                y = (metrics.absoluteHeight() - drawHeight) / 2.0f;
            } else {
                drawHeight = metrics.absoluteHeight();
                drawWidth = drawHeight * textureRatio;
                x = (metrics.absoluteWidth() - drawWidth) / 2.0f;
            }
        }
        drawTexture(graphics, background, metrics.toGui(x), metrics.toGui(y), metrics.toGui(drawWidth), metrics.toGui(drawHeight));
    }

    private float renderLogoLayer(GuiGraphics graphics, RenderMetrics metrics, float uiScale) {
        float scaledOffsetY = this.visualOptions.logoOffsetY() * uiScale;
        if (this.visualOptions.hideLogo()) {
            return metrics.absoluteHeight() / 2.0f + scaledOffsetY;
        }
        TextureInfo logoTexture = resolveLogoTexture();
        if (!logoTexture.isValid()) {
            return metrics.absoluteHeight() / 2.0f + scaledOffsetY;
        }
        float baseWidth = this.visualOptions.logoWidth() > 0 ? this.visualOptions.logoWidth() : logoTexture.width();
        float baseHeight = this.visualOptions.logoHeight() > 0 ? this.visualOptions.logoHeight() : logoTexture.height();
        float width = Math.max(1.0f, baseWidth * uiScale);
        float height = Math.max(1.0f, baseHeight * uiScale);
        float offsetX = this.visualOptions.logoOffsetX() * uiScale;
        float x = (metrics.absoluteWidth() - width) / 2.0f + offsetX;
        float baseline = metrics.absoluteHeight() * 0.35f;
        float y = baseline + scaledOffsetY;
        drawTexture(graphics, logoTexture, metrics.toGui(x), metrics.toGui(y), metrics.toGui(width), metrics.toGui(height));
        return y + height;
    }

    private void renderProgressBar(GuiGraphics graphics, RenderMetrics metrics, float logoBottom, float uiScale) {
        if (this.visualOptions.hideBar()) {
            return;
        }
        float screenWidth = metrics.absoluteWidth();
        float screenHeight = metrics.absoluteHeight();
        int configuredWidth = Math.max(32, this.visualOptions.barWidth());
        int configuredHeight = Math.max(6, this.visualOptions.barHeight());
        float targetWidth = configuredWidth * uiScale;
        float targetHeight = configuredHeight * uiScale;
        float minWidth = 32.0f * uiScale;
        float minHeight = 6.0f * uiScale;
        float maxWidth = Math.max(minWidth, screenWidth - 40.0f);
        float maxHeight = Math.max(minHeight, screenHeight / 6.0f);
        float width = Mth.clamp(targetWidth, minWidth, maxWidth);
        float height = Mth.clamp(targetHeight, minHeight, maxHeight);
        float offsetX = this.visualOptions.barOffsetX() * uiScale;
        float offsetY = this.visualOptions.barOffsetY() * uiScale;
        float baseX = (screenWidth - width) / 2.0f + offsetX;
        float spacing = 32.0f * uiScale;
        float fallbackSpacing = 20.0f * uiScale;
        float defaultY = (logoBottom > 0.0f ? logoBottom + spacing : screenHeight / 2.0f + fallbackSpacing);
        float minY = 10.0f * uiScale;
        float maxY = Math.max(minY, screenHeight - height - minY);
        float baseY = Mth.clamp(defaultY + offsetY, minY, maxY);
        float minX = 10.0f * uiScale;
        float maxX = Math.max(minX, screenWidth - width - minX);
        baseX = Mth.clamp(baseX, minX, maxX);

        float guiBaseX = metrics.toGui(baseX);
        float guiBaseY = metrics.toGui(baseY);
        float guiWidth = metrics.toGui(width);
        float guiHeight = metrics.toGui(height);
        TextureInfo barBackground = fetchTexture(this.textureSuppliers.barBackground());
        if (barBackground.isValid()) {
            drawTexture(graphics, barBackground, guiBaseX, guiBaseY, guiWidth, guiHeight);
        } else {
            drawSolidRect(graphics, guiBaseX, guiBaseY, guiWidth, guiHeight, this.colorScheme.background().withBrightness(0.5f), 0.9f);
            drawOutline(graphics, guiBaseX, guiBaseY, guiWidth, guiHeight, this.colorScheme.foreground(), 1.0f);
        }

        if (this.progressIndeterminate) {
            drawIndeterminateProgress(graphics, guiBaseX, guiBaseY, guiWidth, guiHeight);
        } else {
            float clamped = Mth.clamp(this.displayedProgress, 0.0f, 1.0f);
            drawProgressSegment(graphics, guiBaseX, guiBaseY, guiWidth, guiHeight, 0.0f, clamped);
        }
    }

    private void renderWatermarks(GuiGraphics graphics, RenderMetrics metrics, float uiScale) {
        renderWatermark(graphics, metrics, this.textureSuppliers.topLeft(), this.visualOptions.topLeftWidth(), this.visualOptions.topLeftHeight(),
                this.visualOptions.topLeftOffsetX(), this.visualOptions.topLeftOffsetY(), WatermarkAnchor.TOP_LEFT, uiScale);
        renderWatermark(graphics, metrics, this.textureSuppliers.topRight(), this.visualOptions.topRightWidth(), this.visualOptions.topRightHeight(),
                this.visualOptions.topRightOffsetX(), this.visualOptions.topRightOffsetY(), WatermarkAnchor.TOP_RIGHT, uiScale);
        renderWatermark(graphics, metrics, this.textureSuppliers.bottomLeft(), this.visualOptions.bottomLeftWidth(), this.visualOptions.bottomLeftHeight(),
                this.visualOptions.bottomLeftOffsetX(), this.visualOptions.bottomLeftOffsetY(), WatermarkAnchor.BOTTOM_LEFT, uiScale);
        renderWatermark(graphics, metrics, this.textureSuppliers.bottomRight(), this.visualOptions.bottomRightWidth(), this.visualOptions.bottomRightHeight(),
                this.visualOptions.bottomRightOffsetX(), this.visualOptions.bottomRightOffsetY(), WatermarkAnchor.BOTTOM_RIGHT, uiScale);
    }

    private void renderWatermark(GuiGraphics graphics, RenderMetrics metrics, @Nullable ResourceSupplier<ITexture> supplier, int configuredWidth, int configuredHeight,
                                 int offsetX, int offsetY, WatermarkAnchor anchor, float uiScale) {
        TextureInfo texture = fetchTexture(supplier);
        if (!texture.isValid()) {
            return;
        }
        int fallbackWidth = texture.width() > 0 ? texture.width() : 1;
        int fallbackHeight = texture.height() > 0 ? texture.height() : 1;
        float width = Math.max(1.0f, (configuredWidth > 0 ? configuredWidth : fallbackWidth) * uiScale);
        float height = Math.max(1.0f, (configuredHeight > 0 ? configuredHeight : fallbackHeight) * uiScale);
        float scaledOffsetX = offsetX * uiScale;
        float scaledOffsetY = offsetY * uiScale;
        float x;
        float y;
        switch (anchor) {
            case TOP_LEFT -> {
                x = scaledOffsetX;
                y = scaledOffsetY;
            }
            case TOP_RIGHT -> {
                x = metrics.absoluteWidth() - width + scaledOffsetX;
                y = scaledOffsetY;
            }
            case BOTTOM_LEFT -> {
                x = scaledOffsetX;
                y = metrics.absoluteHeight() - height + scaledOffsetY;
            }
            case BOTTOM_RIGHT -> {
                x = metrics.absoluteWidth() - width + scaledOffsetX;
                y = metrics.absoluteHeight() - height + scaledOffsetY;
            }
            default -> {
                x = scaledOffsetX;
                y = scaledOffsetY;
            }
        }
        drawTexture(graphics, texture, metrics.toGui(x), metrics.toGui(y), metrics.toGui(width), metrics.toGui(height));
    }

    private void renderLoggerOverlay(GuiGraphics graphics, RenderMetrics metrics, float uiScale) {
        if (this.visualOptions.hideLogger() || this.font == null) {
            return;
        }
        long now = Util.getNanos();
        updateLoggerMessages(now);
        List<LoggerLine> lines = collectLoggerLines(now);
        if (lines.isEmpty()) {
            return;
        }
        int visible = Math.min(lines.size(), LOGGER_MAX_VISIBLE_LINES);
        int startIndex = lines.size() - visible;
        float effectiveScale = Math.max(LOGGER_MIN_UI_SCALE, uiScale);
        float textScale = LOGGER_BASE_TEXT_SCALE * effectiveScale;
        float lineHeight = LOGGER_LINE_HEIGHT * textScale;
        float margin = LOGGER_MARGIN * textScale;
        float totalHeight = visible * lineHeight;
        float startY = metrics.absoluteHeight() - totalHeight - margin;
        if (startY < margin) {
            startY = margin;
        }
        float x = margin;
        for (int idx = 0; idx < visible; idx++) {
            LoggerLine line = lines.get(startIndex + idx);
            float y = startY + idx * lineHeight;
            drawLoggerLine(graphics, metrics, line.text(), x, y, line.alpha(), textScale);
        }
    }

    private void drawLoggerLine(GuiGraphics graphics, RenderMetrics metrics, String text, float x, float y, float alpha, float textScale) {
        if (text.isEmpty() || alpha <= 0.0f || this.font == null) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(metrics.toGui(x), metrics.toGui(y), 0.0f);
        graphics.pose().scale(textScale, textScale, 1.0f);
        RenderSystem.enableBlend();
        graphics.drawString(this.font, text, 0, 0, toArgb(this.colorScheme.foreground(), alpha), false);
        graphics.pose().popPose();
    }

    private List<LoggerLine> collectLoggerLines(long now) {
        List<LoggerLine> result = new ArrayList<>();
        for (int i = this.loggerMessages.size() - 1; i >= 0; i--) {
            DebugLoggerMessage message = this.loggerMessages.get(i);
            int reverseIndex = this.loggerMessages.size() - i;
            int ageMillis = (int) ((now - message.createdAtNanos()) / 1_000_000L);
            float fade = computeLoggerFade(ageMillis, reverseIndex);
            if (fade <= 0.01f) {
                continue;
            }
            String sanitized = sanitizeLogMessage(message.text());
            if (sanitized.isEmpty()) {
                continue;
            }
            result.add(new LoggerLine(sanitized, fade));
        }
        return result;
    }

    private void updateLoggerMessages(long now) {
        if (this.loggerMessages.isEmpty()) {
            this.loggerMessages.add(new DebugLoggerMessage(LOGGER_DEBUG_MESSAGE_POOL[0], now));
            this.lastLoggerMessageNanos = now;
            return;
        }
        if ((now - this.lastLoggerMessageNanos) < LOGGER_DEBUG_MESSAGE_INTERVAL_NANOS) {
            return;
        }
        this.lastLoggerMessageNanos = now;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String message = LOGGER_DEBUG_MESSAGE_POOL[random.nextInt(LOGGER_DEBUG_MESSAGE_POOL.length)];
        this.loggerMessages.add(new DebugLoggerMessage(message, now));
        if (this.loggerMessages.size() > LOGGER_MAX_VISIBLE_LINES * 2) {
            this.loggerMessages.remove(0);
        }
    }

    private void drawIndeterminateProgress(GuiGraphics graphics, float x, float y, float width, float height) {
        float start = this.indeterminateOffset;
        float end = start + INDETERMINATE_SEGMENT_WIDTH;
        if (end <= 1.0f) {
            drawProgressSegment(graphics, x, y, width, height, start, end);
        } else {
            drawProgressSegment(graphics, x, y, width, height, start, 1.0f);
            drawProgressSegment(graphics, x, y, width, height, 0.0f, end - 1.0f);
        }
    }

    private void drawProgressSegment(GuiGraphics graphics, float baseX, float baseY, float width, float height, float start, float end) {
        if (end <= start) {
            return;
        }
        TextureInfo progressTexture = fetchTexture(this.textureSuppliers.barProgress());
        if (progressTexture.isValid()) {
            drawProgressTextureClipped(graphics, progressTexture, baseX, baseY, width, height, start, end);
        } else {
            float segmentWidth = width * (end - start);
            float segmentX = baseX + width * start;
            drawSolidRect(graphics, segmentX, baseY, segmentWidth, height, this.colorScheme.foreground(), 1.0f);
        }
    }

    private void drawProgressTextureClipped(GuiGraphics graphics, TextureInfo texture, float baseX, float baseY, float width, float height, float start, float end) {
        float segmentStart = baseX + width * start;
        float segmentEnd = baseX + width * end;
        int clipMinX = Math.round(Math.min(segmentStart, segmentEnd));
        int clipMaxX = Math.round(Math.max(segmentStart, segmentEnd));
        int clipMinY = Math.round(baseY);
        int clipMaxY = Math.round(baseY + height);
        if (clipMaxX <= clipMinX || clipMaxY <= clipMinY) {
            return;
        }
        graphics.enableScissor(clipMinX, clipMinY, clipMaxX, clipMaxY);
        try {
            drawTexture(graphics, texture, baseX, baseY, width, height);
        } finally {
            graphics.disableScissor();
        }
    }

    private void drawTexture(GuiGraphics graphics, TextureInfo texture, float x, float y, float width, float height) {
        if (!texture.isValid()) {
            return;
        }
        int drawWidth = Math.max(1, Math.round(width));
        int drawHeight = Math.max(1, Math.round(height));
        int drawX = Math.round(x);
        int drawY = Math.round(y);
        RenderSystem.enableBlend();
        graphics.blit(texture.location(), drawX, drawY, 0.0f, 0.0f, drawWidth, drawHeight, drawWidth, drawHeight);
    }

    private void drawSolidRect(GuiGraphics graphics, float x, float y, float width, float height, Color color, float alpha) {
        int argb = toArgb(color, alpha);
        int left = Math.round(x);
        int top = Math.round(y);
        int right = Math.max(left + 1, Math.round(x + width));
        int bottom = Math.max(top + 1, Math.round(y + height));
        RenderSystem.enableBlend();
        graphics.fill(left, top, right, bottom, argb);
    }

    private void drawOutline(GuiGraphics graphics, float x, float y, float width, float height, Color color, float alpha) {
        int argb = toArgb(color, alpha);
        int left = Math.round(x);
        int top = Math.round(y);
        int right = Math.max(left + 1, Math.round(x + width));
        int bottom = Math.max(top + 1, Math.round(y + height));
        RenderSystem.enableBlend();
        graphics.fill(left, top, right, top + 1, argb);
        graphics.fill(left, bottom - 1, right, bottom, argb);
        graphics.fill(left, top, left + 1, bottom, argb);
        graphics.fill(right - 1, top, right, bottom, argb);
    }

    private TextureInfo resolveLogoTexture() {
        TextureInfo custom = fetchTexture(this.textureSuppliers.logo());
        if (custom.isValid()) {
            return custom;
        }
        return fetchTexture(MOJANG_LOGO_SUPPLIER);
    }

    private TextureInfo fetchTexture(@Nullable ResourceSupplier<ITexture> supplier) {
        if (supplier == null) {
            return TextureInfo.EMPTY;
        }
        ITexture texture = supplier.get();
        DrippyUtils.waitForTexture(texture);
        if (texture == null || !texture.isReady()) {
            return TextureInfo.EMPTY;
        }
        ResourceLocation location = texture.getResourceLocation();
        if (location == null) {
            return TextureInfo.EMPTY;
        }
        return new TextureInfo(location, Math.max(1, texture.getWidth()), Math.max(1, texture.getHeight()));
    }

    private void updateProgressMetrics() {
        long now = Util.getNanos();
        if (this.lastProgressSampleNanos == 0L) {
            this.lastProgressSampleNanos = now;
            return;
        }
        float deltaSeconds = (now - this.lastProgressSampleNanos) / 1_000_000_000f;
        this.lastProgressSampleNanos = now;
        this.simulationStateTimer += deltaSeconds;
        if (this.simulationDeterminate && this.simulationStateTimer >= 8.0f) {
            this.simulationDeterminate = false;
            this.simulationStateTimer = 0.0f;
            this.indeterminateOffset = 0.0f;
        } else if (!this.simulationDeterminate && this.simulationStateTimer >= 3.0f) {
            this.simulationDeterminate = true;
            this.simulationStateTimer = 0.0f;
            this.simulationProgress = 0.0f;
        }

        if (this.simulationDeterminate) {
            this.progressIndeterminate = false;
            this.simulationProgress = (this.simulationProgress + deltaSeconds * 0.18f) % 1.05f;
            float target = Mth.clamp(this.simulationProgress, 0.0f, 1.0f);
            float lerpFactor = Math.min(1.0f, deltaSeconds * 6.0f);
            this.displayedProgress += (target - this.displayedProgress) * lerpFactor;
            this.indeterminateOffset = 0.0f;
        } else {
            this.progressIndeterminate = true;
            this.indeterminateOffset = (this.indeterminateOffset + deltaSeconds * 0.4f) % 1.0f;
            this.displayedProgress = 0.0f;
        }
    }

    private float computeUiScale(RenderMetrics metrics) {
        float baseW = Math.max(1.0f, this.baseWidth);
        float baseH = Math.max(1.0f, this.baseHeight);
        float scaleX = metrics.absoluteWidth() / baseW;
        float scaleY = metrics.absoluteHeight() / baseH;
        float scale = Math.min(scaleX, scaleY);
        return Math.max(0.1f, scale);
    }

    private static ColorScheme resolveColorScheme() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean dark = minecraft != null && minecraft.options != null && minecraft.options.darkMojangStudiosBackground().get();
        return dark ? ColorScheme.dark() : ColorScheme.red();
    }

    private static String sanitizeTextureValue(@Nullable String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        for (String placeholder : PLACEHOLDER_TEXTURE_VALUES) {
            if (placeholder.equalsIgnoreCase(trimmed)) {
                return null;
            }
        }
        return trimmed;
    }

    @Nullable
    private static ResourceSupplier<ITexture> createSupplier(@Nullable String source) {
        String sanitized = sanitizeTextureValue(source);
        if (sanitized == null) {
            return null;
        }
        try {
            return ResourceSupplier.image(sanitized);
        } catch (Exception ex) {
            LOGGER.warn("[DRIPPY LOADING SCREEN] Failed to create texture supplier for {}", sanitized, ex);
            return null;
        }
    }

    private static ResourceSupplier<ITexture> createBundledSupplier(ResourceLocation location) {
        try {
            ResourceSource source = ResourceSource.of(location.toString(), ResourceSourceType.LOCATION);
            return ResourceSupplier.image(source.getSourceWithPrefix());
        } catch (Exception ex) {
            LOGGER.warn("[DRIPPY LOADING SCREEN] Failed to create bundled texture supplier for {}", location, ex);
            return ResourceSupplier.empty(ITexture.class, FileMediaType.IMAGE);
        }
    }

    private static float computeLoggerFade(int ageMillis, int reverseIndex) {
        float fade = (4000.0f - ageMillis - (reverseIndex - 4) * 1000.0f) / 5000.0f;
        return Mth.clamp(fade, 0.0f, 1.0f);
    }

    private float resolveReferenceWidth() {
        if (this.visualOptions.windowWidthOverride() > 0) {
            return this.visualOptions.windowWidthOverride();
        }
        Integer configured = EarlyWindowReferenceSize.configWidth();
        if (configured != null && configured > 0) {
            return configured;
        }
        return DEFAULT_REFERENCE_WIDTH;
    }

    private float resolveReferenceHeight() {
        if (this.visualOptions.windowHeightOverride() > 0) {
            return this.visualOptions.windowHeightOverride();
        }
        Integer configured = EarlyWindowReferenceSize.configHeight();
        if (configured != null && configured > 0) {
            return configured;
        }
        return DEFAULT_REFERENCE_HEIGHT;
    }

    private static String sanitizeLogMessage(@Nullable String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.strip();
        if (trimmed.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(Math.min(LOGGER_MAX_MESSAGE_LENGTH, trimmed.length()));
        boolean truncated = false;
        for (int i = 0; i < trimmed.length(); i++) {
            if (builder.length() >= LOGGER_MAX_MESSAGE_LENGTH) {
                truncated = true;
                break;
            }
            char ch = trimmed.charAt(i);
            if (ch == '\r' || ch == '\n') {
                if (builder.length() == 0 || builder.charAt(builder.length() - 1) == ' ') {
                    continue;
                }
                builder.append(' ');
                continue;
            }
            if (ch < 32 || ch > 126) {
                builder.append('?');
            } else {
                builder.append(ch);
            }
        }
        if (truncated && LOGGER_MAX_MESSAGE_LENGTH > 3) {
            builder.setLength(LOGGER_MAX_MESSAGE_LENGTH - 3);
            builder.append("...");
        }
        return builder.toString();
    }

    private static int toArgb(Color color, float alpha) {
        int a = Mth.clamp(Math.round(alpha * 255.0f), 0, 255);
        int r = Mth.clamp(Math.round(color.r() * 255.0f), 0, 255);
        int g = Mth.clamp(Math.round(color.g() * 255.0f), 0, 255);
        int b = Mth.clamp(Math.round(color.b() * 255.0f), 0, 255);
        return FastColor.ARGB32.color(a, r, g, b);
    }

    private RenderMetrics captureRenderMetrics() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.getWindow() != null) {
            float guiFactor = Math.max(UIBase.calculateFixedScale(1.0f), 1.0f / Math.max(1.0f, (float)minecraft.getWindow().getGuiScale()));
            float absoluteWidth = Math.max(1.0f, minecraft.getWindow().getWidth());
            float absoluteHeight = Math.max(1.0f, minecraft.getWindow().getHeight());
            return new RenderMetrics(absoluteWidth, absoluteHeight, guiFactor);
        }
        float guiFactor = 1.0f;
        float absoluteWidth = Math.max(1.0f, this.width);
        float absoluteHeight = Math.max(1.0f, this.height);
        return new RenderMetrics(absoluteWidth, absoluteHeight, guiFactor);
    }

    private enum WatermarkAnchor {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    private record RenderMetrics(float absoluteWidth, float absoluteHeight, float guiScaleFactor) {
        private float toGui(float absolute) {
            return absolute * this.guiScaleFactor;
        }
    }

    private static final class EarlyWindowReferenceSize {
        private static final String CONFIG_CLASS = "net.neoforged.fml.loading.FMLConfig";
        private static final String CONFIG_VALUE_CLASS = "net.neoforged.fml.loading.FMLConfig$ConfigValue";
        private static final String METHOD_NAME = "getIntConfigValue";
        private static final String WIDTH_ENUM = "EARLY_WINDOW_WIDTH";
        private static final String HEIGHT_ENUM = "EARLY_WINDOW_HEIGHT";

        private static Integer cachedWidth;
        private static Integer cachedHeight;
        private static boolean widthResolved;
        private static boolean heightResolved;

        @Nullable
        private static Integer configWidth() {
            if (!widthResolved) {
                cachedWidth = fetchValue(WIDTH_ENUM);
                widthResolved = true;
            }
            return cachedWidth;
        }

        @Nullable
        private static Integer configHeight() {
            if (!heightResolved) {
                cachedHeight = fetchValue(HEIGHT_ENUM);
                heightResolved = true;
            }
            return cachedHeight;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Nullable
        private static Integer fetchValue(String enumName) {
            try {
                Class<?> enumRaw = Class.forName(CONFIG_VALUE_CLASS);
                Class<? extends Enum> enumClass = (Class<? extends Enum>) enumRaw.asSubclass(Enum.class);
                Enum constant = Enum.valueOf(enumClass, enumName);
                Class<?> configClass = Class.forName(CONFIG_CLASS);
                Method method = configClass.getMethod(METHOD_NAME, enumRaw);
                Object value = method.invoke(null, constant);
                if (value instanceof Number number) {
                    return number.intValue();
                }
            } catch (Throwable ignored) {
                // NeoForge classes not present (Fabric) or reflection failed.
            }
            return null;
        }
    }

    private record TextureInfo(@Nullable ResourceLocation location, int width, int height) {
        private static final TextureInfo EMPTY = new TextureInfo(null, 0, 0);

        private boolean isValid() {
            return this.location != null && this.width > 0 && this.height > 0;
        }
    }

    private record LoggerLine(String text, float alpha) {}

    private record DebugLoggerMessage(String text, long createdAtNanos) {}

    private record Color(float r, float g, float b) {
        private Color withBrightness(float factor) {
            return new Color(Math.min(1.0f, r * factor), Math.min(1.0f, g * factor), Math.min(1.0f, b * factor));
        }
    }

    private record ColorScheme(Color background, Color foreground) {
        private static ColorScheme red() {
            return new ColorScheme(new Color(239.0f / 255.0f, 50.0f / 255.0f, 61.0f / 255.0f), new Color(1.0f, 1.0f, 1.0f));
        }

        private static ColorScheme dark() {
            return new ColorScheme(new Color(0.0f, 0.0f, 0.0f), new Color(1.0f, 1.0f, 1.0f));
        }
    }

    private record EarlyLoadingVisualOptions(
            String backgroundTexturePath,
            boolean backgroundPreserveAspectRatio,
            String logoTexturePath,
            int logoWidth,
            int logoHeight,
            int logoOffsetX,
            int logoOffsetY,
            String barBackgroundTexturePath,
            String barProgressTexturePath,
            int barWidth,
            int barHeight,
            int barOffsetX,
            int barOffsetY,
            int windowWidthOverride,
            int windowHeightOverride,
            boolean hideLogo,
            boolean hideBar,
            boolean hideLogger,
            String topLeftTexturePath,
            int topLeftWidth,
            int topLeftHeight,
            int topLeftOffsetX,
            int topLeftOffsetY,
            String topRightTexturePath,
            int topRightWidth,
            int topRightHeight,
            int topRightOffsetX,
            int topRightOffsetY,
            String bottomLeftTexturePath,
            int bottomLeftWidth,
            int bottomLeftHeight,
            int bottomLeftOffsetX,
            int bottomLeftOffsetY,
            String bottomRightTexturePath,
            int bottomRightWidth,
            int bottomRightHeight,
            int bottomRightOffsetX,
            int bottomRightOffsetY
    ) {
        private static EarlyLoadingVisualOptions from(Options options) {
            return new EarlyLoadingVisualOptions(
                    options.earlyLoadingBackgroundTexturePath.getValue(),
                    options.earlyLoadingBackgroundPreserveAspectRatio.getValue(),
                    options.earlyLoadingLogoTexturePath.getValue(),
                    options.earlyLoadingLogoWidth.getValue(),
                    options.earlyLoadingLogoHeight.getValue(),
                    options.earlyLoadingLogoPositionOffsetX.getValue(),
                    options.earlyLoadingLogoPositionOffsetY.getValue(),
                    options.earlyLoadingBarBackgroundTexturePath.getValue(),
                    options.earlyLoadingBarProgressTexturePath.getValue(),
                    options.earlyLoadingBarWidth.getValue(),
                    options.earlyLoadingBarHeight.getValue(),
                    options.earlyLoadingBarPositionOffsetX.getValue(),
                    options.earlyLoadingBarPositionOffsetY.getValue(),
                    options.earlyLoadingWindowWidth.getValue(),
                    options.earlyLoadingWindowHeight.getValue(),
                    options.earlyLoadingHideLogo.getValue(),
                    options.earlyLoadingHideBar.getValue(),
                    options.earlyLoadingHideLogger.getValue(),
                    options.earlyLoadingTopLeftWatermarkTexturePath.getValue(),
                    options.earlyLoadingTopLeftWatermarkTextureWidth.getValue(),
                    options.earlyLoadingTopLeftWatermarkTextureHeight.getValue(),
                    options.earlyLoadingTopLeftWatermarkTexturePositionOffsetX.getValue(),
                    options.earlyLoadingTopLeftWatermarkTexturePositionOffsetY.getValue(),
                    options.earlyLoadingTopRightWatermarkTexturePath.getValue(),
                    options.earlyLoadingTopRightWatermarkTextureWidth.getValue(),
                    options.earlyLoadingTopRightWatermarkTextureHeight.getValue(),
                    options.earlyLoadingTopRightWatermarkTexturePositionOffsetX.getValue(),
                    options.earlyLoadingTopRightWatermarkTexturePositionOffsetY.getValue(),
                    options.earlyLoadingBottomLeftWatermarkTexturePath.getValue(),
                    options.earlyLoadingBottomLeftWatermarkTextureWidth.getValue(),
                    options.earlyLoadingBottomLeftWatermarkTextureHeight.getValue(),
                    options.earlyLoadingBottomLeftWatermarkTexturePositionOffsetX.getValue(),
                    options.earlyLoadingBottomLeftWatermarkTexturePositionOffsetY.getValue(),
                    options.earlyLoadingBottomRightWatermarkTexturePath.getValue(),
                    options.earlyLoadingBottomRightWatermarkTextureWidth.getValue(),
                    options.earlyLoadingBottomRightWatermarkTextureHeight.getValue(),
                    options.earlyLoadingBottomRightWatermarkTexturePositionOffsetX.getValue(),
                    options.earlyLoadingBottomRightWatermarkTexturePositionOffsetY.getValue()
            );
        }
    }

    private record TextureSuppliers(
            @Nullable ResourceSupplier<ITexture> background,
            @Nullable ResourceSupplier<ITexture> logo,
            @Nullable ResourceSupplier<ITexture> barBackground,
            @Nullable ResourceSupplier<ITexture> barProgress,
            @Nullable ResourceSupplier<ITexture> topLeft,
            @Nullable ResourceSupplier<ITexture> topRight,
            @Nullable ResourceSupplier<ITexture> bottomLeft,
            @Nullable ResourceSupplier<ITexture> bottomRight
    ) {
        private TextureSuppliers(EarlyLoadingVisualOptions options) {
            this(
                    createSupplier(options.backgroundTexturePath()),
                    createSupplier(options.logoTexturePath()),
                    createSupplier(options.barBackgroundTexturePath()),
                    createSupplier(options.barProgressTexturePath()),
                    createSupplier(options.topLeftTexturePath()),
                    createSupplier(options.topRightTexturePath()),
                    createSupplier(options.bottomLeftTexturePath()),
                    createSupplier(options.bottomRightTexturePath())
            );
        }
    }
}
