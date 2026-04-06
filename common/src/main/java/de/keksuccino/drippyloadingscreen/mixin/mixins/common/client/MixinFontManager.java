package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.mixin.MixinCache;
import net.minecraft.client.gui.font.FontManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FontManager.class)
public class MixinFontManager {

    @Inject(method = "apply", at = @At("RETURN"))
    private void after_apply_Drippy(CallbackInfo info) {
        MixinCache.fontsReady = true;
    }

}
