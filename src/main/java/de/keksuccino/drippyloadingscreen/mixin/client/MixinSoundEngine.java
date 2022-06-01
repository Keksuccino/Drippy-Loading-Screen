package de.keksuccino.drippyloadingscreen.mixin.client;

import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.items.v2.audio.ACIHandler;
import net.minecraft.client.sounds.SoundEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public abstract class MixinSoundEngine {

    @Shadow public abstract void destroy();

    @Shadow protected abstract void loadLibrary();

    private static final Logger MIXIN_LOGGER = LogManager.getLogger("drippyloadingscreen/MixinSoundEngine");

    @Inject(at = @At("HEAD"), method = "reload", cancellable = true)
    private void onReload(CallbackInfo info) {
        if (DrippyLoadingScreen.config != null) {
            if (DrippyLoadingScreen.config.getOrDefault("custom_sound_engine_reloading", false)) {
                if (!ACIHandler.allowSoundEngineReload) {
                    MIXIN_LOGGER.info("Sound engine reload blocked to play sounds in loading screen!");
                    info.cancel();
                } else {
                    MIXIN_LOGGER.info("Reloading sound engine..");
                }
                if (ACIHandler.earlySoungEngineReload) {
                    MIXIN_LOGGER.info("Early sound engine reload! Skipping MC sound registration and Forge event hooks!");
                    info.cancel();
                    this.destroy();
                    this.loadLibrary();
                    ACIHandler.earlySoungEngineReload = false;
                }
            } else {
                ACIHandler.earlySoungEngineReload = false;
            }
        } else {
            MIXIN_LOGGER.error("Error! Drippy config was null!");
        }
    }

}
