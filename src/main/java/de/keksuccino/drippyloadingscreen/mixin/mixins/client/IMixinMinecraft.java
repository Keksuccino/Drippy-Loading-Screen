package de.keksuccino.drippyloadingscreen.mixin.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.fonts.FontResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface IMixinMinecraft {

    @Accessor("fontManager") FontResourceManager getFontManagerDrippy();

}
