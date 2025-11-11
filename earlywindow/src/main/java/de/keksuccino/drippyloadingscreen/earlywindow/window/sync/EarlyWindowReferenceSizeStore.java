package de.keksuccino.drippyloadingscreen.earlywindow.window.sync;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.Properties;

/**
 * Stores the base window size that the early-window provider booted with so the in-game preview can mirror it.
 */
public final class EarlyWindowReferenceSizeStore {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String FILE_NAME = "early_window_reference.properties";

    private EarlyWindowReferenceSizeStore() {}

    public static void persist(Path drippyConfigDir, int width, int height) {
        if (drippyConfigDir == null || width <= 0 || height <= 0) {
            return;
        }
        try {
            Files.createDirectories(drippyConfigDir);
            Properties props = new Properties();
            props.setProperty("width", Integer.toString(width));
            props.setProperty("height", Integer.toString(height));
            props.setProperty("timestamp", Long.toString(System.currentTimeMillis()));
            Path file = drippyConfigDir.resolve(FILE_NAME);
            try (OutputStream out = Files.newOutputStream(file,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                props.store(out, "Drippy Loading Screen early-window reference size");
            }
        } catch (IOException ex) {
            LOGGER.debug("[DRIPPY LOADING SCREEN] Failed to persist early-window reference size to {}", drippyConfigDir, ex);
        }
    }

    public static Optional<ReferenceSize> load(Path drippyConfigDir) {
        if (drippyConfigDir == null) {
            return Optional.empty();
        }
        Path file = drippyConfigDir.resolve(FILE_NAME);
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        } catch (IOException ex) {
            LOGGER.debug("[DRIPPY LOADING SCREEN] Failed to read early-window reference size from {}", file, ex);
            return Optional.empty();
        }
        int width = parseInt(props.getProperty("width"));
        int height = parseInt(props.getProperty("height"));
        long timestamp = parseLong(props.getProperty("timestamp"));
        if (width <= 0 || height <= 0) {
            return Optional.empty();
        }
        return Optional.of(new ReferenceSize(width, height, timestamp));
    }

    private static int parseInt(String raw) {
        if (raw == null) {
            return -1;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static long parseLong(String raw) {
        if (raw == null) {
            return 0L;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    public record ReferenceSize(int width, int height, long timestampMillis) {}
}
