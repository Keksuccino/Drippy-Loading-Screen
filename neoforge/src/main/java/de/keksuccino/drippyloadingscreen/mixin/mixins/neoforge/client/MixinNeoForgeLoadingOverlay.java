package de.keksuccino.drippyloadingscreen.mixin.mixins.neoforge.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.neoforged.fml.earlydisplay.ColourScheme;
import net.neoforged.fml.earlydisplay.DisplayWindow;
import net.neoforged.neoforge.client.loading.NeoForgeLoadingOverlay;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(NeoForgeLoadingOverlay.class)
public class MixinNeoForgeLoadingOverlay extends LoadingOverlay {

    public MixinNeoForgeLoadingOverlay(Minecraft mc, ReloadInstance reload, Consumer<Optional<Throwable>> errorConsumer, boolean b) {
        super(mc, reload, errorConsumer, b);
    }

//    @WrapMethod(method = "render")
//    private void wrap_NeoForge_render_Drippy(GuiGraphics graphics, int mouseX, int mouseY, float partial, Operation<Void> original) {
//
//        // Render original NeoForge overlay to not break logic and mixins of other mods (but remove any actual rendering via mixins below)
//        original.call(graphics, mouseX, mouseY, partial);
//
//        // Restore render defaults
//        RenderSystem.enableBlend();
//        graphics.flush();
//        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
//
//        // Render Vanilla overlay after, so Drippy can render its stuff
//        super.render(graphics, mouseX, mouseY, partial);
//
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ReloadInstance;isDone()Z"))
//    private boolean wrap_NeoForge_reload_isDone_in_render_Drippy(ReloadInstance instance, Operation<Boolean> original) {
//        // Always return FALSE to not make the NeoForge screen finish loading, because that's handled in super.render()
//        return false;
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
//    private void wrap_NeoForge_Screen_render_in_render_Drippy(Screen instance, GuiGraphics graphics, int mouseX, int mouseY, float partial, Operation<Void> original) {
//        // Never call
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"))
//    private void wrap_NeoForge_setOverlay_in_render_Drippy(Minecraft instance, Overlay loadingGui, Operation<Void> original) {
//        // Never call
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableCull()V"))
//    private void wrap_NeoForge_disableCull_in_render_Drippy(Operation<Void> original) {
//        // Never call
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V"))
//    private void wrap_NeoForge_setShaderColor_in_render_Drippy(float f, float g, float h, float i, Operation<Void> original) {
//        // Never call
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/neoforged/fml/earlydisplay/DisplayWindow;render(I)V"))
//    private void wrap_NeoForge_DisplayWindow_render_in_render_Drippy(DisplayWindow instance, int alpha, Operation<Void> original) {
//        // Never call
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_clearColor(FFFF)V"))
//    private void wrap_NeoForge_clearColor_in_render_Drippy(float f, float g, float h, float i, Operation<Void> original) {
//        // Never call
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_clear(IZ)V"))
//    private void wrap_NeoForge_clear_in_render_Drippy(int i, boolean bl, Operation<Void> original) {
//        // Never call
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;blendFunc(II)V"))
//    private void wrap_NeoForge_blendFunc_in_render_Drippy(int i, int j, Operation<Void> original) {
//        // Never call
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL30C;glViewport(IIII)V"))
//    private void wrap_NeoForge_glViewport_in_render_Drippy(int i1, int i2, int i3, int i4, Operation<Void> original) {
//        // Never call
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;glActiveTexture(I)V"))
//    private void wrap_NeoForge_glActiveTexture_in_render_Drippy(int i, Operation<Void> original) {
//        // Never call
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;identity()Lorg/joml/Matrix4f;"))
//    private Matrix4f wrap_NeoForge_modelViewMatrix_identity_in_render_Drippy(Matrix4f instance, Operation<Matrix4f> original) {
//        // Never call
//        return null;
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexSorting;)V"))
//    private void wrap_NeoForge_setProjectionMatrix_in_render_Drippy(Matrix4f matrix4f, VertexSorting vertexSorting, Operation<Void> original) {
//        // Never call
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V"))
//    private void wrap_NeoForge_setShader_in_render_Drippy(Supplier<ShaderInstance> supplier, Operation<Void> original) {
//        // Never call
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(II)V"))
//    private void wrap_NeoForge_setShaderTexture_in_render_Drippy(int i, int j, Operation<Void> original) {
//        // Never call
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL30C;glTexParameterIi(III)V"))
//    private void wrap_NeoForge_glTexParameterIi_in_render_Drippy(int params, int target, int pname, Operation<Void> original) {
//        // Never call
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/Tesselator;begin(Lcom/mojang/blaze3d/vertex/VertexFormat$Mode;Lcom/mojang/blaze3d/vertex/VertexFormat;)Lcom/mojang/blaze3d/vertex/BufferBuilder;"))
//    private BufferBuilder wrap_NeoForge_Tesselator_begin_in_render_Drippy(Tesselator instance, VertexFormat.Mode mode, VertexFormat format, Operation<BufferBuilder> original) {
//        // Never call
//        return null;
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/loading/NeoForgeLoadingOverlay;addQuad(Lcom/mojang/blaze3d/vertex/VertexConsumer;FFFFLnet/neoforged/fml/earlydisplay/ColourScheme$Colour;F)V"))
//    private void wrap_NeoForge_addQuad_in_render_Drippy(VertexConsumer bufferbuilder, float x0, float x1, float y0, float y1, ColourScheme.Colour colour, float fade, Operation<Void> original) {
//        // Never call
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V"))
//    private void wrap_NeoForge_drawWithShader_in_render_Drippy(MeshData meshData, Operation<Void> original) {
//        // Never call
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;addVertex(FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
//    private VertexConsumer wrap_NeoForge_addVertex_in_render_Drippy(BufferBuilder instance, float x, float y, float z, Operation<VertexConsumer> original) {
//        // Never call
//        return null;
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setUv(FF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
//    private VertexConsumer wrap_NeoForge_setUv_in_render_Drippy(VertexConsumer instance, float v1, float v2, Operation<VertexConsumer> original) {
//        // Never call
//        return null;
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
//    private VertexConsumer wrap_NeoForge_setColor_in_render_Drippy(VertexConsumer instance, float red, float green, float blue, float alpha, Operation<VertexConsumer> original) {
//        // Never call
//        return null;
//    }
//
//    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;buildOrThrow()Lcom/mojang/blaze3d/vertex/MeshData;"))
//    private MeshData wrap_NeoForge_buildOrThrow_in_render_Drippy(BufferBuilder instance, Operation<MeshData> original) {
//        // Never call
//        return null;
//    }

}
