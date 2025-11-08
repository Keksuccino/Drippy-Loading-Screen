package de.keksuccino.drippyloadingscreen.earlywindow.config;

/**
 * Immutable view of the subset of Drippy options that are relevant for the early loading window.
 */
public record EarlyLoadingOptions(
        String backgroundTexturePath,
        boolean backgroundPreserveAspectRatio,
        String logoTexturePath,
        int logoWidth,
        int logoHeight,
        int logoOffsetX,
        int logoOffsetY,
        String windowTitle,
        String barBackgroundTexturePath,
        String barProgressTexturePath,
        int barWidth,
        int barHeight,
        int barOffsetX,
        int barOffsetY
) {

    public static EarlyLoadingOptions defaults() {
        return new EarlyLoadingOptions(
                null,
                true,
                null,
                500,
                100,
                0,
                0,
                "Minecraft",
                null,
                null,
                400,
                40,
                0,
                0
        );
    }
}
