package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import net.minecraft.client.gui.font.FontManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FontManager.class)
public interface IMixinFontManager {

    @Accessor("reloadListener") PreparableReloadListener getReloadListenerDrippy();

}