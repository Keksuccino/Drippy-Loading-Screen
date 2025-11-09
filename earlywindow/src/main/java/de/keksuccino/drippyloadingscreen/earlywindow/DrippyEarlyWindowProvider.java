package de.keksuccino.drippyloadingscreen.earlywindow;

import de.keksuccino.drippyloadingscreen.earlywindow.config.EarlyLoadingOptions;
import de.keksuccino.drippyloadingscreen.earlywindow.config.EarlyLoadingOptionsLoader;
import de.keksuccino.drippyloadingscreen.earlywindow.texture.EarlyWindowTextureLoader;
import de.keksuccino.drippyloadingscreen.earlywindow.texture.EarlyWindowTextureLoader.LoadedTexture;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.progress.Message;
import net.neoforged.fml.loading.progress.ProgressMeter;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import net.neoforged.neoforgespi.earlywindow.ImmediateWindowProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class DrippyEarlyWindowProvider implements ImmediateWindowProvider {

    public static final String PROVIDER_NAME = "drippy_early_window";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Runnable EMPTY_TICK = () -> {};
    private static final String MOJANG_LOGO_PATH = "assets/minecraft/textures/gui/title/mojangstudios.png";
    private static final float INDETERMINATE_SEGMENT_WIDTH = 0.3f;
    private static final boolean LOGGER_DEBUG_MODE = true;
    private static final float LOGGER_LINE_HEIGHT = 12.0f;
    private static final float LOGGER_MARGIN = 10.0f;
    private static final int LOGGER_MAX_VISIBLE_LINES = 6;
    private static final int LOGGER_MAX_MESSAGE_LENGTH = 256;
    private static final int LOGGER_VERTEX_BUFFER_CAPACITY = LOGGER_MAX_MESSAGE_LENGTH * 300;
    private static final long LOGGER_DEBUG_MESSAGE_INTERVAL_NANOS = 2_000_000_000L;
    private static final String[] LOGGER_DEBUG_MESSAGE_POOL = {
            "Initializing Drippy Early Window renderer...",
            "Connecting to NeoForge loading pipeline",
            "Registering FancyMenu compatibility hooks",
            "Preparing APNG decoder",
            "Waiting for Minecraft bootstrap",
            "Completing early window handoff"
    };

    private long window;
    private boolean running;
    private int windowWidth;
    private int windowHeight;
    private int baseWindowWidth = 1;
    private int baseWindowHeight = 1;
    private int framebufferWidth;
    private int framebufferHeight;
    private int windowX;
    private int windowY;
    private Runnable ticker = EMPTY_TICK;
    private String glVersion = "3.2";
    private Constructor<?> overlayConstructor;
    private Thread renderThread;
    private GLCapabilities renderCapabilities;

    private Path gameDirectory;
    private EarlyLoadingOptions options = EarlyLoadingOptions.defaults();
    private ColorScheme colorScheme = ColorScheme.red();
    private EarlyWindowTextureLoader textureLoader;
    private String effectiveWindowTitle = "Minecraft";

    private LoadedTexture backgroundTexture;
    private LoadedTexture logoTexture;
    private LoadedTexture barBackgroundTexture;
    private LoadedTexture barProgressTexture;
    private LoadedTexture topLeftWatermarkTexture;
    private LoadedTexture topRightWatermarkTexture;
    private LoadedTexture bottomLeftWatermarkTexture;
    private LoadedTexture bottomRightWatermarkTexture;
    private final ByteBuffer loggerVertexBuffer = BufferUtils.createByteBuffer(LOGGER_VERTEX_BUFFER_CAPACITY);
    private final List<DebugLoggerMessage> loggerDebugMessages = new ArrayList<>();
    private long lastDebugMessageNanos;

    private float displayedProgress;
    private boolean progressIndeterminate;
    private float indeterminateOffset;
    private long lastProgressSampleNanos;
    private long currentFrameTimestampNanos;

    @Override
    public String name() {
        return PROVIDER_NAME;
    }

    @Override
    public Runnable initialize(String[] arguments) {
        this.gameDirectory = FMLPaths.GAMEDIR.get();
        this.textureLoader = new EarlyWindowTextureLoader(this.gameDirectory, DrippyEarlyWindowProvider.class.getClassLoader());
        Path configDir = FMLPaths.CONFIGDIR.get();
        this.options = new EarlyLoadingOptionsLoader(configDir).load();
        this.effectiveWindowTitle = this.options.windowTitle();
        this.colorScheme = resolveColorScheme();

        setupWindow();
        startRenderThread();
        this.ticker = this::pollEvents;
        return this::periodicTick;
    }

    private void setupWindow() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("[DRIPPY LOADING SCREEN] Failed to initialize GLFW for Drippy early window!");
        }

        int configuredWidth = Math.max(1, FMLConfig.getIntConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_WIDTH));
        int configuredHeight = Math.max(1, FMLConfig.getIntConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_HEIGHT));
        if (this.options.windowWidthOverride() != -1) {
            configuredWidth = Math.max(1, this.options.windowWidthOverride());
        }
        if (this.options.windowHeightOverride() != -1) {
            configuredHeight = Math.max(1, this.options.windowHeightOverride());
        }
        this.windowWidth = configuredWidth;
        this.windowHeight = configuredHeight;
        this.baseWindowWidth = configuredWidth;
        this.baseWindowHeight = configuredHeight;

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_COMPAT_PROFILE);

        this.window = GLFW.glfwCreateWindow(this.windowWidth, this.windowHeight, this.effectiveWindowTitle, 0L, 0L);
        if (this.window == 0L) {
            throw new IllegalStateException("[DRIPPY LOADING SCREEN] Failed to create Drippy early window!");
        }

        centerWindow();
        GLFW.glfwMakeContextCurrent(this.window);
        updateFramebufferFromWindow();
        GLFW.glfwSetFramebufferSizeCallback(this.window, (handle, width, height) -> {
            this.framebufferWidth = Math.max(1, width);
            this.framebufferHeight = Math.max(1, height);
        });
        GLFW.glfwSetWindowSizeCallback(this.window, (handle, width, height) -> {
            this.windowWidth = Math.max(1, width);
            this.windowHeight = Math.max(1, height);
        });
        GLFW.glfwSetWindowPosCallback(this.window, (handle, x, y) -> {
            this.windowX = x;
            this.windowY = y;
        });

        GLFW.glfwShowWindow(this.window);
        GLFW.glfwMakeContextCurrent(0L);
        this.running = true;
    }

    private void centerWindow() {
        long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode videoMode = primaryMonitor != 0L ? GLFW.glfwGetVideoMode(primaryMonitor) : null;
        if (videoMode != null) {
            int x = Math.max(0, videoMode.width() - this.windowWidth) / 2;
            int y = Math.max(0, videoMode.height() - this.windowHeight) / 2;
            GLFW.glfwSetWindowPos(this.window, x, y);
            this.windowX = x;
            this.windowY = y;
        } else {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                var posX = stack.mallocInt(1);
                var posY = stack.mallocInt(1);
                GLFW.glfwGetWindowPos(this.window, posX, posY);
                this.windowX = posX.get(0);
                this.windowY = posY.get(0);
            }
        }
    }

    private void updateFramebufferFromWindow() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var fbw = stack.mallocInt(1);
            var fbh = stack.mallocInt(1);
            GLFW.glfwGetFramebufferSize(this.window, fbw, fbh);
            this.framebufferWidth = Math.max(1, fbw.get(0));
            this.framebufferHeight = Math.max(1, fbh.get(0));
        }
    }

    private void startRenderThread() {
        this.renderThread = new Thread(this::renderLoop, "Drippy-EarlyWindow");
        this.renderThread.setDaemon(true);
        this.renderThread.start();
    }

    private void renderLoop() {
        GLFW.glfwMakeContextCurrent(this.window);
        this.renderCapabilities = GL.createCapabilities();
        GL.setCapabilities(this.renderCapabilities);
        this.glVersion = Optional.ofNullable(GL11.glGetString(GL11.GL_VERSION)).orElse(this.glVersion);
        GLFW.glfwSwapInterval(1);
        loadTextures();
        this.lastProgressSampleNanos = System.nanoTime();

        try {
            while (this.running && !GLFW.glfwWindowShouldClose(this.window)) {
                GL.setCapabilities(this.renderCapabilities);
                drawFrame();
                GLFW.glfwSwapBuffers(this.window);
            }
        } finally {
            cleanupTextures();
            GL.setCapabilities(null);
            GLFW.glfwMakeContextCurrent(0L);
        }
    }

    @Override
    public void updateFramebufferSize(IntConsumer width, IntConsumer height) {
        width.accept(this.framebufferWidth);
        height.accept(this.framebufferHeight);
    }

    @Override
    public long setupMinecraftWindow(IntSupplier width, IntSupplier height, Supplier<String> title, LongSupplier monitor) {
        this.running = false;
        this.ticker = EMPTY_TICK;
        if (this.renderThread != null) {
            try {
                this.renderThread.join(2000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        GLFW.glfwMakeContextCurrent(this.window);
        GL.createCapabilities();
        GLFW.glfwSetWindowTitle(this.window, title.get());
        GLFW.glfwSwapInterval(0);
        GLFW.glfwMakeContextCurrent(0L);
        return this.window;
    }

    @Override
    public boolean positionWindow(Optional<Object> monitor, IntConsumer widthSetter, IntConsumer heightSetter, IntConsumer xSetter, IntConsumer ySetter) {
        widthSetter.accept(this.windowWidth);
        heightSetter.accept(this.windowHeight);
        xSetter.accept(this.windowX);
        ySetter.accept(this.windowY);
        return true;
    }

    @Override
    public <T> Supplier<T> loadingOverlay(Supplier<?> mc, Supplier<?> ri, Consumer<Optional<Throwable>> ex, boolean fade) {
        if (this.overlayConstructor == null) {
            throw new IllegalStateException("[DRIPPY LOADING SCREEN] Custom loading overlay is not available yet!");
        }
        return () -> {
            try {
                Object overlay = overlayConstructor.newInstance(mc.get(), ri.get(), ex, fade);
                @SuppressWarnings("unchecked")
                T castOverlay = (T) overlay;
                return castOverlay;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("[DRIPPY LOADING SCREEN] Failed to create Drippy loading overlay!", e);
            }
        };
    }

    @Override
    public void updateModuleReads(ModuleLayer layer) {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            var overlayClass = Class.forName("de.keksuccino.drippyloadingscreen.neoforge.CustomLoadingOverlay", false, loader);
            var minecraftClass = Class.forName("net.minecraft.client.Minecraft", false, loader);
            var reloadClass = Class.forName("net.minecraft.server.packs.resources.ReloadInstance", false, loader);
            this.overlayConstructor = overlayClass.getConstructor(minecraftClass, reloadClass, Consumer.class, boolean.class);
        } catch (Exception e) {
            throw new IllegalStateException("[DRIPPY LOADING SCREEN] Custom loading overlay class missing!", e);
        }
    }

    @Override
    public void periodicTick() {
        this.ticker.run();
    }

    private void pollEvents() {
        if (this.window != 0L) {
            GLFW.glfwPollEvents();
        }
    }

    @Override
    public String getGLVersion() {
        return this.glVersion;
    }

    @Override
    public void crash(String message) {
        LOGGER.error("Early window crash: {}", message);
        TinyFileDialogs.tinyfd_messageBox("Drippy Loading Screen", message, "ok", "error", true);
    }

    private void drawFrame() {
        updateProgressMetrics();
        this.currentFrameTimestampNanos = System.nanoTime();

        GL11.glViewport(0, 0, this.framebufferWidth, this.framebufferHeight);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glClearColor(this.colorScheme.background().r(), this.colorScheme.background().g(), this.colorScheme.background().b(), 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0, this.windowWidth, this.windowHeight, 0.0, -1.0, 1.0);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float uiScale = computeUiScale();
        renderBackgroundLayer();
        float logoBottom = renderLogoLayer(uiScale);
        renderProgressBar(logoBottom, uiScale);
        renderWatermarks(uiScale);
        renderLoggerOverlay(uiScale);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderBackgroundLayer() {
        if (this.backgroundTexture == null) {
            drawSolidQuad(0.0f, 0.0f, this.windowWidth, this.windowHeight, this.colorScheme.background(), 1.0f);
            return;
        }
        float drawWidth = this.windowWidth;
        float drawHeight = this.windowHeight;
        float x = 0.0f;
        float y = 0.0f;
        if (this.options.backgroundPreserveAspectRatio() && this.backgroundTexture.width() > 0 && this.backgroundTexture.height() > 0) {
            float textureRatio = (float) this.backgroundTexture.width() / this.backgroundTexture.height();
            float windowRatio = (float) this.windowWidth / this.windowHeight;
            if (windowRatio > textureRatio) {
                drawWidth = this.windowWidth;
                drawHeight = drawWidth / textureRatio;
                y = (this.windowHeight - drawHeight) / 2.0f;
            } else {
                drawHeight = this.windowHeight;
                drawWidth = drawHeight * textureRatio;
                x = (this.windowWidth - drawWidth) / 2.0f;
            }
        }
        drawTexturedQuad(x, y, drawWidth, drawHeight, this.backgroundTexture, 0.0f, 0.0f, 1.0f, 1.0f);
    }

    private float renderLogoLayer(float uiScale) {
        float scaledOffsetY = this.options.logoOffsetY() * uiScale;
        if (this.options.hideLogo()) {
            return this.windowHeight / 2.0f + scaledOffsetY;
        }
        if (this.logoTexture == null) {
            return this.windowHeight / 2.0f + scaledOffsetY;
        }
        float baseWidth = this.options.logoWidth() > 0 ? this.options.logoWidth() : this.logoTexture.width();
        float baseHeight = this.options.logoHeight() > 0 ? this.options.logoHeight() : this.logoTexture.height();
        float width = Math.max(1.0f, baseWidth * uiScale);
        float height = Math.max(1.0f, baseHeight * uiScale);
        float offsetX = this.options.logoOffsetX() * uiScale;
        float x = (this.windowWidth - width) / 2.0f + offsetX;
        float baseline = this.windowHeight * 0.35f;
        float y = baseline + scaledOffsetY;
        drawTexturedQuad(x, y, width, height, this.logoTexture, 0.0f, 0.0f, 1.0f, 1.0f);
        return y + height;
    }

    private void renderProgressBar(float logoBottom, float uiScale) {
        if (this.options.hideBar()) {
            return;
        }
        int configuredWidth = Math.max(32, this.options.barWidth());
        int configuredHeight = Math.max(6, this.options.barHeight());
        float targetWidth = configuredWidth * uiScale;
        float targetHeight = configuredHeight * uiScale;
        float minWidth = 32.0f * uiScale;
        float minHeight = 6.0f * uiScale;
        float maxWidth = Math.max(minWidth, this.windowWidth - 40.0f);
        float maxHeight = Math.max(minHeight, this.windowHeight / 6.0f);
        float width = Math.max(minWidth, Math.min(targetWidth, maxWidth));
        float height = Math.max(minHeight, Math.min(targetHeight, maxHeight));
        float offsetX = this.options.barOffsetX() * uiScale;
        float offsetY = this.options.barOffsetY() * uiScale;
        float baseX = (this.windowWidth - width) / 2.0f + offsetX;
        float spacing = 32.0f * uiScale;
        float fallbackSpacing = 20.0f * uiScale;
        float defaultY = (logoBottom > 0.0f ? logoBottom + spacing : this.windowHeight / 2.0f + fallbackSpacing);
        float minY = 10.0f * uiScale;
        float maxY = Math.max(minY, this.windowHeight - height - minY);
        float baseY = clamp(defaultY + offsetY, minY, maxY);
        float minX = 10.0f * uiScale;
        float maxX = Math.max(minX, this.windowWidth - width - minX);
        baseX = clamp(baseX, minX, maxX);

        if (this.barBackgroundTexture != null) {
            drawTexturedQuad(baseX, baseY, width, height, this.barBackgroundTexture, 0.0f, 0.0f, 1.0f, 1.0f);
        } else {
            drawSolidQuad(baseX, baseY, width, height, this.colorScheme.background().withBrightness(0.5f), 0.9f);
            drawRectangleOutline(baseX, baseY, width, height, this.colorScheme.foreground(), 1.0f);
        }

        if (this.progressIndeterminate) {
            drawIndeterminateProgress(baseX, baseY, width, height);
        } else {
            drawProgressSegment(baseX, baseY, width, height, 0.0f, Math.max(0.0f, Math.min(1.0f, this.displayedProgress)));
        }
    }

    private void renderWatermarks(float uiScale) {
        renderWatermark(this.topLeftWatermarkTexture,
                this.options.topLeftWatermarkWidth(),
                this.options.topLeftWatermarkHeight(),
                this.options.topLeftWatermarkOffsetX(),
                this.options.topLeftWatermarkOffsetY(),
                WatermarkAnchor.TOP_LEFT,
                uiScale);
        renderWatermark(this.topRightWatermarkTexture,
                this.options.topRightWatermarkWidth(),
                this.options.topRightWatermarkHeight(),
                this.options.topRightWatermarkOffsetX(),
                this.options.topRightWatermarkOffsetY(),
                WatermarkAnchor.TOP_RIGHT,
                uiScale);
        renderWatermark(this.bottomLeftWatermarkTexture,
                this.options.bottomLeftWatermarkWidth(),
                this.options.bottomLeftWatermarkHeight(),
                this.options.bottomLeftWatermarkOffsetX(),
                this.options.bottomLeftWatermarkOffsetY(),
                WatermarkAnchor.BOTTOM_LEFT,
                uiScale);
        renderWatermark(this.bottomRightWatermarkTexture,
                this.options.bottomRightWatermarkWidth(),
                this.options.bottomRightWatermarkHeight(),
                this.options.bottomRightWatermarkOffsetX(),
                this.options.bottomRightWatermarkOffsetY(),
                WatermarkAnchor.BOTTOM_RIGHT,
                uiScale);
    }

    private void renderWatermark(LoadedTexture texture, int configuredWidth, int configuredHeight, int offsetX, int offsetY, WatermarkAnchor anchor, float uiScale) {
        if (texture == null) {
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
                x = this.windowWidth - width + scaledOffsetX;
                y = scaledOffsetY;
            }
            case BOTTOM_LEFT -> {
                x = scaledOffsetX;
                y = this.windowHeight - height + scaledOffsetY;
            }
            case BOTTOM_RIGHT -> {
                x = this.windowWidth - width + scaledOffsetX;
                y = this.windowHeight - height + scaledOffsetY;
            }
            default -> {
                x = scaledOffsetX;
                y = scaledOffsetY;
            }
        }
        drawTexturedQuad(x, y, width, height, texture, 0.0f, 0.0f, 1.0f, 1.0f);
    }

    private void renderLoggerOverlay(float uiScale) {
        if (this.options.hideLogger()) {
            return;
        }
        List<StartupNotificationManager.AgeMessage> rawMessages = collectLoggerMessages();
        if (rawMessages.isEmpty()) {
            return;
        }
        List<LoggerLine> lines = new ArrayList<>();
        for (int i = rawMessages.size() - 1; i >= 0; i--) {
            var ageMessage = rawMessages.get(i);
            float fade = computeLoggerFade(ageMessage.age(), i);
            if (fade <= 0.01f) {
                continue;
            }
            String sanitized = sanitizeLogMessage(ageMessage.message().getText());
            if (sanitized.isEmpty()) {
                continue;
            }
            lines.add(new LoggerLine(sanitized, fade));
        }
        if (lines.isEmpty()) {
            return;
        }
        int visible = Math.min(lines.size(), LOGGER_MAX_VISIBLE_LINES);
        int startIndex = lines.size() - visible;
        float lineHeight = LOGGER_LINE_HEIGHT * uiScale;
        float margin = LOGGER_MARGIN * uiScale;
        float totalHeight = visible * lineHeight;
        float startY = this.windowHeight - totalHeight - margin;
        if (startY < margin) {
            startY = margin;
        }
        float x = margin;
        for (int idx = 0; idx < visible; idx++) {
            LoggerLine line = lines.get(startIndex + idx);
            float y = startY + idx * lineHeight;
            drawLoggerLine(line.text(), x, y, line.alpha());
        }
    }

    private void drawLoggerLine(String text, float x, float y, float alpha) {
        if (text == null || text.isEmpty()) {
            return;
        }
        this.loggerVertexBuffer.clear();
        int quadCount = STBEasyFont.stb_easy_font_print(x, y, text, null, this.loggerVertexBuffer);
        if (quadCount <= 0) {
            return;
        }
        this.loggerVertexBuffer.limit(this.loggerVertexBuffer.capacity());
        this.loggerVertexBuffer.position(0);
        boolean texturesEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glVertexPointer(2, GL11.GL_FLOAT, 16, this.loggerVertexBuffer);
        Color foreground = this.colorScheme.foreground();
        GL11.glColor4f(foreground.r(), foreground.g(), foreground.b(), clamp(alpha, 0.0f, 1.0f));
        GL11.glDrawArrays(GL11.GL_QUADS, 0, quadCount * 4);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        if (texturesEnabled) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private List<StartupNotificationManager.AgeMessage> collectLoggerMessages() {
        List<StartupNotificationManager.AgeMessage> messages = new ArrayList<>(StartupNotificationManager.getMessages());
        if (LOGGER_DEBUG_MODE) {
            injectDebugLoggerMessage();
            messages.addAll(buildDebugLoggerAgeMessages());
        }
        return messages;
    }

    private void injectDebugLoggerMessage() {
        long now = System.nanoTime();
        if ((now - this.lastDebugMessageNanos) < LOGGER_DEBUG_MESSAGE_INTERVAL_NANOS) {
            return;
        }
        this.lastDebugMessageNanos = now;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String message = LOGGER_DEBUG_MESSAGE_POOL[random.nextInt(LOGGER_DEBUG_MESSAGE_POOL.length)];
        this.loggerDebugMessages.add(new DebugLoggerMessage(message, now));
        if (this.loggerDebugMessages.size() > LOGGER_MAX_VISIBLE_LINES * 2) {
            this.loggerDebugMessages.remove(0);
        }
    }

    private List<StartupNotificationManager.AgeMessage> buildDebugLoggerAgeMessages() {
        long now = System.nanoTime();
        List<StartupNotificationManager.AgeMessage> debugMessages = new ArrayList<>();
        for (DebugLoggerMessage message : this.loggerDebugMessages) {
            int ageMillis = (int) ((now - message.createdAtNanos()) / 1_000_000L);
            debugMessages.add(new StartupNotificationManager.AgeMessage(ageMillis, new Message(message.text(), null)));
        }
        return debugMessages;
    }

    private void drawIndeterminateProgress(float x, float y, float width, float height) {
        float start = this.indeterminateOffset;
        float end = start + INDETERMINATE_SEGMENT_WIDTH;
        if (end <= 1.0f) {
            drawProgressSegment(x, y, width, height, start, end);
        } else {
            drawProgressSegment(x, y, width, height, start, 1.0f);
            drawProgressSegment(x, y, width, height, 0.0f, end - 1.0f);
        }
    }

    private void drawProgressSegment(float baseX, float baseY, float width, float height, float start, float end) {
        if (end <= start) {
            return;
        }
        float segmentWidth = width * (end - start);
        float segmentX = baseX + width * start;
        if (this.barProgressTexture != null) {
            drawTexturedQuad(segmentX, baseY, segmentWidth, height, this.barProgressTexture, start, 0.0f, end, 1.0f);
        } else {
            drawSolidQuad(segmentX, baseY, segmentWidth, height, this.colorScheme.foreground(), 1.0f);
        }
    }

    private void drawSolidQuad(float x, float y, float width, float height, Color color, float alpha) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(color.r(), color.g(), color.b(), alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawRectangleOutline(float x, float y, float width, float height, Color color, float alpha) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(color.r(), color.g(), color.b(), alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawTexturedQuad(float x, float y, float width, float height, LoadedTexture texture, float u0, float v0, float u1, float v1) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        int textureId = texture.currentTextureId(this.currentFrameTimestampNanos);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(u0, v0);
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(u1, v0);
        GL11.glVertex2f(x + width, y);
        GL11.glTexCoord2f(u1, v1);
        GL11.glVertex2f(x + width, y + height);
        GL11.glTexCoord2f(u0, v1);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    private void loadTextures() {
        if (this.textureLoader == null) {
            this.textureLoader = new EarlyWindowTextureLoader(this.gameDirectory, DrippyEarlyWindowProvider.class.getClassLoader());
        }
        this.backgroundTexture = this.textureLoader.loadUserTexture(this.options.backgroundTexturePath(), true);
        this.logoTexture = this.textureLoader.loadUserTexture(this.options.logoTexturePath(), true);
        if (this.logoTexture == null) {
            this.logoTexture = this.textureLoader.loadBundledTexture(MOJANG_LOGO_PATH);
        }
        this.barBackgroundTexture = this.textureLoader.loadUserTexture(this.options.barBackgroundTexturePath(), true);
        this.barProgressTexture = this.textureLoader.loadUserTexture(this.options.barProgressTexturePath(), true);
        this.topLeftWatermarkTexture = this.textureLoader.loadUserTexture(this.options.topLeftWatermarkTexturePath(), true);
        this.topRightWatermarkTexture = this.textureLoader.loadUserTexture(this.options.topRightWatermarkTexturePath(), true);
        this.bottomLeftWatermarkTexture = this.textureLoader.loadUserTexture(this.options.bottomLeftWatermarkTexturePath(), true);
        this.bottomRightWatermarkTexture = this.textureLoader.loadUserTexture(this.options.bottomRightWatermarkTexturePath(), true);
    }

    private void cleanupTextures() {
        deleteTexture(this.backgroundTexture);
        deleteTexture(this.logoTexture);
        deleteTexture(this.barBackgroundTexture);
        deleteTexture(this.barProgressTexture);
        deleteTexture(this.topLeftWatermarkTexture);
        deleteTexture(this.topRightWatermarkTexture);
        deleteTexture(this.bottomLeftWatermarkTexture);
        deleteTexture(this.bottomRightWatermarkTexture);
    }

    private void deleteTexture(LoadedTexture texture) {
        if (texture != null) {
            texture.delete();
        }
    }

    private void updateProgressMetrics() {
        long now = System.nanoTime();
        if (this.lastProgressSampleNanos == 0L) {
            this.lastProgressSampleNanos = now;
        }
        float deltaSeconds = (now - this.lastProgressSampleNanos) / 1_000_000_000f;
        this.lastProgressSampleNanos = now;

        ProgressSample sample = ProgressSample.capture();
        if (sample.indeterminate()) {
            this.progressIndeterminate = true;
            this.indeterminateOffset = (this.indeterminateOffset + deltaSeconds * 0.4f) % 1.0f;
            this.displayedProgress = 0.0f;
        } else {
            this.progressIndeterminate = false;
            float target = sample.progress();
            float lerpFactor = Math.min(1.0f, deltaSeconds * 6.0f);
            this.displayedProgress += (target - this.displayedProgress) * lerpFactor;
            this.indeterminateOffset = 0.0f;
        }
    }

    private ColorScheme resolveColorScheme() {
        if (System.getenv("FML_EARLY_WINDOW_DARK") != null) {
            return ColorScheme.dark();
        }
        if (this.gameDirectory == null) {
            return ColorScheme.red();
        }
        Path optionsFile = this.gameDirectory.resolve("options.txt");
        if (!Files.isRegularFile(optionsFile)) {
            return ColorScheme.red();
        }
        try {
            List<String> lines = Files.readAllLines(optionsFile, StandardCharsets.UTF_8);
            for (String line : lines) {
                int idx = line.indexOf(':');
                if (idx <= 0) {
                    continue;
                }
                String key = line.substring(0, idx).trim();
                if (!"darkMojangStudiosBackground".equals(key)) {
                    continue;
                }
                String value = line.substring(idx + 1).trim();
                return Boolean.parseBoolean(value) ? ColorScheme.dark() : ColorScheme.red();
            }
        } catch (IOException ignored) {
            // fall through to default red scheme
        }
        return ColorScheme.red();
    }

    private static float computeLoggerFade(int ageMillis, int reverseIndex) {
        float fade = (4000.0f - ageMillis - (reverseIndex - 4) * 1000.0f) / 5000.0f;
        return clamp(fade, 0.0f, 1.0f);
    }

    private static String sanitizeLogMessage(String raw) {
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

    private record ProgressSample(float progress, boolean indeterminate) {
        private static ProgressSample capture() {
            List<ProgressMeter> meters = StartupNotificationManager.getCurrentProgress();
            for (ProgressMeter meter : meters) {
                if (meter.steps() > 0) {
                    return new ProgressSample(Math.max(0.0f, Math.min(1.0f, meter.progress())), false);
                }
            }
            return new ProgressSample(0.0f, true);
        }
    }

    private enum WatermarkAnchor {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    private float computeUiScale() {
        float baseW = Math.max(1.0f, this.baseWindowWidth);
        float baseH = Math.max(1.0f, this.baseWindowHeight);
        float scaleX = this.windowWidth / baseW;
        float scaleY = this.windowHeight / baseH;
        float scale = Math.min(scaleX, scaleY);
        return Math.max(0.1f, scale);
    }

    private static float clamp(float value, float min, float max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(value, max));
    }

}
