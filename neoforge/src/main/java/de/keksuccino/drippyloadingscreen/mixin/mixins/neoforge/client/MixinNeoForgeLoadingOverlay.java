package de.keksuccino.drippyloadingscreen.mixin.mixins.neoforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.neoforged.neoforge.client.loading.NeoForgeLoadingOverlay;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(NeoForgeLoadingOverlay.class)
public class MixinNeoForgeLoadingOverlay extends LoadingOverlay {

    @Unique private static final Logger LOGGER_DRIPPY = LogManager.getLogger();

    public MixinNeoForgeLoadingOverlay(Minecraft mc, ReloadInstance reload, Consumer<Optional<Throwable>> errorConsumer, boolean b) {
        super(mc, reload, errorConsumer, b);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cancelForgeCustomLoadingOverlayRenderingDrippy(GuiGraphics graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {

        try {

            super.render(graphics, mouseX, mouseY, partial);
            info.cancel();

        } catch (Exception ex) {
            LOGGER_DRIPPY.error("[DRIPPY LOADING SCREEN] Error while trying to render custom loading screen in NeoForgeLoadingOverlay!", ex);
        }

    }

}
