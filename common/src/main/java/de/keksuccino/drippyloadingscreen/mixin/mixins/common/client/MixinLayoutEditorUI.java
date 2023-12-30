package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.DrippyUtils;
import de.keksuccino.fancymenu.customization.element.ElementBuilder;
import de.keksuccino.fancymenu.customization.element.ElementRegistry;
import de.keksuccino.fancymenu.customization.layout.editor.LayoutEditorScreen;
import de.keksuccino.fancymenu.customization.layout.editor.LayoutEditorUI;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.List;

@Mixin(LayoutEditorUI.class)
public class MixinLayoutEditorUI {

    /**
     * @reason Remove unsupported element types in loading screen layouts.
     */
    @Redirect(method = "buildElementContextMenu", at = @At(value = "INVOKE", target = "Lde/keksuccino/fancymenu/customization/element/ElementRegistry;getBuilders()Ljava/util/List;"), remap = false)
    private static List<ElementBuilder<?,?>> wrapGetBuildersDrippy() {
        List<ElementBuilder<?,?>> l = ElementRegistry.getBuilders();
        if (Minecraft.getInstance().screen instanceof LayoutEditorScreen editor) {
            if (!editor.layout.isUniversalLayout() && DrippyUtils.isDrippyIdentifier(editor.layout.screenIdentifier)) {
                l.removeIf(elementBuilder -> {

                    String id = elementBuilder.getIdentifier();

                    if (id.equals("audio_v2")) return true;
                    if (id.equals("fancymenu_customization_player_entity")) return true;

                    return false;

                });
            }
        }
        return l;
    }

}
