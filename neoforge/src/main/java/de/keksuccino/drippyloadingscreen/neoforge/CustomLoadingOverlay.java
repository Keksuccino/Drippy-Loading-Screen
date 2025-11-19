package de.keksuccino.drippyloadingscreen.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import java.util.Optional;
import java.util.function.Consumer;

public class CustomLoadingOverlay extends LoadingOverlay {

    public CustomLoadingOverlay(Minecraft minecraft, ReloadInstance reload, Consumer<Optional<Throwable>> onFinish, boolean fadeIn) {
        super(minecraft, reload, onFinish, fadeIn);
    }

}
