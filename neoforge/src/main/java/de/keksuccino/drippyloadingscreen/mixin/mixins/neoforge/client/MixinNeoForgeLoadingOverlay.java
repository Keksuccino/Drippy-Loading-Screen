package de.keksuccino.drippyloadingscreen.mixin.mixins.neoforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.neoforged.neoforge.client.loading.NeoForgeLoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(NeoForgeLoadingOverlay.class)
public class MixinNeoForgeLoadingOverlay extends LoadingOverlay {

    public MixinNeoForgeLoadingOverlay(Minecraft mc, ReloadInstance reload, Consumer<Optional<Throwable>> errorConsumer, boolean b) {
        super(mc, reload, errorConsumer, b);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cancelForgeCustomLoadingOverlayRenderingDrippy(GuiGraphics graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {

        info.cancel();

        super.render(graphics, mouseX, mouseY, partial);

    }

}
