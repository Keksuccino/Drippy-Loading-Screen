package de.keksuccino.drippyloadingscreen.mixin.mixins.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraftforge.client.loading.ForgeLoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(ForgeLoadingOverlay.class)
public class MixinForgeLoadingOverlay extends LoadingOverlay {

    public MixinForgeLoadingOverlay(Minecraft mc, ReloadInstance reload, Consumer<Optional<Throwable>> errorConsumer, boolean b) {
        super(mc, reload, errorConsumer, b);
    }

    @Inject(method = "renderContents", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelForgeCustomLoadingOverlayRenderingDrippy(GuiGraphics gui, float fade, CallbackInfoReturnable<Boolean> info) {

        info.setReturnValue(true);
        info.cancel();

    }

}
