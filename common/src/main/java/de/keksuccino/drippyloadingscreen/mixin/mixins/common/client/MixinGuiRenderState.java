package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.DrippyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderState.class)
public class MixinGuiRenderState {

    @Inject(method = "submitText", at = @At("HEAD"), cancellable = true)
    private void before_submitText_Drippy(GuiTextRenderState guiTextRenderState, CallbackInfo info) {
        if (Minecraft.getInstance().getOverlay() != null) {
            if (!DrippyUtils.fontsReady()) info.cancel();
        }
    }

}
