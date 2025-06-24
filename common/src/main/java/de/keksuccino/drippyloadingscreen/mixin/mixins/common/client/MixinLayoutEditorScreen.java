package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.fancymenu.customization.layout.Layout;
import de.keksuccino.fancymenu.customization.layout.editor.LayoutEditorScreen;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayoutEditorScreen.class)
public class MixinLayoutEditorScreen {

    @Shadow @Nullable public Screen layoutTargetScreen;

    @Shadow(remap = false) @NotNull public Layout layout;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lde/keksuccino/fancymenu/customization/layout/editor/LayoutEditorScreen;serializeElementInstancesToLayoutInstance()V", shift = At.Shift.AFTER, remap = false))
    private void before_scaling_in_init_Drippy(CallbackInfo info) {

        if (this.layoutTargetScreen instanceof DrippyOverlayScreen) {
            this.layout.forcedScale = 0.0F;
            this.layout.autoScalingWidth = 0;
            this.layout.autoScalingHeight = 0;
        }

    }

}
