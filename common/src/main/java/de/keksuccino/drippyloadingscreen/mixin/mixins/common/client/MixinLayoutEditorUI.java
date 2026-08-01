package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.keksuccino.drippyloadingscreen.DrippyUtils;
import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.fancymenu.customization.decorationoverlay.AbstractDecorationOverlay;
import de.keksuccino.fancymenu.customization.decorationoverlay.AbstractDecorationOverlayBuilder;
import de.keksuccino.fancymenu.customization.decorationoverlay.overlays.DecorationOverlays;
import de.keksuccino.fancymenu.customization.element.ElementBuilder;
import de.keksuccino.fancymenu.customization.element.ElementRegistry;
import de.keksuccino.fancymenu.customization.layout.editor.LayoutEditorScreen;
import de.keksuccino.fancymenu.customization.layout.editor.LayoutEditorUI;
import de.keksuccino.fancymenu.util.Pair;
import de.keksuccino.fancymenu.util.rendering.ui.contextmenu.v2.ContextMenu;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

@Mixin(LayoutEditorUI.class)
public class MixinLayoutEditorUI {

    @Shadow(remap = false)
    @Final private LayoutEditorScreen editor;

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

    /**
     * @reason This is to be able to filter which types of decoration overlays should be available in the loading screen.
     */
    @WrapOperation(method = "buildDecorationOverlaysMenu", at = @At(value = "INVOKE", target = "Ljava/util/List;sort(Ljava/util/Comparator;)V"), remap = false)
    private static void wrap_List_sort_in_buildDecorationOverlaysMenu_Drippy(List<Pair<AbstractDecorationOverlayBuilder<?>, AbstractDecorationOverlay<?>>> instance, Comparator<?> c, Operation<Void> original) {

        if (Minecraft.getInstance().screen instanceof LayoutEditorScreen editor) {
            if (!editor.layout.isUniversalLayout() && DrippyUtils.isDrippyIdentifier(editor.layout.screenIdentifier)) {

                instance.removeIf(pair -> {

                    if (pair.getFirst().getIdentifier().equals(DecorationOverlays.BROWSER.getIdentifier())) return true;

                    if (pair.getFirst().getIdentifier().equals(DecorationOverlays.BUDDY.getIdentifier())) return true;

                    return false;

                });

            }
        }

        original.call(instance, c);

    }

    /**
     * @reason Custom GUI scaling is not supported in the loading overlay.
     */
    @Inject(method = "buildRightClickContextMenu", at = @At("RETURN"), remap = false)
    private void after_buildRightClickContextMenu_Drippy(CallbackInfoReturnable<ContextMenu> info) {

        if (this.editor.layoutTargetScreen instanceof DrippyOverlayScreen) {
            info.getReturnValue().removeEntry("auto_scaling");
            info.getReturnValue().removeEntry("forced_gui_scale");
        }

    }

}
