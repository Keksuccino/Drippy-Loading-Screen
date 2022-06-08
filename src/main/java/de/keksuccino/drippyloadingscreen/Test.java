package de.keksuccino.drippyloadingscreen;

import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Test extends GuiComponent {

    AdvancedButton b = new AdvancedButton(20, 20,100, 20, "Open Editor DL", true, (press) -> {
        Minecraft.getInstance().setScreen(new LayoutEditorScreen());
    });

    @SubscribeEvent
    public void onDrawScreenPost(ScreenEvent.DrawScreenEvent.Post e) {
        b.render(e.getPoseStack(), e.getMouseX(), e.getMouseY(), e.getPartialTick());
    }

}
