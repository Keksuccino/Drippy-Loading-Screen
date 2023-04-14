package de.keksuccino.drippyloadingscreen.mixin.mixins.client;

import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.fancymenu.menu.animation.AnimationHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnimationHandler.class)
public class MixinAnimationHandler {

    //TODO übernehmen
//    @Inject(at = @At("HEAD"), method = "isReady", cancellable = true, remap = false)
//    private static void onIsReady(CallbackInfoReturnable<Boolean> info) {
//        if ((Minecraft.getInstance().screen != null) && (Minecraft.getInstance().screen instanceof DrippyOverlayScreen)) {
//            info.setReturnValue(true);
//        }
//    }

}
