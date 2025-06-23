package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.mixin.MixinCache;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FontManager.class)
public class MixinFontManager {

    @Inject(method = "apply", at = @At("RETURN"))
    private void after_apply_Drippy(FontManager.Preparation $$0, ProfilerFiller $$1, CallbackInfo info) {
        MixinCache.fontsReady = true;
    }

}
