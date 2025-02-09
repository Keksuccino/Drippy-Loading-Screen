package de.keksuccino.drippyloadingscreen.mixin.mixins.forge.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LoadingOverlay.class)
public class MixinForgeLoadingOverlay {

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/loading/ClientModLoader;renderProgressText()V", remap = false))
    private boolean cancelForgeOverlayRenderingDrippy() {
        return false;
    }

}
