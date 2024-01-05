package de.keksuccino.drippyloadingscreen.mixin.mixins.client;

import de.keksuccino.drippyloadingscreen.DrippyForgeLoadingOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraftforge.client.loading.ForgeLoadingOverlay;
import net.minecraftforge.fml.earlydisplay.DisplayWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(ForgeLoadingOverlay.class)
public class MixinForgeLoadingOverlay {

    @Unique private static final Logger LOGGER = LogManager.getLogger();

    @Inject(method = "newInstance", at = @At("RETURN"), remap = false, cancellable = true)
    private static void onNewInstanceFancyMenu(Supplier<Minecraft> mc, Supplier<ReloadInstance> ri, Consumer<Optional<Throwable>> handler, DisplayWindow window, CallbackInfoReturnable<Supplier<LoadingOverlay>> info) {
        LOGGER.info("[DRIPPY LOADING SCREEN] Constructing vanillafied ForgeLoadingOverlay instance..");
        info.setReturnValue(()->new DrippyForgeLoadingOverlay(mc.get(), ri.get(), handler, window));
    }

}
