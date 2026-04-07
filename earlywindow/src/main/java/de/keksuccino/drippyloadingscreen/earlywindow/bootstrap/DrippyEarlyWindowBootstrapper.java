package de.keksuccino.drippyloadingscreen.earlywindow.bootstrap;

import java.util.Objects;
import java.util.UUID;

import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DrippyEarlyWindowBootstrapper implements GraphicsBootstrapper {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String PROVIDER_NAME = "drippy_early_window";

    @Override
    public String name() {
        return "drippy_early_window_bootstrap";
    }

    @Override
    public void bootstrap(String[] arguments) {

        // This is to inform Drippy that the early loading module is present
        String sessionToken = UUID.randomUUID().toString();
        System.setProperty("drippyloadingscreen.earlywindow.session", sessionToken);

        if (!FMLConfig.getBoolConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_CONTROL)) {
            return;
        }

        String currentProvider = FMLConfig.getConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_PROVIDER);
        if (PROVIDER_NAME.equals(currentProvider)) {
            return;
        }
        if (!Objects.equals(currentProvider, "fmlearlywindow")) {
            LOGGER.debug("[DRIPPY LOADING SCREEN] Leaving user-selected early window provider {} unchanged.", currentProvider);
            return;
        }

        FMLConfig.updateConfig(FMLConfig.ConfigValue.EARLY_WINDOW_PROVIDER, PROVIDER_NAME);
        LOGGER.info("[DRIPPY LOADING SCREEN] Configured NeoForge to use the {} early window provider.", PROVIDER_NAME);

    }

}
