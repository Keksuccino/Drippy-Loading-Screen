package de.keksuccino.drippyloadingscreen.mixin.mixins.client;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SimplePreparableReloadListener.class)
public interface IMixinSimplePreparableReloadListener<T> {

    @Invoker("prepare") T invokePrepareDrippy(ResourceManager var1, ProfilerFiller var2);

    @Invoker("apply") void invokeApplyDrippy(T var1, ResourceManager var2, ProfilerFiller var3);

}
