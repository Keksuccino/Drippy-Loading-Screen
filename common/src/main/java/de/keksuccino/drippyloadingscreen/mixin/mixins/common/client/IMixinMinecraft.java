package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface IMixinMinecraft {

    @Accessor("fontManager") FontManager getFontManagerDrippy();

    @Accessor(value = "reloadListenerRegisteredFancyMenu", remap = false)
    static void setFancyMenuReloadListenerRegisteredDrippy(boolean value) {
        throw new UnsupportedOperationException();
    }

    @Accessor(value = "reloadListenerRegisteredFancyMenu", remap = false)
    static boolean isFancyMenuReloadListenerRegisteredDrippy() {
        throw new UnsupportedOperationException();
    }

}
