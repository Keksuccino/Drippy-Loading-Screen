package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import de.keksuccino.fancymenu.customization.overlay.CustomizationOverlay;
import de.keksuccino.fancymenu.customization.overlay.CustomizationOverlayMenuBar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CustomizationOverlay.class)
public class MixinCustomizationOverlay {

    //TODO übernehmen
    //Don't render FM's menu bar when DrippyOverlayScreen gets rendered in LoadingOverlay
    @WrapWithCondition(method = "onRenderPost", at = @At(value = "INVOKE", target = "Lde/keksuccino/fancymenu/customization/overlay/CustomizationOverlayMenuBar;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    private boolean wrapMenuBarRenderingDrippy(CustomizationOverlayMenuBar instance, GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        return !(Minecraft.getInstance().getOverlay() instanceof LoadingOverlay);
    }

}
