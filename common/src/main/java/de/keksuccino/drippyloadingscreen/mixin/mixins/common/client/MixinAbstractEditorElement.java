package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.fancymenu.customization.element.editor.AbstractEditorElement;
import de.keksuccino.fancymenu.customization.layout.editor.LayoutEditorScreen;
import de.keksuccino.fancymenu.util.rendering.ui.contextmenu.v2.ContextMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractEditorElement.class)
public class MixinAbstractEditorElement {

    @Shadow(remap = false) public ContextMenu rightClickMenu;
    @Shadow(remap = false) public LayoutEditorScreen editor;

    /**
     * @reason Element-level auto-sizing is not supported by the loading screen.
     */
    @Inject(method = "init", at = @At("RETURN"), remap = false)
    private void after_init_Drippy(CallbackInfo info) {

        if (this.editor.layoutTargetScreen instanceof DrippyOverlayScreen) {
            // This identifier belongs to FancyMenu and must stay aligned with AbstractEditorElement's menu entry.
            this.rightClickMenu.removeEntry("auto_sizing");
        }

    }

}
