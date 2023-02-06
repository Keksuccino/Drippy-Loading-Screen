package de.keksuccino.drippyloadingscreen.mixin.mixins.client;

import net.minecraft.client.gui.fonts.FontResourceManager;
import net.minecraft.resources.IFutureReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FontResourceManager.class)
public interface IMixinFontResourceManager {

    @Accessor("reloadListener") IFutureReloadListener getReloadListenerDrippy();

}
