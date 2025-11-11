package de.keksuccino.drippyloadingscreen.neoforge;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.DrippyUtils;
import de.keksuccino.drippyloadingscreen.Options;
import de.keksuccino.fancymenu.util.cycle.CommonCycles;
import de.keksuccino.fancymenu.util.file.FileFilter;
import de.keksuccino.fancymenu.util.file.type.FileMediaType;
import de.keksuccino.fancymenu.util.file.type.groups.FileTypeGroup;
import de.keksuccino.fancymenu.util.file.type.types.FileTypes;
import de.keksuccino.fancymenu.util.file.type.types.ImageFileType;
import de.keksuccino.fancymenu.util.rendering.ui.NonStackableOverlayUI;
import de.keksuccino.fancymenu.util.rendering.ui.UIBase;
import de.keksuccino.fancymenu.util.rendering.ui.contextmenu.v2.ContextMenu;
import de.keksuccino.fancymenu.util.rendering.ui.screen.resource.ResourceChooserScreen;
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
import net.neoforged.fml.loading.FMLConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mirrors the early-window renderer inside an in-game {@link Screen} so users can preview their setup without leaving
 * Minecraft. Rendering intentionally follows {@link de.keksuccino.drippyloadingscreen.earlywindow.window.DrippyEarlyWindowProvider}.
 */
public class EarlyLoadingEditorScreen extends Screen {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Component TITLE = Component.translatable("drippyloadingscreen.screen.early_loading_preview");
    private static final ResourceLocation MOJANG_LOGO_LOCATION = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/title/mojangstudios.png");
    private static final ResourceSupplier<ITexture> MOJANG_LOGO_SUPPLIER = createBundledSupplier(MOJANG_LOGO_LOCATION);
    private static final float MOJANG_LOGO_U_OVERLAP = 0.0625f;

    private static final String[] PLACEHOLDER_TEXTURE_VALUES = {
            "/config/fancymenu/assets/some_image.png",
            "/config/fancymenu/assets/some_bar_background_image.png",
            "/config/fancymenu/assets/some_bar_progress_image.png"
    };
    private static final FileFilter PNG_APNG_FILE_FILTER = file -> {
        if (file.isDirectory()) {
            return true;
        }
        String name = file.getName().toLowerCase(Locale.ROOT);
        return name.endsWith(".png") || name.endsWith(".apng");
    };
    private static final FileTypeGroup<ImageFileType> PNG_APNG_FILE_TYPES = FileTypeGroup.of(FileTypes.PNG_IMAGE, FileTypes.APNG_IMAGE);
    private static final String REFERENCE_SIZE_FILE = "early_window_reference.properties";

    private static final int DEFAULT_REFERENCE_WIDTH = 854;
    private static final int DEFAULT_REFERENCE_HEIGHT = 480;

    private static final float INDETERMINATE_SEGMENT_WIDTH = 0.3f;
    private static final float LOGGER_BASE_TEXT_SCALE = 1.35f;
    private static final float LOGGER_LINE_HEIGHT = 12.0f;
    private static final float LOGGER_MARGIN = 10.0f;
    private static final float LOGGER_MIN_UI_SCALE = 0.75f;
    private static final int WATERMARK_PLACEHOLDER_SIZE = 128;
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
    private static final int RESIZE_HANDLE_SIZE = 4;
    private static final float MIN_RESIZE_SIZE = 8.0f;
    private static final float ELEMENT_DRAG_CRUMPLE_ZONE = 5.0f;

    private EarlyLoadingVisualOptions visualOptions;
    private TextureSuppliers textureSuppliers;
    private ColorScheme colorScheme;
    private boolean referenceSizeLoaded;
    @Nullable
    private StoredReferenceSize cachedReferenceSize;

    private ContextMenu backgroundContextMenu;
    private ContextMenu logoContextMenu;
    private ContextMenu progressBarContextMenu;
    private final EnumMap<WatermarkAnchor, ContextMenu> watermarkContextMenus = new EnumMap<>(WatermarkAnchor.class);
    private final EnumMap<WatermarkAnchor, ElementBounds> watermarkBounds = new EnumMap<>(WatermarkAnchor.class);
    private final EnumMap<SelectableElement, ElementGeometry> elementGeometries = new EnumMap<>(SelectableElement.class);
    private final List<ContextMenu> contextMenus = new ArrayList<>();
    private ElementBounds backgroundBounds;
    private ElementBounds logoBounds;
    private ElementBounds progressBarBounds;
    @Nullable
    private SelectableElement selectedElement;
    @Nullable
    private ResizeSession activeResize;
    @Nullable
    private MoveSession activeMove;
    @Nullable
    private PendingMove pendingMove;
    @Nullable
    private RenderMetrics lastRenderMetrics;
    private float lastUiScale = 1.0f;
    private float lastProgressDefaultY;

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

    public EarlyLoadingEditorScreen() {
        super(TITLE);
        this.visualOptions = EarlyLoadingVisualOptions.from(DrippyLoadingScreen.getOptions());
        this.textureSuppliers = new TextureSuppliers(this.visualOptions);
        this.colorScheme = resolveColorScheme();
        this.baseWidth = resolveReferenceWidth();
        this.baseHeight = resolveReferenceHeight();
    }

    @Override
    protected void init() {
        super.init();
        syncVisualOptionsFromConfig();
        this.baseWidth = resolveReferenceWidth();
        this.baseHeight = resolveReferenceHeight();
        rebuildContextMenus();
    }

