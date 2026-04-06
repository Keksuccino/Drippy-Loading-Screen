package de.keksuccino.drippyloadingscreen.mixin.mixins.neoforge.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.neoforged.fml.earlydisplay.DisplayWindow;
import net.neoforged.neoforge.client.loading.NeoForgeLoadingOverlay;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This is only used for when Drippy's early loading module isn't installed.
 */
@Mixin(NeoForgeLoadingOverlay.class)
public class MixinNeoForgeLoadingOverlay extends LoadingOverlay {

    @Unique private static final Logger LOGGER_DRIPPY = LogManager.getLogger();

    public MixinNeoForgeLoadingOverlay(Minecraft mc, ReloadInstance reload, Consumer<Optional<Throwable>> errorConsumer, boolean b) {
        super(mc, reload, errorConsumer, b);
    }

    @WrapMethod(method = "extractRenderState")
    private void wrap_NeoForge_extractRenderState_Drippy(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial, Operation<Void> original) {

        // Render original NeoForge overlay to not break logic and mixins of other mods (but remove any actual rendering via mixins below)
        original.call(graphics, mouseX, mouseY, partial);

        // Render Vanilla overlay after, so Drippy can render its stuff
        super.extractRenderState(graphics, mouseX, mouseY, partial);

    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
    private void wrap_NeoForge_Screen_extractRenderState_in_extractRenderState_Drippy(Screen instance, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial, Operation<Void> original) {
        // Never call
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"))
    private void wrap_NeoForge_setOverlay_in_extractRenderState_Drippy(Minecraft instance, Overlay loadingGui, Operation<Void> original) {
        // Never call
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/neoforged/fml/earlydisplay/DisplayWindow;renderToFramebuffer()V"))
    private void wrap_NeoForge_DisplayWindow_renderToFramebuffer_in_extractRenderState_Drippy(DisplayWindow instance, Operation<Void> original) {
        // Never call
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIIIII)V"))
    private void wrap_NeoForge_blit_in_extractRenderState_Drippy(GuiGraphicsExtractor instance, RenderPipeline pipeline, Identifier atlas, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, Operation<Void> original) {
        // Never call
    }

}
