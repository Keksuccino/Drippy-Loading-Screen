//TODO übernehmen
package de.keksuccino.drippyloadingscreen.mixin.client;

import de.keksuccino.drippyloadingscreen.events.OverlayOpenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(at = @At("HEAD"), method = "setOverlay")
    private void onSetOverlay(Overlay overlay, CallbackInfo info) {

        MinecraftForge.EVENT_BUS.post(new OverlayOpenEvent(overlay));

    }

}