    @Override
    public void tick() {
        super.tick();
        syncVisualOptionsFromConfig();
        this.colorScheme = resolveColorScheme();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        this.backgroundBounds = new ElementBounds(0.0f, 0.0f, this.width, this.height);
        this.logoBounds = null;
        this.progressBarBounds = null;
        this.watermarkBounds.clear();
        this.elementGeometries.clear();
        updateProgressMetrics();
        RenderMetrics metrics = captureRenderMetrics();
        this.lastRenderMetrics = metrics;
        renderBackgroundLayer(graphics, metrics);
        float uiScale = computeUiScale(metrics);
        this.lastUiScale = Math.max(0.001f, uiScale);
        float logoBottom = renderLogoLayer(graphics, metrics, uiScale);
        renderProgressBar(graphics, metrics, logoBottom, uiScale);
        renderWatermarks(graphics, metrics, uiScale);
        renderLoggerOverlay(graphics, metrics, uiScale);
        renderElementHoverIndicators(graphics, mouseX, mouseY);
        if (this.selectedElement != null && !this.elementGeometries.containsKey(this.selectedElement)) {
            setSelectedElement(null);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Background is fully controlled by render().
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0) {
            if (tryBeginResize(mouseX, mouseY)) {
                return true;
            }
            SelectableElement hit = hitTestElement(mouseX, mouseY);
            if (hit != null) {
                setSelectedElement(hit);
                prepareMoveSession(hit, mouseX, mouseY);
                if (!isAnyContextMenuHovered()) {
                    closeAllContextMenus();
                }
                return true;
            }
            if (this.selectedElement != null) {
                setSelectedElement(null);
            }
        }
        if (button == 1 && openContextMenuAt(mouseX, mouseY)) {
            return true;
        }
        if (button == 0 && !isAnyContextMenuHovered()) {
            closeAllContextMenus();
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0) {
            if (this.activeResize != null) {
                handleResizeDrag(mouseX, mouseY);
                return true;
            }
            ensureMoveSessionStarted(mouseX, mouseY);
            if (this.activeMove != null) {
                handleMoveDrag(mouseX, mouseY);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            boolean handled = false;
            if (this.activeResize != null) {
                this.activeResize = null;
                handled = true;
            }
            if (this.activeMove != null) {
                this.activeMove = null;
                handled = true;
            }
            if (this.pendingMove != null) {
                this.pendingMove = null;
                handled = true;
            }
            if (handled) {
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        super.removed();
        closeAllContextMenus();
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
        LogoTexture logo = resolveLogoTexture();
        TextureInfo logoTexture = logo.texture();
        float scaledOffsetY = this.visualOptions.logoOffsetY() * uiScale;
        int textureWidth = logoTexture.isValid() ? logoTexture.width() : 0;
        int textureHeight = logoTexture.isValid() ? logoTexture.height() : 0;
        float baseWidth = this.visualOptions.logoWidth() > 0 ? this.visualOptions.logoWidth() : textureWidth;
        float baseHeight = this.visualOptions.logoHeight() > 0 ? this.visualOptions.logoHeight() : textureHeight;
        float width = Math.max(1.0f, baseWidth * uiScale);
        float height = Math.max(1.0f, baseHeight * uiScale);
        float offsetX = this.visualOptions.logoOffsetX() * uiScale;
        float x = (metrics.absoluteWidth() - width) / 2.0f + offsetX;
        float baseline = metrics.absoluteHeight() * 0.35f;
        float y = baseline + scaledOffsetY;
        float guiX = metrics.toGui(x);
        float guiY = metrics.toGui(y);
        float guiWidth = metrics.toGui(width);
        float guiHeight = metrics.toGui(height);
        this.logoBounds = new ElementBounds(guiX, guiY, guiWidth, guiHeight);
        registerElementGeometry(SelectableElement.LOGO, this.logoBounds, x, y, width, height);
        if (this.visualOptions.hideLogo()) {
            drawPlaceholderOverlay(graphics, guiX, guiY, guiWidth, guiHeight);
            return y + height;
        }
        if (!logoTexture.isValid()) {
            return y + height;
        }
        if (logo.useBundledLayout()) {
            drawBundledMojangLogo(graphics, logoTexture, guiX, guiY, guiWidth, guiHeight);
        } else {
            drawTexture(graphics, logoTexture, guiX, guiY, guiWidth, guiHeight);
        }
        return y + height;
    }

    private void renderProgressBar(GuiGraphics graphics, RenderMetrics metrics, float logoBottom, float uiScale) {
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
        this.lastProgressDefaultY = defaultY;
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
        this.progressBarBounds = new ElementBounds(guiBaseX, guiBaseY, guiWidth, guiHeight);
        registerElementGeometry(SelectableElement.PROGRESS_BAR, this.progressBarBounds, baseX, baseY, width, height);
        if (this.visualOptions.hideBar()) {
            drawPlaceholderOverlay(graphics, guiBaseX, guiBaseY, guiWidth, guiHeight);
            return;
        }
        TextureInfo barBackground = fetchTexture(this.textureSuppliers.barBackground());
        TextureInfo barProgress = fetchTexture(this.textureSuppliers.barProgress());
        boolean vanillaBar = !barBackground.isValid() && !barProgress.isValid();
        ProgressFrameMetrics frameMetrics = vanillaBar ? computeProgressFrameMetrics(guiWidth, guiHeight) : null;
        if (barBackground.isValid()) {
            drawTexture(graphics, barBackground, guiBaseX, guiBaseY, guiWidth, guiHeight);
        } else if (vanillaBar && frameMetrics != null) {
            drawVanillaProgressFrame(graphics, guiBaseX, guiBaseY, guiWidth, guiHeight, frameMetrics);
        } else {
            drawSolidRect(graphics, guiBaseX, guiBaseY, guiWidth, guiHeight, this.colorScheme.background().withBrightness(0.5f), 0.9f);
            drawOutline(graphics, guiBaseX, guiBaseY, guiWidth, guiHeight, this.colorScheme.foreground(), 1.0f);
        }

        if (this.progressIndeterminate) {
            drawIndeterminateProgress(graphics, guiBaseX, guiBaseY, guiWidth, guiHeight, frameMetrics, barProgress);
        } else {
            float clamped = Mth.clamp(this.displayedProgress, 0.0f, 1.0f);
            drawProgressSegment(graphics, guiBaseX, guiBaseY, guiWidth, guiHeight, 0.0f, clamped, frameMetrics, barProgress);
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

    private void renderElementHoverIndicators(GuiGraphics graphics, double mouseX, double mouseY) {
        if (this.elementGeometries.isEmpty()) {
            return;
        }
        int hoverColor = UIBase.getUIColorTheme().layout_editor_element_border_color_normal.getColorInt();
        int selectedColor = UIBase.getUIColorTheme().layout_editor_element_border_color_selected.getColorInt();
        for (SelectableElement element : SelectableElement.values()) {
            ElementGeometry geometry = this.elementGeometries.get(element);
            if (geometry == null) {
                continue;
            }
            boolean isSelected = element == this.selectedElement;
            boolean hovered = geometry.bounds().contains(mouseX, mouseY);
            ContextMenu menu = getContextMenuFor(element);
            boolean menuOpen = menu != null && menu.isOpen();
            if (!isSelected && !hovered && !menuOpen) {
                continue;
            }
            int color = isSelected ? selectedColor : hoverColor;
            drawEditorHoverBorder(graphics, geometry.bounds(), color);
            if (isSelected) {
                drawResizeHandles(graphics, geometry.bounds(), color);
            }
        }
    }

    private void drawEditorHoverBorder(GuiGraphics graphics, ElementBounds bounds, int argbColor) {
        int left = Math.round(bounds.x());
        int top = Math.round(bounds.y());
        int right = Math.round(bounds.x() + bounds.width());
        int bottom = Math.round(bounds.y() + bounds.height());
        if ((right - left) < 2 || (bottom - top) < 2) {
            return;
        }
        RenderSystem.enableBlend();
        graphics.fill(left + 1, top, right - 1, top + 1, argbColor);
        graphics.fill(left + 1, bottom - 1, right - 1, bottom, argbColor);
        graphics.fill(left, top, left + 1, bottom, argbColor);
        graphics.fill(right - 1, top, right, bottom, argbColor);
    }

    private void drawResizeHandles(GuiGraphics graphics, ElementBounds bounds, int argbColor) {
        int half = RESIZE_HANDLE_SIZE / 2;
        float left = bounds.x();
        float top = bounds.y();
        float right = bounds.x() + bounds.width();
        float bottom = bounds.y() + bounds.height();
        float centerX = (left + right) / 2.0f;
        float centerY = (top + bottom) / 2.0f;
        drawHandle(graphics, left, top, half, argbColor);
        drawHandle(graphics, centerX, top, half, argbColor);
        drawHandle(graphics, right, top, half, argbColor);
        drawHandle(graphics, left, centerY, half, argbColor);
        drawHandle(graphics, right, centerY, half, argbColor);
        drawHandle(graphics, left, bottom, half, argbColor);
        drawHandle(graphics, centerX, bottom, half, argbColor);
        drawHandle(graphics, right, bottom, half, argbColor);
    }

    private void drawHandle(GuiGraphics graphics, float centerX, float centerY, int half, int color) {
        int cx = Math.round(centerX);
        int cy = Math.round(centerY);
        RenderSystem.enableBlend();
        graphics.fill(cx - half, cy - half, cx + half, cy + half, color);
    }

    private void renderWatermark(GuiGraphics graphics, RenderMetrics metrics, @Nullable ResourceSupplier<ITexture> supplier, int configuredWidth, int configuredHeight,
                                 int offsetX, int offsetY, WatermarkAnchor anchor, float uiScale) {
        TextureInfo texture = fetchTexture(supplier);
        boolean hasTexture = texture.isValid();
        int fallbackWidth = hasTexture && texture.width() > 0 ? texture.width() : WATERMARK_PLACEHOLDER_SIZE;
        int fallbackHeight = hasTexture && texture.height() > 0 ? texture.height() : WATERMARK_PLACEHOLDER_SIZE;
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
        float guiX = metrics.toGui(x);
       float guiY = metrics.toGui(y);
       float guiWidth = metrics.toGui(width);
       float guiHeight = metrics.toGui(height);
        ElementBounds bounds = new ElementBounds(guiX, guiY, guiWidth, guiHeight);
        this.watermarkBounds.put(anchor, bounds);
        registerElementGeometry(SelectableElement.fromAnchor(anchor), bounds, x, y, width, height);
        if (hasTexture) {
            drawTexture(graphics, texture, guiX, guiY, guiWidth, guiHeight);
        } else {
            drawPlaceholderOverlay(graphics, guiX, guiY, guiWidth, guiHeight);
        }
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
        float guiAdjustedScale = textScale * metrics.guiScaleFactor();
        if (guiAdjustedScale <= 0.0f) {
            graphics.pose().popPose();
            return;
        }
        graphics.pose().scale(guiAdjustedScale, guiAdjustedScale, 1.0f);
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

    private void drawIndeterminateProgress(GuiGraphics graphics, float x, float y, float width, float height,
                                           ProgressFrameMetrics frameMetrics, TextureInfo progressTexture) {
        float start = this.indeterminateOffset;
        float end = start + INDETERMINATE_SEGMENT_WIDTH;
        if (end <= 1.0f) {
            drawProgressSegment(graphics, x, y, width, height, start, end, frameMetrics, progressTexture);
        } else {
            drawProgressSegment(graphics, x, y, width, height, start, 1.0f, frameMetrics, progressTexture);
            drawProgressSegment(graphics, x, y, width, height, 0.0f, end - 1.0f, frameMetrics, progressTexture);
        }
    }

    private void drawProgressSegment(GuiGraphics graphics, float baseX, float baseY, float width, float height,
                                     float start, float end, ProgressFrameMetrics frameMetrics, TextureInfo progressTexture) {
        if (end <= start) {
            return;
        }
        if (progressTexture.isValid()) {
            drawProgressTextureClipped(graphics, progressTexture, baseX, baseY, width, height, start, end);
        } else if (frameMetrics != null) {
            drawVanillaProgressSegment(graphics, baseX, baseY, width, height, start, end, frameMetrics);
        } else {
            float segmentWidth = width * (end - start);
            float segmentX = baseX + width * start;
            drawSolidRect(graphics, segmentX, baseY, segmentWidth, height, this.colorScheme.foreground(), 1.0f);
        }
    }

    private ProgressFrameMetrics computeProgressFrameMetrics(float width, float height) {
        float safeWidth = Math.max(1.0f, width);
        float safeHeight = Math.max(1.0f, height);
        float maxBorder = Math.min(safeWidth, safeHeight) / 2.0f;
        float border = Math.min(1.0f, maxBorder);
        float horizontalInset = Math.min(2.0f, Math.max(border, (safeWidth - border * 2.0f) / 2.0f));
        float verticalInset = Math.min(2.0f, Math.max(border, (safeHeight - border * 2.0f) / 2.0f));
        return new ProgressFrameMetrics(border, horizontalInset, verticalInset);
    }

    private void drawVanillaProgressFrame(GuiGraphics graphics, float baseX, float baseY, float width, float height,
                                          ProgressFrameMetrics metrics) {
        if (metrics == null) {
            return;
        }
        Color color = this.colorScheme.foreground();
        float border = Math.max(0.0f, metrics.borderThickness());
        float horizontalWidth = Math.max(border, width - border * 2.0f);
        drawSolidRect(graphics, baseX, baseY, border, height, color, 1.0f);
        drawSolidRect(graphics, baseX + width - border, baseY, border, height, color, 1.0f);
        drawSolidRect(graphics, baseX + border, baseY, horizontalWidth, border, color, 1.0f);
        drawSolidRect(graphics, baseX + border, baseY + height - border, horizontalWidth, border, color, 1.0f);
    }

    private void drawVanillaProgressSegment(GuiGraphics graphics, float baseX, float baseY, float width, float height,
                                            float start, float end, ProgressFrameMetrics metrics) {
        if (metrics == null) {
            return;
        }
        float clampedStart = Mth.clamp(start, 0.0f, 1.0f);
        float clampedEnd = Mth.clamp(end, 0.0f, 1.0f);
        if (clampedEnd <= clampedStart) {
            return;
        }
        float innerLeft = baseX + metrics.horizontalInset();
        float innerRight = baseX + width - metrics.horizontalInset();
        float innerWidth = Math.max(0.0f, innerRight - innerLeft);
        if (innerWidth <= 0.0f) {
            return;
        }
        float x0 = innerLeft + innerWidth * clampedStart;
        float x1 = innerLeft + innerWidth * clampedEnd;
        float segmentWidth = Math.max(0.0f, x1 - x0);
        float innerTop = baseY + metrics.verticalInset();
        float innerHeight = Math.max(0.0f, height - metrics.verticalInset() * 2.0f);
        if (segmentWidth <= 0.0f || innerHeight <= 0.0f) {
            return;
        }
        drawSolidRect(graphics, x0, innerTop, segmentWidth, innerHeight, this.colorScheme.foreground(), 1.0f);
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

    private void drawBundledMojangLogo(GuiGraphics graphics, TextureInfo texture, float x, float y, float width, float height) {
        if (!texture.isValid()) {
            return;
        }
        int drawX = Math.round(x);
        int drawY = Math.round(y);
        int totalWidth = Math.max(1, Math.round(width));
        int totalHeight = Math.max(1, Math.round(height));
        int leftWidth = Math.max(1, totalWidth / 2);
        int rightWidth = Math.max(1, totalWidth - leftWidth);
        int textureWidth = Math.max(1, texture.width());
        int textureHeight = Math.max(1, texture.height());
        int topPixels = Math.max(1, textureHeight / 2);
        int bottomPixels = Math.max(1, textureHeight - topPixels);
        RenderSystem.enableBlend();
        graphics.blit(texture.location(), drawX, drawY, leftWidth, totalHeight, -MOJANG_LOGO_U_OVERLAP, 0.0f, textureWidth, topPixels, textureWidth, textureHeight);
        graphics.blit(texture.location(), drawX + leftWidth, drawY, rightWidth, totalHeight, MOJANG_LOGO_U_OVERLAP, topPixels, textureWidth, bottomPixels, textureWidth, textureHeight);
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

    private void drawPlaceholderOverlay(GuiGraphics graphics, float x, float y, float width, float height) {
        if (width <= 1.0f || height <= 1.0f) {
            return;
        }
        Color faded = this.colorScheme.foreground().withBrightness(0.4f);
        drawSolidRect(graphics, x, y, width, height, faded, 0.25f);
        drawOutline(graphics, x, y, width, height, this.colorScheme.foreground(), 0.6f);

        int left = Math.round(x);
        int top = Math.round(y);
        int right = Math.max(left + 1, Math.round(x + width));
        int bottom = Math.max(top + 1, Math.round(y + height));
        graphics.enableScissor(left, top, right, bottom);
        graphics.pose().pushPose();
        graphics.pose().translate(x + width / 2.0f, y + height / 2.0f, 0.0f);
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(-45.0f));
        float span = (float) Math.hypot(width, height) + 20.0f;
        float stripeSpacing = 20.0f;
        float stripeThickness = 6.0f;
        int stripeColor = toArgb(this.colorScheme.foreground(), 0.45f);
        for (float offset = -span; offset <= span; offset += stripeSpacing) {
            int minX = Math.round(offset - stripeThickness / 2.0f);
            int maxX = Math.round(offset + stripeThickness / 2.0f);
            graphics.fill(minX, Math.round(-span), maxX, Math.round(span), stripeColor);
        }
        graphics.pose().popPose();
        graphics.disableScissor();
    }

    private void registerElementGeometry(SelectableElement element, ElementBounds bounds, float absoluteX, float absoluteY, float absoluteWidth, float absoluteHeight) {
        this.elementGeometries.put(element, new ElementGeometry(bounds, absoluteX, absoluteY, absoluteWidth, absoluteHeight));
    }

    @Nullable
    private SelectableElement hitTestElement(double mouseX, double mouseY) {
        if (this.progressBarBounds != null && this.progressBarBounds.contains(mouseX, mouseY)) {
            return SelectableElement.PROGRESS_BAR;
        }
        if (this.logoBounds != null && this.logoBounds.contains(mouseX, mouseY)) {
            return SelectableElement.LOGO;
        }
        for (WatermarkAnchor anchor : WatermarkAnchor.values()) {
            ElementBounds bounds = this.watermarkBounds.get(anchor);
            if (bounds != null && bounds.contains(mouseX, mouseY)) {
                return SelectableElement.fromAnchor(anchor);
            }
        }
        return null;
    }

    private void setSelectedElement(@Nullable SelectableElement element) {
        if (this.selectedElement == element) {
            return;
        }
        this.selectedElement = element;
        this.activeResize = null;
        this.activeMove = null;
        this.pendingMove = null;
        if (element == null) {
            return;
        }
    }

    private boolean tryBeginResize(double mouseX, double mouseY) {
        if (this.selectedElement == null) {
            return false;
        }
        ElementGeometry geometry = this.elementGeometries.get(this.selectedElement);
        if (geometry == null) {
            return false;
        }
        ResizeHandle handle = detectHandle(geometry.bounds(), mouseX, mouseY);
        if (handle == null) {
            return false;
        }
        float left = geometry.absoluteX();
        float top = geometry.absoluteY();
        float right = left + geometry.width();
        float bottom = top + geometry.height();
        this.activeMove = null;
        this.pendingMove = null;
        this.activeResize = new ResizeSession(this.selectedElement, handle, left, top, right, bottom);
        closeAllContextMenus();
        return true;
    }

    private void prepareMoveSession(SelectableElement element, double mouseX, double mouseY) {
        if (this.lastRenderMetrics == null) {
            this.activeMove = null;
            this.pendingMove = null;
            return;
        }
        ElementGeometry geometry = this.elementGeometries.get(element);
        if (geometry == null) {
            this.activeMove = null;
            this.pendingMove = null;
            return;
        }
        float guiFactor = Math.max(this.lastRenderMetrics.guiScaleFactor(), 0.0001f);
        float mouseAbsX = (float) (mouseX / guiFactor);
        float mouseAbsY = (float) (mouseY / guiFactor);
        float grabOffsetX = mouseAbsX - geometry.absoluteX();
        float grabOffsetY = mouseAbsY - geometry.absoluteY();
        this.activeResize = null;
        this.activeMove = null;
        this.pendingMove = new PendingMove(element, mouseAbsX, mouseAbsY, grabOffsetX, grabOffsetY, geometry.width(), geometry.height());
    }

    private void ensureMoveSessionStarted(double mouseX, double mouseY) {
        if (this.pendingMove == null || this.lastRenderMetrics == null) {
            return;
        }
        float guiFactor = Math.max(this.lastRenderMetrics.guiScaleFactor(), 0.0001f);
        float mouseAbsX = (float) (mouseX / guiFactor);
        float mouseAbsY = (float) (mouseY / guiFactor);
        float deltaX = Math.abs(mouseAbsX - this.pendingMove.initialMouseX());
        float deltaY = Math.abs(mouseAbsY - this.pendingMove.initialMouseY());
        if (deltaX < ELEMENT_DRAG_CRUMPLE_ZONE && deltaY < ELEMENT_DRAG_CRUMPLE_ZONE) {
            return;
        }
        this.activeMove = new MoveSession(
                this.pendingMove.element(),
                this.pendingMove.grabOffsetX(),
                this.pendingMove.grabOffsetY(),
                this.pendingMove.width(),
                this.pendingMove.height()
        );
        this.pendingMove = null;
    }

    @Nullable
    private ResizeHandle detectHandle(ElementBounds bounds, double mouseX, double mouseY) {
        int half = RESIZE_HANDLE_SIZE / 2;
        float left = bounds.x();
        float top = bounds.y();
        float right = bounds.x() + bounds.width();
        float bottom = bounds.y() + bounds.height();
        float centerX = (left + right) / 2.0f;
        float centerY = (top + bottom) / 2.0f;
        if (isWithinHandle(left, top, mouseX, mouseY, half)) {
            return ResizeHandle.TOP_LEFT;
        }
        if (isWithinHandle(right, top, mouseX, mouseY, half)) {
            return ResizeHandle.TOP_RIGHT;
        }
        if (isWithinHandle(left, bottom, mouseX, mouseY, half)) {
            return ResizeHandle.BOTTOM_LEFT;
        }
        if (isWithinHandle(right, bottom, mouseX, mouseY, half)) {
            return ResizeHandle.BOTTOM_RIGHT;
        }
        if (isWithinHandle(centerX, top, mouseX, mouseY, half)) {
            return ResizeHandle.TOP;
        }
        if (isWithinHandle(centerX, bottom, mouseX, mouseY, half)) {
            return ResizeHandle.BOTTOM;
        }
        if (isWithinHandle(left, centerY, mouseX, mouseY, half)) {
            return ResizeHandle.LEFT;
        }
        if (isWithinHandle(right, centerY, mouseX, mouseY, half)) {
            return ResizeHandle.RIGHT;
        }
        return null;
    }

    private boolean isWithinHandle(float centerX, float centerY, double mouseX, double mouseY, int half) {
        double minX = centerX - half;
        double maxX = centerX + half;
        double minY = centerY - half;
        double maxY = centerY + half;
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    private void handleResizeDrag(double mouseX, double mouseY) {
        if (this.activeResize == null || this.lastRenderMetrics == null) {
            return;
        }
        float guiFactor = Math.max(this.lastRenderMetrics.guiScaleFactor(), 0.0001f);
        float mouseAbsX = (float) (mouseX / guiFactor);
        float mouseAbsY = (float) (mouseY / guiFactor);
        float clampedX = Mth.clamp(mouseAbsX, 0.0f, this.lastRenderMetrics.absoluteWidth());
        float clampedY = Mth.clamp(mouseAbsY, 0.0f, this.lastRenderMetrics.absoluteHeight());
        float left = this.activeResize.initialLeft();
        float right = this.activeResize.initialRight();
        if (this.activeResize.handle().adjustsLeft()) {
            left = Math.min(clampedX, this.activeResize.initialRight() - MIN_RESIZE_SIZE);
        }
        if (this.activeResize.handle().adjustsRight()) {
            right = Math.max(clampedX, left + MIN_RESIZE_SIZE);
        }
        float top = this.activeResize.initialTop();
        float bottom = this.activeResize.initialBottom();
        if (this.activeResize.handle().adjustsTop()) {
            top = Math.min(clampedY, this.activeResize.initialBottom() - MIN_RESIZE_SIZE);
        }
        if (this.activeResize.handle().adjustsBottom()) {
            bottom = Math.max(clampedY, top + MIN_RESIZE_SIZE);
        }
        float width = Math.max(MIN_RESIZE_SIZE, right - left);
        float height = Math.max(MIN_RESIZE_SIZE, bottom - top);
        applyResizedGeometry(this.activeResize.element(), left, top, width, height);
    }

    private void handleMoveDrag(double mouseX, double mouseY) {
        if (this.activeMove == null || this.lastRenderMetrics == null) {
            return;
        }
        float guiFactor = Math.max(this.lastRenderMetrics.guiScaleFactor(), 0.0001f);
        float mouseAbsX = (float) (mouseX / guiFactor);
        float mouseAbsY = (float) (mouseY / guiFactor);
        float newLeft = mouseAbsX - this.activeMove.grabOffsetX();
        float newTop = mouseAbsY - this.activeMove.grabOffsetY();
        float width = this.activeMove.width();
        float height = this.activeMove.height();
        float maxLeft = Math.max(0.0f, this.lastRenderMetrics.absoluteWidth() - width);
        float maxTop = Math.max(0.0f, this.lastRenderMetrics.absoluteHeight() - height);
        newLeft = Mth.clamp(newLeft, 0.0f, maxLeft);
        newTop = Mth.clamp(newTop, 0.0f, maxTop);
        applyResizedGeometry(this.activeMove.element(), newLeft, newTop, width, height);
    }

    private void applyResizedGeometry(SelectableElement element, float newLeft, float newTop, float newWidth, float newHeight) {
        if (this.lastRenderMetrics == null) {
            return;
        }
        float uiScale = Math.max(0.001f, this.lastUiScale);
        Options options = DrippyLoadingScreen.getOptions();
        float screenWidth = this.lastRenderMetrics.absoluteWidth();
        float screenHeight = this.lastRenderMetrics.absoluteHeight();
        switch (element) {
            case LOGO -> {
                int widthConfig = Math.max(1, Math.round(newWidth / uiScale));
                int heightConfig = Math.max(1, Math.round(newHeight / uiScale));
                float midpoint = (screenWidth - newWidth) / 2.0f;
                int offsetX = Math.round((newLeft - midpoint) / uiScale);
                float baseline = screenHeight * 0.35f;
                int offsetY = Math.round((newTop - baseline) / uiScale);
                applyOptionChange(() -> {
                    options.earlyLoadingLogoWidth.setValue(widthConfig);
                    options.earlyLoadingLogoHeight.setValue(heightConfig);
                    options.earlyLoadingLogoPositionOffsetX.setValue(offsetX);
                    options.earlyLoadingLogoPositionOffsetY.setValue(offsetY);
                });
            }
            case PROGRESS_BAR -> {
                int widthConfig = Math.max(32, Math.round(newWidth / uiScale));
                int heightConfig = Math.max(6, Math.round(newHeight / uiScale));
                float midpoint = (screenWidth - newWidth) / 2.0f;
                int offsetX = Math.round((newLeft - midpoint) / uiScale);
                float defaultY = this.lastProgressDefaultY;
                if (!Float.isFinite(defaultY)) {
                    defaultY = newTop;
                }
                int offsetY = Math.round((newTop - defaultY) / uiScale);
                applyOptionChange(() -> {
                    options.earlyLoadingBarWidth.setValue(widthConfig);
                    options.earlyLoadingBarHeight.setValue(heightConfig);
                    options.earlyLoadingBarPositionOffsetX.setValue(offsetX);
                    options.earlyLoadingBarPositionOffsetY.setValue(offsetY);
                });
            }
            case WATERMARK_TOP_LEFT, WATERMARK_TOP_RIGHT, WATERMARK_BOTTOM_LEFT, WATERMARK_BOTTOM_RIGHT -> {
                WatermarkAnchor anchor = element.watermarkAnchor();
                if (anchor == null) {
                    return;
                }
                WatermarkOptionAccess access = resolveWatermarkOptions(options, anchor);
                int widthConfig = Math.max(1, Math.round(newWidth / uiScale));
                int heightConfig = Math.max(1, Math.round(newHeight / uiScale));
                int offsetX;
                int offsetY;
                switch (anchor) {
                    case TOP_LEFT -> {
                        offsetX = Math.round(newLeft / uiScale);
                        offsetY = Math.round(newTop / uiScale);
                    }
                    case TOP_RIGHT -> {
                        offsetX = Math.round((newLeft - (screenWidth - newWidth)) / uiScale);
                        offsetY = Math.round(newTop / uiScale);
                    }
                    case BOTTOM_LEFT -> {
                        offsetX = Math.round(newLeft / uiScale);
                        offsetY = Math.round((newTop - (screenHeight - newHeight)) / uiScale);
                    }
                    case BOTTOM_RIGHT -> {
                        offsetX = Math.round((newLeft - (screenWidth - newWidth)) / uiScale);
                        offsetY = Math.round((newTop - (screenHeight - newHeight)) / uiScale);
                    }
                    default -> {
                        offsetX = 0;
                        offsetY = 0;
                    }
                }
                applyOptionChange(() -> {
                    access.width.setValue(widthConfig);
                    access.height.setValue(heightConfig);
                    access.offsetX.setValue(offsetX);
                    access.offsetY.setValue(offsetY);
                });
            }
        }
    }

    @Nullable
    private ContextMenu getContextMenuFor(SelectableElement element) {
        return switch (element) {
            case LOGO -> this.logoContextMenu;
            case PROGRESS_BAR -> this.progressBarContextMenu;
            case WATERMARK_TOP_LEFT -> this.watermarkContextMenus.get(WatermarkAnchor.TOP_LEFT);
            case WATERMARK_TOP_RIGHT -> this.watermarkContextMenus.get(WatermarkAnchor.TOP_RIGHT);
            case WATERMARK_BOTTOM_LEFT -> this.watermarkContextMenus.get(WatermarkAnchor.BOTTOM_LEFT);
            case WATERMARK_BOTTOM_RIGHT -> this.watermarkContextMenus.get(WatermarkAnchor.BOTTOM_RIGHT);
        };
    }

    private void rebuildContextMenus() {
        for (ContextMenu menu : this.contextMenus) {
            this.removeWidget(menu);
        }
        this.contextMenus.clear();
        this.watermarkContextMenus.clear();
        this.backgroundContextMenu = createBackgroundContextMenu();
        this.logoContextMenu = createLogoContextMenu();
        this.progressBarContextMenu = createProgressBarContextMenu();
        registerContextMenu(this.backgroundContextMenu);
        registerContextMenu(this.logoContextMenu);
        registerContextMenu(this.progressBarContextMenu);
        for (WatermarkAnchor anchor : WatermarkAnchor.values()) {
            ContextMenu menu = createWatermarkContextMenu(anchor);
            this.watermarkContextMenus.put(anchor, menu);
            registerContextMenu(menu);
        }
    }

    private void registerContextMenu(@Nullable ContextMenu menu) {
        if (menu == null) {
            return;
        }
        this.contextMenus.add(menu);
        this.addRenderableWidget(menu);
    }

    private ContextMenu createBackgroundContextMenu() {

        ContextMenu menu = new ContextMenu().setForceUIScale(true);
        Options options = DrippyLoadingScreen.getOptions();

        addImageChooserEntry(menu, "background_set_image", Component.translatable("drippyloadingscreen.early_loading.context.background.set_image"), options.earlyLoadingBackgroundTexturePath);
        menu.addValueCycleEntry("background_preserve_aspect_ratio",
                        CommonCycles.cycleEnabledDisabled("drippyloadingscreen.early_loading.context.background.preserve_aspect_ratio", options.earlyLoadingBackgroundPreserveAspectRatio.getValue())
                                .addCycleListener(value -> applyOptionChange(() -> options.earlyLoadingBackgroundPreserveAspectRatio.setValue(value.getAsBoolean()))));

        menu.addSeparatorEntry("separator_after_image");

        ContextMenu windowSizeMenu = new ContextMenu();
        menu.addSubMenuEntry("background_window_size", Component.translatable("drippyloadingscreen.early_loading.context.background.window_size"), windowSizeMenu);
        addIntegerInputEntry(windowSizeMenu, "background_window_width", Component.translatable("drippyloadingscreen.early_loading.context.common.width"), options.earlyLoadingWindowWidth);
        addIntegerInputEntry(windowSizeMenu, "background_window_height", Component.translatable("drippyloadingscreen.early_loading.context.common.height"), options.earlyLoadingWindowHeight);

        addStringInputEntry(menu, "background_window_title", Component.translatable("drippyloadingscreen.early_loading.context.background.window_title"), options.earlyLoadingWindowTitle);

        menu.addSeparatorEntry("separator_after_window_title");

        menu.addValueCycleEntry("background_hide_logger",
                        CommonCycles.cycleEnabledDisabled("drippyloadingscreen.early_loading.context.background.hide_logger", options.earlyLoadingHideLogger.getValue())
                                .addCycleListener(value -> applyOptionChange(() -> options.earlyLoadingHideLogger.setValue(value.getAsBoolean()))));

        return menu;

    }

    private ContextMenu createLogoContextMenu() {

        ContextMenu menu = new ContextMenu().setForceUIScale(true);
        Options options = DrippyLoadingScreen.getOptions();

        addImageChooserEntry(menu, "logo_set_image", Component.translatable("drippyloadingscreen.early_loading.context.logo.set_image"), options.earlyLoadingLogoTexturePath);

        menu.addSeparatorEntry("separator_after_image");

        ContextMenu sizeMenu = new ContextMenu();
        menu.addSubMenuEntry("logo_size", Component.translatable("drippyloadingscreen.early_loading.context.logo.size"), sizeMenu);
        addIntegerInputEntry(sizeMenu, "logo_width", Component.translatable("drippyloadingscreen.early_loading.context.common.width"), options.earlyLoadingLogoWidth);
        addIntegerInputEntry(sizeMenu, "logo_height", Component.translatable("drippyloadingscreen.early_loading.context.common.height"), options.earlyLoadingLogoHeight);

        ContextMenu offsetMenu = new ContextMenu();
        menu.addSubMenuEntry("logo_offset", Component.translatable("drippyloadingscreen.early_loading.context.logo.offset"), offsetMenu);
        addIntegerInputEntry(offsetMenu, "logo_offset_x", Component.translatable("drippyloadingscreen.early_loading.context.common.offset_x"), options.earlyLoadingLogoPositionOffsetX);
        addIntegerInputEntry(offsetMenu, "logo_offset_y", Component.translatable("drippyloadingscreen.early_loading.context.common.offset_y"), options.earlyLoadingLogoPositionOffsetY);

        menu.addSeparatorEntry("separator_after_offset");

        menu.addValueCycleEntry("logo_hide",
                        CommonCycles.cycleEnabledDisabled("drippyloadingscreen.early_loading.context.common.hide_element", options.earlyLoadingHideLogo.getValue())
                                .addCycleListener(value -> applyOptionChange(() -> options.earlyLoadingHideLogo.setValue(value.getAsBoolean()))));

        return menu;

    }

    private ContextMenu createProgressBarContextMenu() {

        ContextMenu menu = new ContextMenu().setForceUIScale(true);
        Options options = DrippyLoadingScreen.getOptions();

        addImageChooserEntry(menu, "progress_set_background", Component.translatable("drippyloadingscreen.early_loading.context.progress.set_background"), options.earlyLoadingBarBackgroundTexturePath);
        addImageChooserEntry(menu, "progress_set_progress", Component.translatable("drippyloadingscreen.early_loading.context.progress.set_progress"), options.earlyLoadingBarProgressTexturePath);

        menu.addSeparatorEntry("separator_after_image");

        ContextMenu sizeMenu = new ContextMenu();
        menu.addSubMenuEntry("progress_size", Component.translatable("drippyloadingscreen.early_loading.context.progress.size"), sizeMenu);
        addIntegerInputEntry(sizeMenu, "progress_width", Component.translatable("drippyloadingscreen.early_loading.context.common.width"), options.earlyLoadingBarWidth);
        addIntegerInputEntry(sizeMenu, "progress_height", Component.translatable("drippyloadingscreen.early_loading.context.common.height"), options.earlyLoadingBarHeight);

        ContextMenu offsetMenu = new ContextMenu();
        menu.addSubMenuEntry("progress_offset", Component.translatable("drippyloadingscreen.early_loading.context.progress.offset"), offsetMenu);
        addIntegerInputEntry(offsetMenu, "progress_offset_x", Component.translatable("drippyloadingscreen.early_loading.context.common.offset_x"), options.earlyLoadingBarPositionOffsetX);
        addIntegerInputEntry(offsetMenu, "progress_offset_y", Component.translatable("drippyloadingscreen.early_loading.context.common.offset_y"), options.earlyLoadingBarPositionOffsetY);

        menu.addSeparatorEntry("separator_after_offset");

        menu.addValueCycleEntry("progress_hide",
                        CommonCycles.cycleEnabledDisabled("drippyloadingscreen.early_loading.context.common.hide_element", options.earlyLoadingHideBar.getValue())
                                .addCycleListener(value -> applyOptionChange(() -> options.earlyLoadingHideBar.setValue(value.getAsBoolean()))));

        return menu;

    }

    private ContextMenu createWatermarkContextMenu(WatermarkAnchor anchor) {

        ContextMenu menu = new ContextMenu().setForceUIScale(true);
        Options options = DrippyLoadingScreen.getOptions();
        WatermarkOptionAccess access = resolveWatermarkOptions(options, anchor);
        String prefix = anchor.name().toLowerCase(Locale.ROOT);

        addImageChooserEntry(menu, prefix + "_set_image", Component.translatable("drippyloadingscreen.early_loading.context.watermark.set_image"), access.texturePath());

        menu.addSeparatorEntry("separator_after_image");

        ContextMenu sizeMenu = new ContextMenu();
        menu.addSubMenuEntry(prefix + "_size", Component.translatable("drippyloadingscreen.early_loading.context.watermark.size"), sizeMenu);
        addIntegerInputEntry(sizeMenu, prefix + "_width", Component.translatable("drippyloadingscreen.early_loading.context.common.width"), access.width());
        addIntegerInputEntry(sizeMenu, prefix + "_height", Component.translatable("drippyloadingscreen.early_loading.context.common.height"), access.height());

        ContextMenu offsetMenu = new ContextMenu();
        menu.addSubMenuEntry(prefix + "_offset", Component.translatable("drippyloadingscreen.early_loading.context.watermark.offset"), offsetMenu);
        addIntegerInputEntry(offsetMenu, prefix + "_offset_x", Component.translatable("drippyloadingscreen.early_loading.context.common.offset_x"), access.offsetX());
        addIntegerInputEntry(offsetMenu, prefix + "_offset_y", Component.translatable("drippyloadingscreen.early_loading.context.common.offset_y"), access.offsetY());

        return menu;

    }

    private void addIntegerInputEntry(ContextMenu menu, String entryIdentifier, Component label, Options.Option<Integer> option) {
        NonStackableOverlayUI.addIntegerInputContextMenuEntryTo(menu, entryIdentifier, label,
                        option::getValue,
                        value -> applyOptionChange(() -> option.setValue(value != null ? value : option.getDefaultValue())),
                        true, option.getDefaultValue(), null, null)
                .setStackable(false);
    }

    private void addStringInputEntry(ContextMenu menu, String entryIdentifier, Component label, Options.Option<String> option) {
        NonStackableOverlayUI.addInputContextMenuEntryTo(menu, entryIdentifier, label,
                        option::getValue,
                        value -> applyOptionChange(() -> option.setValue((value != null) ? value : option.getDefaultValue())),
                        true, option.getDefaultValue(), null, false, false, null, null)
                .setStackable(false);
    }

    private void addImageChooserEntry(ContextMenu menu, String entryIdentifier, Component label, Options.Option<String> option) {
        final ResourceChooserScreen<ITexture, ImageFileType> chooser = ResourceChooserScreen.image(PNG_APNG_FILE_FILTER, s -> {});
        ResourceSupplier<ITexture> defaultSupplier = createRawImageSupplier(option.getDefaultValue());
        NonStackableOverlayUI.addGenericResourceChooserContextMenuEntryTo(menu, entryIdentifier,
                        () -> chooser,
                        ResourceSupplier::image,
                        defaultSupplier,
                        () -> createRawImageSupplier(option.getValue()),
                        supplier -> applyOptionChange(() -> option.setValue(normalizeResourceSource(supplier.getSourceWithoutPrefix()))),
                        label,
                        true,
                        PNG_APNG_FILE_TYPES,
                        PNG_APNG_FILE_FILTER,
                        false,
                        true,
                        true)
                .setStackable(false);
    }

    private static ResourceSupplier<ITexture> createRawImageSupplier(@Nullable String source) {
        if (source == null || source.isBlank()) {
            return ResourceSupplier.empty(ITexture.class, FileMediaType.IMAGE);
        }
        try {
            return ResourceSupplier.image(source);
        } catch (Exception ex) {
            LOGGER.warn("[DRIPPY LOADING SCREEN] Failed to build image supplier for {}", source, ex);
            return ResourceSupplier.empty(ITexture.class, FileMediaType.IMAGE);
        }
    }

    private void applyOptionChange(Runnable task) {
        task.run();
        syncVisualOptionsFromConfig();
    }

    private void syncVisualOptionsFromConfig() {
        Options options = DrippyLoadingScreen.getOptions();
        EarlyLoadingVisualOptions latest = EarlyLoadingVisualOptions.from(options);
        if (!latest.equals(this.visualOptions)) {
            this.visualOptions = latest;
            this.textureSuppliers = new TextureSuppliers(latest);
            this.baseWidth = resolveReferenceWidth();
            this.baseHeight = resolveReferenceHeight();
        }
    }

    private WatermarkOptionAccess resolveWatermarkOptions(Options options, WatermarkAnchor anchor) {
        return switch (anchor) {
            case TOP_LEFT -> new WatermarkOptionAccess(
                    options.earlyLoadingTopLeftWatermarkTexturePath,
                    options.earlyLoadingTopLeftWatermarkTextureWidth,
                    options.earlyLoadingTopLeftWatermarkTextureHeight,
                    options.earlyLoadingTopLeftWatermarkTexturePositionOffsetX,
                    options.earlyLoadingTopLeftWatermarkTexturePositionOffsetY
            );
            case TOP_RIGHT -> new WatermarkOptionAccess(
                    options.earlyLoadingTopRightWatermarkTexturePath,
                    options.earlyLoadingTopRightWatermarkTextureWidth,
                    options.earlyLoadingTopRightWatermarkTextureHeight,
                    options.earlyLoadingTopRightWatermarkTexturePositionOffsetX,
                    options.earlyLoadingTopRightWatermarkTexturePositionOffsetY
            );
            case BOTTOM_LEFT -> new WatermarkOptionAccess(
                    options.earlyLoadingBottomLeftWatermarkTexturePath,
                    options.earlyLoadingBottomLeftWatermarkTextureWidth,
                    options.earlyLoadingBottomLeftWatermarkTextureHeight,
                    options.earlyLoadingBottomLeftWatermarkTexturePositionOffsetX,
                    options.earlyLoadingBottomLeftWatermarkTexturePositionOffsetY
            );
            case BOTTOM_RIGHT -> new WatermarkOptionAccess(
                    options.earlyLoadingBottomRightWatermarkTexturePath,
                    options.earlyLoadingBottomRightWatermarkTextureWidth,
                    options.earlyLoadingBottomRightWatermarkTextureHeight,
                    options.earlyLoadingBottomRightWatermarkTexturePositionOffsetX,
                    options.earlyLoadingBottomRightWatermarkTexturePositionOffsetY
            );
        };
    }

    private static String normalizeResourceSource(@Nullable String sourceWithPrefix) {
        if (sourceWithPrefix == null) {
            return "";
        }
        String trimmed = sourceWithPrefix.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
//        ResourceSourceType type = ResourceSourceType.getSourceTypeOf(trimmed);
//        if (type == ResourceSourceType.LOCAL) {
//            return ResourceSourceType.getWithoutSourcePrefix(trimmed);
//        }
        return trimmed;
    }

    private boolean openContextMenuAt(double mouseX, double mouseY) {
        if (this.progressBarBounds != null && this.progressBarBounds.contains(mouseX, mouseY)) {
            setSelectedElement(SelectableElement.PROGRESS_BAR);
            return openContextMenu(this.progressBarContextMenu);
        }
        if (this.logoBounds != null && this.logoBounds.contains(mouseX, mouseY)) {
            setSelectedElement(SelectableElement.LOGO);
            return openContextMenu(this.logoContextMenu);
        }
        for (WatermarkAnchor anchor : WatermarkAnchor.values()) {
            ElementBounds bounds = this.watermarkBounds.get(anchor);
            if (bounds != null && bounds.contains(mouseX, mouseY)) {
                setSelectedElement(SelectableElement.fromAnchor(anchor));
                return openContextMenu(this.watermarkContextMenus.get(anchor));
            }
        }
        if (this.backgroundBounds == null) {
            this.backgroundBounds = new ElementBounds(0.0f, 0.0f, this.width, this.height);
        }
        if (this.backgroundBounds.contains(mouseX, mouseY)) {
            setSelectedElement(null);
            return openContextMenu(this.backgroundContextMenu);
        }
        return false;
    }

    private boolean openContextMenu(@Nullable ContextMenu menu) {
        if (menu == null) {
            return false;
        }
        closeAllContextMenus();
        menu.openMenuAtMouse();
        return true;
    }

    private void closeAllContextMenus() {
        for (ContextMenu menu : this.contextMenus) {
            menu.closeMenu();
        }
    }

    private boolean isAnyContextMenuHovered() {
        for (ContextMenu menu : this.contextMenus) {
            if (menu.isOpen() && (menu.isHovered() || menu.isUserNavigatingInMenu())) {
                return true;
            }
        }
        return false;
    }

    private LogoTexture resolveLogoTexture() {
        TextureInfo custom = fetchTexture(this.textureSuppliers.logo());
        if (custom.isValid()) {
            return new LogoTexture(custom, false);
        }
        TextureInfo bundled = fetchTexture(MOJANG_LOGO_SUPPLIER);
        boolean hasBundled = bundled.isValid();
        return new LogoTexture(bundled, hasBundled);
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
        StoredReferenceSize referenceSize = getCachedReferenceSize();
        if (referenceSize != null && referenceSize.width() > 0) {
            return referenceSize.width();
        }
        int configured = FMLConfig.getIntConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_WIDTH);
        return configured > 0 ? configured : DEFAULT_REFERENCE_WIDTH;
    }

    private float resolveReferenceHeight() {
        if (this.visualOptions.windowHeightOverride() > 0) {
            return this.visualOptions.windowHeightOverride();
        }
        StoredReferenceSize referenceSize = getCachedReferenceSize();
        if (referenceSize != null && referenceSize.height() > 0) {
            return referenceSize.height();
        }
        int configured = FMLConfig.getIntConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_HEIGHT);
        return configured > 0 ? configured : DEFAULT_REFERENCE_HEIGHT;
    }

    @Nullable
    private StoredReferenceSize getCachedReferenceSize() {
        if (!this.referenceSizeLoaded) {
            this.referenceSizeLoaded = true;
            this.cachedReferenceSize = loadReferenceSizeFromDisk();
        }
        return this.cachedReferenceSize;
    }

    @Nullable
    private StoredReferenceSize loadReferenceSizeFromDisk() {
        Path configDir = DrippyLoadingScreen.MOD_DIR.toPath();
        Path file = configDir.resolve(REFERENCE_SIZE_FILE);
        if (!Files.isRegularFile(file)) {
            return null;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        } catch (IOException ex) {
            LOGGER.debug("[DRIPPY LOADING SCREEN] Failed to read early-window reference size from {}", file, ex);
            return null;
        }
        int width = parseInt(props.getProperty("width"));
        int height = parseInt(props.getProperty("height"));
        long timestamp = parseLong(props.getProperty("timestamp"));
        if (width <= 0 || height <= 0) {
            return null;
        }
        return new StoredReferenceSize(width, height, timestamp);
    }

    private static int parseInt(@Nullable String raw) {
        if (raw == null) {
            return -1;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static long parseLong(@Nullable String raw) {
        if (raw == null) {
            return 0L;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return 0L;
        }
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

    private enum SelectableElement {
        LOGO(null),
        PROGRESS_BAR(null),
        WATERMARK_TOP_LEFT(WatermarkAnchor.TOP_LEFT),
        WATERMARK_TOP_RIGHT(WatermarkAnchor.TOP_RIGHT),
        WATERMARK_BOTTOM_LEFT(WatermarkAnchor.BOTTOM_LEFT),
        WATERMARK_BOTTOM_RIGHT(WatermarkAnchor.BOTTOM_RIGHT);

        @Nullable
        private final WatermarkAnchor watermarkAnchor;

        SelectableElement(@Nullable WatermarkAnchor watermarkAnchor) {
            this.watermarkAnchor = watermarkAnchor;
        }

        @Nullable
        private WatermarkAnchor watermarkAnchor() {
            return this.watermarkAnchor;
        }

        private static SelectableElement fromAnchor(WatermarkAnchor anchor) {
            return switch (anchor) {
                case TOP_LEFT -> WATERMARK_TOP_LEFT;
                case TOP_RIGHT -> WATERMARK_TOP_RIGHT;
                case BOTTOM_LEFT -> WATERMARK_BOTTOM_LEFT;
                case BOTTOM_RIGHT -> WATERMARK_BOTTOM_RIGHT;
            };
        }
    }

    private record RenderMetrics(float absoluteWidth, float absoluteHeight, float guiScaleFactor) {
        private float toGui(float absolute) {
            return absolute * this.guiScaleFactor;
        }
    }

    private record ElementBounds(float x, float y, float width, float height) {
        private boolean contains(double px, double py) {
            return px >= this.x && px <= this.x + this.width && py >= this.y && py <= this.y + this.height;
        }
    }

    private record ElementGeometry(ElementBounds bounds, float absoluteX, float absoluteY, float width, float height) {}

    private record ResizeSession(SelectableElement element, ResizeHandle handle, float initialLeft, float initialTop, float initialRight, float initialBottom) {}

    private record MoveSession(SelectableElement element, float grabOffsetX, float grabOffsetY, float width, float height) {}

    private record PendingMove(SelectableElement element, float initialMouseX, float initialMouseY, float grabOffsetX, float grabOffsetY,
                               float width, float height) {}

    private record ProgressFrameMetrics(float borderThickness, float horizontalInset, float verticalInset) {}

    private record TextureInfo(@Nullable ResourceLocation location, int width, int height) {
        private static final TextureInfo EMPTY = new TextureInfo(null, 0, 0);

        private boolean isValid() {
            return this.location != null && this.width > 0 && this.height > 0;
        }
    }

    private record LogoTexture(TextureInfo texture, boolean useBundledLayout) {}

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

    private record StoredReferenceSize(int width, int height, long timestampMillis) {}

    private record WatermarkOptionAccess(
            Options.Option<String> texturePath,
            Options.Option<Integer> width,
            Options.Option<Integer> height,
            Options.Option<Integer> offsetX,
            Options.Option<Integer> offsetY
    ) {}

    private enum ResizeHandle {
        TOP_LEFT(true, true, false, false),
        TOP_RIGHT(false, true, true, false),
        BOTTOM_LEFT(true, false, false, true),
        BOTTOM_RIGHT(false, false, true, true),
        TOP(false, true, false, false),
        RIGHT(false, false, true, false),
        BOTTOM(false, false, false, true),
        LEFT(true, false, false, false);

        private final boolean adjustsLeft;
        private final boolean adjustsTop;
        private final boolean adjustsRight;
        private final boolean adjustsBottom;

        ResizeHandle(boolean adjustsLeft, boolean adjustsTop, boolean adjustsRight, boolean adjustsBottom) {
            this.adjustsLeft = adjustsLeft;
            this.adjustsTop = adjustsTop;
            this.adjustsRight = adjustsRight;
            this.adjustsBottom = adjustsBottom;
        }

        private boolean adjustsLeft() {
            return this.adjustsLeft;
        }

        private boolean adjustsTop() {
            return this.adjustsTop;
        }

        private boolean adjustsRight() {
            return this.adjustsRight;
        }

        private boolean adjustsBottom() {
            return this.adjustsBottom;
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
