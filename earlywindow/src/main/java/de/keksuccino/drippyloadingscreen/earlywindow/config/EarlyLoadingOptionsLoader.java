package de.keksuccino.drippyloadingscreen.earlywindow.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Minimal parser for {@code config/drippyloadingscreen/options.txt}. Only the early-loading keys matter here, so the
 * parser intentionally ignores everything else and keeps the implementation lightweight (no dependency on Drippy code).
 */
public final class EarlyLoadingOptionsLoader {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String SECTION_PREFIX = "##[";
    private static final String[] PLACEHOLDER_TEXTURE_VALUES = {
            "/config/fancymenu/assets/some_image.png",
            "/config/fancymenu/assets/some_bar_background_image.png",
            "/config/fancymenu/assets/some_bar_progress_image.png"
    };

    private final Path optionsFile;

    public EarlyLoadingOptionsLoader(Path configDirectory) {
        this.optionsFile = configDirectory.resolve("drippyloadingscreen").resolve("options.txt");
    }

    public EarlyLoadingOptions load() {
        EarlyLoadingOptions defaults = EarlyLoadingOptions.defaults();
        if (!Files.isRegularFile(this.optionsFile)) {
            LOGGER.debug("Early loading options file {} not found, using defaults", this.optionsFile);
            return defaults;
        }

        Map<String, String> values = new HashMap<>();
        try {
            for (String rawLine : Files.readAllLines(this.optionsFile, StandardCharsets.UTF_8)) {
                parseLine(rawLine, values);
            }
        } catch (IOException ioException) {
            LOGGER.warn("Failed to read Drippy options from {}", this.optionsFile, ioException);
            return defaults;
        }

        return new EarlyLoadingOptions(
                sanitizeTextureValue(values.get("early_loading_background_texture_path")),
                parseBoolean(values.get("early_loading_background_preserve_aspect_ratio"), defaults.backgroundPreserveAspectRatio()),
                sanitizeTextureValue(values.get("early_loading_logo_texture_path")),
                parseInt(values.get("early_loading_logo_width"), defaults.logoWidth()),
                parseInt(values.get("early_loading_logo_height"), defaults.logoHeight()),
                parseInt(values.get("early_loading_logo_position_offset_x"), defaults.logoOffsetX()),
                parseInt(values.get("early_loading_logo_position_offset_y"), defaults.logoOffsetY()),
                sanitizeTitle(values.get("early_loading_window_title"), defaults.windowTitle()),
                sanitizeTextureValue(values.get("early_loading_bar_background_texture_path")),
                sanitizeTextureValue(values.get("early_loading_bar_progress_texture_path")),
                parseInt(values.get("early_loading_bar_width"), defaults.barWidth()),
                parseInt(values.get("early_loading_bar_height"), defaults.barHeight()),
                parseInt(values.get("early_loading_bar_position_offset_x"), defaults.barOffsetX()),
                parseInt(values.get("early_loading_bar_position_offset_y"), defaults.barOffsetY())
        );
    }

    private static void parseLine(String rawLine, Map<String, String> values) {
        String line = rawLine.trim();
        if (line.isEmpty() || line.startsWith("#") || line.startsWith(SECTION_PREFIX)) {
            return;
        }
        int typeSeparator = line.indexOf(':');
        int assignmentIndex = line.indexOf('=');
        if (typeSeparator <= 0 || assignmentIndex <= typeSeparator) {
            return;
        }
        String key = line.substring(typeSeparator + 1, assignmentIndex).trim();
        if (key.isEmpty()) {
            return;
        }
        String valuePortion = line.substring(assignmentIndex + 1).trim();
        if (valuePortion.endsWith(";")) {
            valuePortion = valuePortion.substring(0, valuePortion.length() - 1).trim();
        }
        if (valuePortion.length() >= 2 && valuePortion.startsWith("'") && valuePortion.endsWith("'")) {
            valuePortion = valuePortion.substring(1, valuePortion.length() - 1);
        }
        values.put(key, valuePortion);
    }

    private static int parseInt(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static boolean parseBoolean(String raw, boolean fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(normalized) || "false".equals(normalized)) {
            return Boolean.parseBoolean(normalized);
        }
        return fallback;
    }

    private static String sanitizeTextureValue(String raw) {
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

    private static String sanitizeTitle(String raw, String fallback) {
        if (raw == null) {
            return fallback;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }
}
