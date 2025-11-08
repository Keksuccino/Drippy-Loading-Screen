package de.keksuccino.drippyloadingscreen.earlywindow;

import de.keksuccino.drippyloadingscreen.earlywindow.config.EarlyLoadingOptions;
import de.keksuccino.drippyloadingscreen.earlywindow.config.EarlyLoadingOptionsLoader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.progress.ProgressMeter;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import net.neoforged.neoforgespi.earlywindow.ImmediateWindowProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class DrippyEarlyWindowProvider implements ImmediateWindowProvider {

    public static final String PROVIDER_NAME = "drippy_early_window";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Runnable EMPTY_TICK = () -> {};
    private static final String MOJANG_LOGO_PATH = "assets/minecraft/textures/gui/title/mojangstudios.png";
    private static final float INDETERMINATE_SEGMENT_WIDTH = 0.3f;

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
    private ColourScheme colourScheme = ColourScheme.red();
    private String effectiveWindowTitle = "Minecraft";

    private LoadedTexture backgroundTexture;
    private LoadedTexture logoTexture;
    private LoadedTexture barBackgroundTexture;
    private LoadedTexture barProgressTexture;

    private float displayedProgress;
    private boolean progressIndeterminate;
    private float indeterminateOffset;
    private long lastProgressSampleNanos;

    @Override
    public String name() {
        return PROVIDER_NAME;
    }

    @Override
    public Runnable initialize(String[] arguments) {
        this.gameDirectory = FMLPaths.GAMEDIR.get();
        Path configDir = FMLPaths.CONFIGDIR.get();
        this.options = new EarlyLoadingOptionsLoader(configDir).load();
        this.effectiveWindowTitle = this.options.windowTitle();
        this.colourScheme = resolveColourScheme();

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

        this.windowWidth = Math.max(1, FMLConfig.getIntConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_WIDTH));
        this.windowHeight = Math.max(1, FMLConfig.getIntConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_HEIGHT));
        this.baseWindowWidth = this.windowWidth;
        this.baseWindowHeight = this.windowHeight;

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
            @SuppressWarnings("unchecked")
            Constructor<?> ctor = overlayClass.getConstructor(minecraftClass, reloadClass, Consumer.class, boolean.class);
            this.overlayConstructor = ctor;
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

        GL11.glViewport(0, 0, this.framebufferWidth, this.framebufferHeight);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glClearColor(this.colourScheme.background().r(), this.colourScheme.background().g(), this.colourScheme.background().b(), 1.0f);
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

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderBackgroundLayer() {
        if (this.backgroundTexture == null) {
            drawSolidQuad(0.0f, 0.0f, this.windowWidth, this.windowHeight, this.colourScheme.background(), 1.0f);
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
            drawSolidQuad(baseX, baseY, width, height, this.colourScheme.background().withBrightness(0.5f), 0.9f);
            drawRectangleOutline(baseX, baseY, width, height, this.colourScheme.foreground(), 1.0f);
        }

        if (this.progressIndeterminate) {
            drawIndeterminateProgress(baseX, baseY, width, height);
        } else {
            drawProgressSegment(baseX, baseY, width, height, 0.0f, Math.max(0.0f, Math.min(1.0f, this.displayedProgress)));
        }
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
            drawSolidQuad(segmentX, baseY, segmentWidth, height, this.colourScheme.foreground(), 1.0f);
        }
    }

    private void drawSolidQuad(float x, float y, float width, float height, Colour colour, float alpha) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(colour.r(), colour.g(), colour.b(), alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawRectangleOutline(float x, float y, float width, float height, Colour colour, float alpha) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(colour.r(), colour.g(), colour.b(), alpha);
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
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.id());
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
        this.backgroundTexture = loadUserTexture(this.options.backgroundTexturePath());
        this.logoTexture = loadUserTexture(this.options.logoTexturePath());
        if (this.logoTexture == null) {
            this.logoTexture = loadMojangLogoTexture();
        }
        this.barBackgroundTexture = loadUserTexture(this.options.barBackgroundTexturePath());
        this.barProgressTexture = loadUserTexture(this.options.barProgressTexturePath());
    }

    private void cleanupTextures() {
        deleteTexture(this.backgroundTexture);
        deleteTexture(this.logoTexture);
        deleteTexture(this.barBackgroundTexture);
        deleteTexture(this.barProgressTexture);
    }

    private void deleteTexture(LoadedTexture texture) {
        if (texture != null) {
            GL11.glDeleteTextures(texture.id());
        }
    }

    private LoadedTexture loadUserTexture(String configuredPath) {
        Path resolved = resolveTexturePath(configuredPath);
        if (resolved == null) {
            return null;
        }
        return uploadTextureFromPath(resolved);
    }

    private Path resolveTexturePath(String configuredPath) {
        if (configuredPath == null || configuredPath.isBlank() || this.gameDirectory == null) {
            return null;
        }
        String trimmed = configuredPath.trim();
        try {
            Path candidate;
            if (trimmed.startsWith("/") || trimmed.startsWith("\\")) {
                candidate = this.gameDirectory.resolve(trimmed.substring(1));
            } else {
                Path raw = Paths.get(trimmed);
                candidate = raw.isAbsolute() ? raw : this.gameDirectory.resolve(raw);
            }
            if (!Files.isReadable(candidate)) {
                LOGGER.debug("[DRIPPY LOADING SCREEN] Configured early loading texture {} not found", candidate);
                return null;
            }
            return candidate.normalize();
        } catch (InvalidPathException ex) {
            LOGGER.warn("[DRIPPY LOADING SCREEN] Invalid early loading texture path '{}'", configuredPath, ex);
            return null;
        }
    }

    private LoadedTexture uploadTextureFromPath(Path path) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var width = stack.mallocInt(1);
            var height = stack.mallocInt(1);
            var channels = stack.mallocInt(1);
            ByteBuffer image = STBImage.stbi_load(path.toString(), width, height, channels, 4);
            if (image == null) {
                LOGGER.warn("[DRIPPY LOADING SCREEN] Failed to decode texture {}: {}", path, STBImage.stbi_failure_reason());
                return null;
            }
            return uploadTexture(image, width.get(0), height.get(0));
        }
    }

    private LoadedTexture uploadTexture(ByteBuffer image, int width, int height) {
        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        STBImage.stbi_image_free(image);
        return new LoadedTexture(textureId, width, height);
    }

    private LoadedTexture loadMojangLogoTexture() {
        try (InputStream stream = DrippyEarlyWindowProvider.class.getClassLoader().getResourceAsStream(MOJANG_LOGO_PATH)) {
            if (stream == null) {
                LOGGER.warn("[DRIPPY LOADING SCREEN] Unable to locate Mojang logo at {}", MOJANG_LOGO_PATH);
                return null;
            }
            byte[] data = stream.readAllBytes();
            ByteBuffer buffer = MemoryUtil.memAlloc(data.length);
            buffer.put(data).flip();
            LoadedTexture texture = decodeTexture(buffer, MOJANG_LOGO_PATH);
            MemoryUtil.memFree(buffer);
            return texture;
        } catch (IOException e) {
            LOGGER.warn("[DRIPPY LOADING SCREEN] Failed to load Mojang logo resource {}", MOJANG_LOGO_PATH, e);
            return null;
        }
    }

    private LoadedTexture decodeTexture(ByteBuffer buffer, String label) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var width = stack.mallocInt(1);
            var height = stack.mallocInt(1);
            var channels = stack.mallocInt(1);
            ByteBuffer image = STBImage.stbi_load_from_memory(buffer, width, height, channels, 4);
            if (image == null) {
                LOGGER.warn("[DRIPPY LOADING SCREEN] Failed to decode texture {}: {}", label, STBImage.stbi_failure_reason());
                return null;
            }
            return uploadTexture(image, width.get(0), height.get(0));
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

    private ColourScheme resolveColourScheme() {
        if (System.getenv("FML_EARLY_WINDOW_DARK") != null) {
            return ColourScheme.dark();
        }
        if (this.gameDirectory == null) {
            return ColourScheme.red();
        }
        Path optionsFile = this.gameDirectory.resolve("options.txt");
        if (!Files.isRegularFile(optionsFile)) {
            return ColourScheme.red();
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
                return Boolean.parseBoolean(value) ? ColourScheme.dark() : ColourScheme.red();
            }
        } catch (IOException ignored) {
            // fall through to default red scheme
        }
        return ColourScheme.red();
    }

    private record LoadedTexture(int id, int width, int height) {}

    private record Colour(float r, float g, float b) {
        private Colour withBrightness(float factor) {
            return new Colour(Math.min(1.0f, r * factor), Math.min(1.0f, g * factor), Math.min(1.0f, b * factor));
        }
    }

    private record ColourScheme(Colour background, Colour foreground) {
        private static ColourScheme red() {
            return new ColourScheme(new Colour(239.0f / 255.0f, 50.0f / 255.0f, 61.0f / 255.0f), new Colour(1.0f, 1.0f, 1.0f));
        }

        private static ColourScheme dark() {
            return new ColourScheme(new Colour(0.0f, 0.0f, 0.0f), new Colour(1.0f, 1.0f, 1.0f));
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
