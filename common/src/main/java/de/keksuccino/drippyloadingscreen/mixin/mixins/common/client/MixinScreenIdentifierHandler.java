package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.fancymenu.customization.screen.identifier.ScreenIdentifierHandler;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenIdentifierHandler.class)
public class MixinScreenIdentifierHandler {

    @Inject(method = "isValidIdentifier", at = @At("HEAD"), remap = false)
    private static void head_isValidIdentifier_Drippy(String screenIdentifier, CallbackInfoReturnable<Boolean> info) {
        LogManager.getLogger().info("########################################### CALLING isValidIdentifier !!!!!!!!!", new Throwable());
    }

}
