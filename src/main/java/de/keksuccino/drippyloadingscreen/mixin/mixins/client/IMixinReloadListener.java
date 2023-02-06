package de.keksuccino.drippyloadingscreen.mixin.mixins.client;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ReloadListener.class)
public interface IMixinReloadListener<T> {

    @Invoker("prepare") T invokePrepareDrippy(IResourceManager p_212854_1_, IProfiler p_212854_2_);

    @Invoker("apply") void invokeApplyDrippy(T p_212853_1_, IResourceManager p_212853_2_, IProfiler p_212853_3_);

}
