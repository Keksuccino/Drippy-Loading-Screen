package de.keksuccino.drippyloadingscreen.mixin.mixins.client;

import net.minecraft.client.gui.font.FontManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(FontManager.class)
public interface IMixinFontManager {

//    @Invoker("prepare") CompletableFuture<FontManager.Preparation> invokePrepareDrippy(ResourceManager p_285252_, Executor p_284969_);
//
//    @Invoker("apply") void invokeApplyDrippy(FontManager.Preparation p_284939_, ProfilerFiller p_285407_);

}
