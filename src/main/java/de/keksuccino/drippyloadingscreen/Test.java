package de.keksuccino.drippyloadingscreen;

import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Test extends AbstractGui {

    AdvancedButton b = new AdvancedButton(20, 20,100, 20, "Open Editor DL", true, (press) -> {
        Minecraft.getInstance().displayGuiScreen(new LayoutEditorScreen());
        Minecraft.getInstance().displayGuiScreen(new LayoutEditorScreen());
    });

    @SubscribeEvent
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post e) {
        b.render(e.getMatrixStack(), e.getMouseX(), e.getMouseY(), e.getRenderPartialTicks());
    }

}
