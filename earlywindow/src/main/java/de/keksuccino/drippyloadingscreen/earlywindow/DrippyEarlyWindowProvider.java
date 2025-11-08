package de.keksuccino.drippyloadingscreen.earlywindow;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.neoforged.fml.loading.FMLConfig;
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

    public static final String PROVIDER_NAME = "drippyearlywindow";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String DEFAULT_TEXT = "This is a custom window";
    private static final float BG_RED = 0.69f;
    private static final float BG_GREEN = 0.87f;
    private static final float BG_BLUE = 0.94f;

    private final ByteBuffer textBuffer = BufferUtils.createByteBuffer(64 * 1024);
    private final Runnable emptyTick = () -> {};

    private long window;
    private boolean running;
    private int windowWidth;
    private int windowHeight;
    private int framebufferWidth;
    private int framebufferHeight;
    private int windowX;
    private int windowY;
    private final String displayText = DEFAULT_TEXT;
    private Runnable ticker = emptyTick;
    private String glVersion = "3.2";
    private Constructor<?> overlayConstructor;
    private Thread renderThread;
    private GLCapabilities renderCapabilities;

    @Override
    public String name() {
        return PROVIDER_NAME;
    }

    @Override
    public Runnable initialize(String[] arguments) {
        setupWindow();
        startRenderThread();
        this.ticker = this::pollEvents;
        return this::periodicTick;
    }

    private void setupWindow() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW for Drippy early window");
        }

        this.windowWidth = Math.max(1, FMLConfig.getIntConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_WIDTH));
        this.windowHeight = Math.max(1, FMLConfig.getIntConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_HEIGHT));

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_COMPAT_PROFILE);

        this.window = GLFW.glfwCreateWindow(this.windowWidth, this.windowHeight, "Drippy Loading Screen", 0L, 0L);
        if (this.window == 0L) {
            throw new IllegalStateException("Failed to create Drippy early window");
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

        while (this.running && !GLFW.glfwWindowShouldClose(this.window)) {
            GLFW.glfwMakeContextCurrent(this.window);
            GL.setCapabilities(this.renderCapabilities);
            drawFrame();
            GLFW.glfwSwapBuffers(this.window);
        }

        GL.setCapabilities(null);
        GLFW.glfwMakeContextCurrent(0L);
    }

    @Override
    public void updateFramebufferSize(IntConsumer width, IntConsumer height) {
        width.accept(this.framebufferWidth);
        height.accept(this.framebufferHeight);
    }

    @Override
    public long setupMinecraftWindow(IntSupplier width, IntSupplier height, Supplier<String> title, LongSupplier monitor) {
        this.running = false;
        this.ticker = emptyTick;
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
            throw new IllegalStateException("Custom loading overlay is not available yet");
        }
        return () -> {
            try {
                Object overlay = overlayConstructor.newInstance(mc.get(), ri.get(), ex, fade);
                @SuppressWarnings("unchecked")
                T castOverlay = (T) overlay;
                return castOverlay;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to create Drippy loading overlay", e);
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
            throw new IllegalStateException("Custom loading overlay class missing", e);
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
        GL11.glViewport(0, 0, this.framebufferWidth, this.framebufferHeight);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glClearColor(BG_RED, BG_GREEN, BG_BLUE, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0, this.windowWidth, this.windowHeight, 0.0, -1.0, 1.0);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        renderText();
    }

    private void renderText() {
        this.textBuffer.clear();
        this.textBuffer.limit(this.textBuffer.capacity());
        float textWidth = STBEasyFont.stb_easy_font_width(this.displayText);
        float x = Math.max(16.0f, (this.windowWidth - textWidth) / 2.0f);
        float y = this.windowHeight / 2.0f;
        int quads = STBEasyFont.stb_easy_font_print(x, y, this.displayText, null, this.textBuffer);
        this.textBuffer.flip();

        GL11.glColor3f(0.05f, 0.05f, 0.05f);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glVertexPointer(2, GL11.GL_FLOAT, 16, this.textBuffer);
        GL11.glDrawArrays(GL11.GL_QUADS, 0, quads * 4);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }

}
