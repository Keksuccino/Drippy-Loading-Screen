package de.keksuccino.drippyloadingscreen.earlywindow.texture;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.ellerton.japng.Png;
import net.ellerton.japng.argb8888.Argb8888Bitmap;
import net.ellerton.japng.argb8888.Argb8888BitmapSequence;
import net.ellerton.japng.chunks.PngAnimationControl;
import net.ellerton.japng.chunks.PngFrameControl;
import net.ellerton.japng.error.PngException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

/**
 * Handles decoding and uploading textures (including APNGs) for the early window.
 */
public final class EarlyWindowTextureLoader {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final long DEFAULT_FRAME_DURATION_NANOS = 100_000_000L;
    private static final int APNG_DISPOSE_NONE = 0;
    private static final int APNG_DISPOSE_BACKGROUND = 1;
    private static final int APNG_DISPOSE_PREVIOUS = 2;
    private static final int APNG_BLEND_OVER = 1;

    private final Path gameDirectory;
    private final ClassLoader resourceLoader;

    public EarlyWindowTextureLoader(Path gameDirectory, ClassLoader resourceLoader) {
        this.gameDirectory = gameDirectory;
        this.resourceLoader = resourceLoader != null ? resourceLoader : EarlyWindowTextureLoader.class.getClassLoader();
    }

    public LoadedTexture loadUserTexture(String configuredPath, boolean supportApng) {
        Path resolved = resolveTexturePath(configuredPath);
        if (resolved == null) {
            return null;
        }
        if (supportApng) {
            LoadedTexture animated = loadApngTexture(resolved);
            if (animated != null) {
                return animated;
            }
        }
        return uploadTextureFromPath(resolved);
    }

