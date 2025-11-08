package de.keksuccino.drippyloadingscreen.bootstrap;

import de.keksuccino.drippyloadingscreen.earlywindow.DrippyEarlyWindowProvider;
import java.util.Objects;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DrippyEarlyWindowBootstrapper implements GraphicsBootstrapper {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String name() {
        return "drippyearlywindow_bootstrap";
    }

    @Override
    public void bootstrap(String[] arguments) {
        if (!FMLConfig.getBoolConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_CONTROL)) {
            return;
        }

        String currentProvider = FMLConfig.getConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_PROVIDER);
        if (DrippyEarlyWindowProvider.PROVIDER_NAME.equals(currentProvider)) {
            return;
        }
        if (!Objects.equals(currentProvider, "fmlearlywindow")) {
            LOGGER.debug("Leaving user-selected early window provider {} unchanged", currentProvider);
            return;
        }

        FMLConfig.updateConfig(FMLConfig.ConfigValue.EARLY_WINDOW_PROVIDER, DrippyEarlyWindowProvider.PROVIDER_NAME);
        LOGGER.info("Configured NeoForge to use the {} early window provider", DrippyEarlyWindowProvider.PROVIDER_NAME);
    }
}
