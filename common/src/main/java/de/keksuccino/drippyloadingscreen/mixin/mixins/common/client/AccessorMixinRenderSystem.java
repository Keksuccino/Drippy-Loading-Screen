package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import com.mojang.blaze3d.pipeline.PipelineCache;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSystem.class)
public interface AccessorMixinRenderSystem {

    @Accessor("currentPipelineCache")
    static @Nullable PipelineCache getCurrentPipelineCache_Drippy() {
        throw new AssertionError("Mixin accessor was not applied");
    }

}