    public LoadedTexture loadBundledTexture(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return null;
        }
        try (InputStream stream = this.resourceLoader.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                LOGGER.warn("[DRIPPY LOADING SCREEN] Unable to locate bundled texture at {}", resourcePath);
                return null;
            }
            byte[] data = stream.readAllBytes();
            ByteBuffer buffer = MemoryUtil.memAlloc(data.length);
            buffer.put(data).flip();
            try {
                return decodeTexture(buffer, resourcePath);
            } finally {
                MemoryUtil.memFree(buffer);
            }
        } catch (IOException e) {
            LOGGER.warn("[DRIPPY LOADING SCREEN] Failed to load bundled texture resource {}", resourcePath, e);
            return null;
        }
    }

    private LoadedTexture loadApngTexture(Path path) {
        try (InputStream stream = new BufferedInputStream(Files.newInputStream(path))) {
            Argb8888BitmapSequence sequence = Png.readArgb8888BitmapSequence(stream);
            if (sequence == null || !sequence.isAnimated()) {
                return null;
            }
            return uploadApngSequence(sequence);
        } catch (IOException | RuntimeException | PngException ex) {
            LOGGER.warn("[DRIPPY LOADING SCREEN] Failed to decode APNG texture {}: {}", path, ex.getMessage());
            LOGGER.debug("[DRIPPY LOADING SCREEN] Detailed APNG decode failure for {}", path, ex);
            return null;
        }
    }

    private LoadedTexture uploadApngSequence(Argb8888BitmapSequence sequence) {
        int width = Math.max(1, sequence.header.width);
        int height = Math.max(1, sequence.header.height);
        List<TextureFrame> frames = new ArrayList<>();
        int[] working = new int[width * height];
        if (sequence.hasDefaultImage() && sequence.defaultImage != null) {
            int[] defaultPixels = sequence.defaultImage.getPixelArray();
            System.arraycopy(defaultPixels, 0, working, 0, Math.min(defaultPixels.length, working.length));
            frames.add(createFrameFromPixels(working, width, height, DEFAULT_FRAME_DURATION_NANOS));
        }
        List<Argb8888BitmapSequence.Frame> animationFrames = sequence.getAnimationFrames();
        if (animationFrames == null || animationFrames.isEmpty()) {
            if (frames.isEmpty()) {
                return null;
            }
            TextureFrame single = frames.get(0);
            return LoadedTexture.singleFrame(single.textureId(), width, height);
        }
        for (Argb8888BitmapSequence.Frame frame : animationFrames) {
            PngFrameControl control = frame.control;
            if (control == null || frame.bitmap == null) {
                continue;
            }
            int[] snapshot = control.disposeOp == APNG_DISPOSE_PREVIOUS ? Arrays.copyOf(working, working.length) : null;
            blendFrameOnto(working, frame.bitmap, control, width, height);
            long duration = toFrameDurationNanos(control.getDelayMilliseconds());
            frames.add(createFrameFromPixels(working, width, height, duration));
            applyDisposal(working, snapshot, control, width, height);
        }
        if (frames.isEmpty()) {
            return null;
        }
        if (frames.size() == 1) {
            TextureFrame single = frames.get(0);
            return LoadedTexture.singleFrame(single.textureId(), width, height);
        }
        int loopCount = extractLoopCount(sequence.getAnimationControl());
        return LoadedTexture.animated(frames, width, height, loopCount);
    }

    private void blendFrameOnto(int[] canvas, Argb8888Bitmap bitmap, PngFrameControl control, int canvasWidth, int canvasHeight) {
        int destX = clampInt(control.xOffset, 0, Math.max(0, canvasWidth - 1));
        int destY = clampInt(control.yOffset, 0, Math.max(0, canvasHeight - 1));
        int availableWidth = canvasWidth - destX;
        int availableHeight = canvasHeight - destY;
        int copyWidth = Math.min(Math.min(bitmap.getWidth(), control.width), Math.max(0, availableWidth));
        int copyHeight = Math.min(Math.min(bitmap.getHeight(), control.height), Math.max(0, availableHeight));
        if (copyWidth <= 0 || copyHeight <= 0) {
            return;
        }
        int[] srcPixels = bitmap.getPixelArray();
        int srcStride = bitmap.getWidth();
        boolean blendOver = control.blendOp == APNG_BLEND_OVER;
        for (int row = 0; row < copyHeight; row++) {
            int destIndex = (destY + row) * canvasWidth + destX;
            int srcIndex = row * srcStride;
            for (int col = 0; col < copyWidth; col++) {
                int srcPixel = srcPixels[srcIndex + col];
                if (!blendOver) {
                    canvas[destIndex + col] = srcPixel;
                } else {
                    canvas[destIndex + col] = blendOver(canvas[destIndex + col], srcPixel);
                }
            }
        }
    }

    private void applyDisposal(int[] canvas, int[] previous, PngFrameControl control, int canvasWidth, int canvasHeight) {
        if (control.disposeOp == APNG_DISPOSE_NONE) {
            return;
        }
        int destX = clampInt(control.xOffset, 0, Math.max(0, canvasWidth - 1));
        int destY = clampInt(control.yOffset, 0, Math.max(0, canvasHeight - 1));
        int regionWidth = Math.min(control.width, Math.max(0, canvasWidth - destX));
        int regionHeight = Math.min(control.height, Math.max(0, canvasHeight - destY));
        if (regionWidth <= 0 || regionHeight <= 0) {
            return;
        }
        if (control.disposeOp == APNG_DISPOSE_BACKGROUND) {
            for (int row = 0; row < regionHeight; row++) {
                int index = (destY + row) * canvasWidth + destX;
                Arrays.fill(canvas, index, index + regionWidth, 0);
            }
        } else if (control.disposeOp == APNG_DISPOSE_PREVIOUS && previous != null) {
            for (int row = 0; row < regionHeight; row++) {
                int index = (destY + row) * canvasWidth + destX;
                System.arraycopy(previous, index, canvas, index, regionWidth);
            }
        }
    }

    private long toFrameDurationNanos(int delayMs) {
        long clamped = Math.max(0, delayMs);
        if (clamped == 0) {
            return DEFAULT_FRAME_DURATION_NANOS;
        }
        return clamped * 1_000_000L;
    }

    private TextureFrame createFrameFromPixels(int[] pixels, int width, int height, long durationNanos) {
        int[] snapshot = Arrays.copyOf(pixels, pixels.length);
        ByteBuffer buffer = toRgbaBuffer(snapshot);
        try {
            int textureId = createGlTexture(buffer, width, height);
            long sanitizedDuration = durationNanos <= 0 ? DEFAULT_FRAME_DURATION_NANOS : durationNanos;
            return new TextureFrame(textureId, sanitizedDuration);
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }

    private ByteBuffer toRgbaBuffer(int[] argbPixels) {
        ByteBuffer buffer = MemoryUtil.memAlloc(argbPixels.length * 4);
        for (int pixel : argbPixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) ((pixel >>> 24) & 0xFF));
        }
        buffer.flip();
        return buffer;
    }

    private int extractLoopCount(PngAnimationControl control) {
        if (control == null) {
            return -1;
        }
        return control.loopForever() ? -1 : Math.max(1, control.numPlays);
    }

    private int blendOver(int dstPixel, int srcPixel) {
        int srcAlpha = (srcPixel >>> 24) & 0xFF;
        if (srcAlpha == 0) {
            return dstPixel;
        }
        int dstAlpha = (dstPixel >>> 24) & 0xFF;
        float srcA = srcAlpha / 255.0f;
        float dstA = dstAlpha / 255.0f;
        float outA = srcA + dstA * (1.0f - srcA);
        if (outA <= 0.0f) {
            return 0;
        }
        int srcR = (srcPixel >>> 16) & 0xFF;
        int srcG = (srcPixel >>> 8) & 0xFF;
        int srcB = srcPixel & 0xFF;
        int dstR = (dstPixel >>> 16) & 0xFF;
        int dstG = (dstPixel >>> 8) & 0xFF;
        int dstB = dstPixel & 0xFF;
        int outR = clampChannel((int) Math.round((srcR * srcA + dstR * dstA * (1.0f - srcA)) / outA));
        int outG = clampChannel((int) Math.round((srcG * srcA + dstG * dstA * (1.0f - srcA)) / outA));
        int outB = clampChannel((int) Math.round((srcB * srcA + dstB * dstA * (1.0f - srcA)) / outA));
        int outAlpha = clampChannel((int) Math.round(outA * 255.0f));
        return (outAlpha << 24) | (outR << 16) | (outG << 8) | outB;
    }

    private int clampChannel(int value) {
        return Math.max(0, Math.min(255, value));
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
            try {
                int textureId = createGlTexture(image, width.get(0), height.get(0));
                return LoadedTexture.singleFrame(textureId, width.get(0), height.get(0));
            } finally {
                STBImage.stbi_image_free(image);
            }
        }
    }

    private int createGlTexture(ByteBuffer image, int width, int height) {
        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        return textureId;
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
            try {
                int textureId = createGlTexture(image, width.get(0), height.get(0));
                return LoadedTexture.singleFrame(textureId, width.get(0), height.get(0));
            } finally {
                STBImage.stbi_image_free(image);
            }
        }
    }

    private int clampInt(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(value, max));
    }

    public static final class LoadedTexture {
        private final TextureFrame[] frames;
        private final int width;
        private final int height;
        private final boolean loopsForever;
        private final int loopLimit;
        private final boolean animated;
        private int currentFrameIndex;
        private long frameStartedAtNanos;
        private int completedLoops;
        private boolean animationFinished;

        private LoadedTexture(TextureFrame[] frames, int width, int height, int loopLimit) {
            this.frames = frames;
            this.width = width;
            this.height = height;
            this.loopLimit = Math.max(0, loopLimit);
            this.loopsForever = loopLimit <= 0;
            this.animated = frames.length > 1;
        }

        private static LoadedTexture singleFrame(int textureId, int width, int height) {
            return new LoadedTexture(new TextureFrame[]{new TextureFrame(textureId, 0L)}, width, height, 0);
        }

        private static LoadedTexture animated(List<TextureFrame> frameList, int width, int height, int loopLimit) {
            TextureFrame[] array = frameList.toArray(TextureFrame[]::new);
            return new LoadedTexture(array, width, height, loopLimit);
        }

        public int width() {
            return this.width;
        }

        public int height() {
            return this.height;
        }

        public int currentTextureId(long nowNanos) {
            if (this.frames.length == 0) {
                return 0;
            }
            if (!this.animated || this.animationFinished) {
                if (this.frameStartedAtNanos == 0L) {
                    this.frameStartedAtNanos = nowNanos;
                }
                return this.frames[this.currentFrameIndex].textureId();
            }
            if (this.frameStartedAtNanos == 0L) {
                this.frameStartedAtNanos = nowNanos;
                return this.frames[this.currentFrameIndex].textureId();
            }
            long frameDuration = frameDuration(this.frames[this.currentFrameIndex]);
            while ((nowNanos - this.frameStartedAtNanos) >= frameDuration && !this.animationFinished) {
                this.frameStartedAtNanos += frameDuration;
                advanceFrame();
                frameDuration = frameDuration(this.frames[this.currentFrameIndex]);
            }
            return this.frames[this.currentFrameIndex].textureId();
        }

        private long frameDuration(TextureFrame frame) {
            long duration = frame.durationNanos();
            return duration <= 0 ? DEFAULT_FRAME_DURATION_NANOS : duration;
        }

        private void advanceFrame() {
            if (this.frames.length <= 1) {
                return;
            }
            this.currentFrameIndex++;
            if (this.currentFrameIndex >= this.frames.length) {
                if (this.loopsForever) {
                    this.currentFrameIndex = 0;
                } else {
                    this.completedLoops++;
                    if (this.completedLoops >= this.loopLimit) {
                        this.animationFinished = true;
                        this.currentFrameIndex = this.frames.length - 1;
                    } else {
                        this.currentFrameIndex = 0;
                    }
                }
            }
        }

        public void delete() {
            for (TextureFrame frame : this.frames) {
                GL11.glDeleteTextures(frame.textureId());
            }
        }
    }

    private record TextureFrame(int textureId, long durationNanos) {}
}
