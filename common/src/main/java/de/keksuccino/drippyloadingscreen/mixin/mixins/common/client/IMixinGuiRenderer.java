package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiRenderer.class)
public interface IMixinGuiRenderer {

    @Accessor("guiProjectionMatrixBuffer") CachedOrthoProjectionMatrixBuffer get_guiProjectionMatrixBuffer_Drippy();

}
