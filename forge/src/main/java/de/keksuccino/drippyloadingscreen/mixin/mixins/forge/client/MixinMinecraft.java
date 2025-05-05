package de.keksuccino.drippyloadingscreen.mixin.mixins.forge.client;

import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void after_construct_Forge_Drippy(GameConfig gameConfig, CallbackInfo info) {

        DrippyLoadingScreen.init();

    }

}
