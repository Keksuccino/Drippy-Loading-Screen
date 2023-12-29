package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import de.keksuccino.fancymenu.customization.overlay.CustomizationOverlay;
import de.keksuccino.fancymenu.util.rendering.ui.menubar.v2.MenuBar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CustomizationOverlay.class)
public class MixinCustomizationOverlay {

    //Don't render FM's menu bar when DrippyOverlayScreen gets rendered in LoadingOverlay
    @WrapWithCondition(method = "onRenderPost", at = @At(value = "INVOKE", target = "Lde/keksuccino/fancymenu/util/rendering/ui/menubar/v2/MenuBar;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    private boolean wrapMenuBarRenderingDrippy(MenuBar instance, GuiGraphics e, int leftX, int rightX, float c) {
        return !(Minecraft.getInstance().getOverlay() instanceof LoadingOverlay);
    }

}
