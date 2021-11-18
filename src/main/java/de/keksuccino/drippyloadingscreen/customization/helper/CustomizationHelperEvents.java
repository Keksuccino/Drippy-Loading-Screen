package de.keksuccino.drippyloadingscreen.customization.helper;

import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.UIBase;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import de.keksuccino.drippyloadingscreen.events.CustomizationSystemReloadedEvent;
import de.keksuccino.drippyloadingscreen.events.WindowResizedEvent;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CustomizationHelperEvents {
	
	private static final ResourceLocation OPEN_HELPER_BUTTON_TEXTURE_IDLE = new ResourceLocation("drippyloadingscreen", "/helper/cus_button_normal.png");
	private static final ResourceLocation OPEN_HELPER_BUTTON_TEXTURE_HOVER = new ResourceLocation("drippyloadingscreen", "/helper/cus_button_hover.png");
	
	protected AdvancedButton openHelperButton;

	@SubscribeEvent
	public void onWindowResize(WindowResizedEvent e) {
		Screen s = Minecraft.getInstance().currentScreen;
		if ((s != null) && (s instanceof CustomizationHelperScreen)) {
			Minecraft.getInstance().displayGuiScreen(s);
		}
	}
	
	@SubscribeEvent
	public void onInitScreenPost(GuiScreenEvent.InitGuiEvent.Post e) {
		
		if (e.getGui() instanceof MainMenuScreen) {

			if (DrippyLoadingScreen.config.getOrDefault("showcustomizationcontrols", true)) {

				int btnwidth = (int) (88 * UIBase.getUIScale());
				int btnheight = (int) (70 * UIBase.getUIScale());

				this.openHelperButton = new AdvancedButton(0, 90 , btnwidth, btnheight, "", true, (press) -> {
					Minecraft.getInstance().displayGuiScreen(new CustomizationHelperScreen());
				});
				this.openHelperButton.setBackgroundTexture(OPEN_HELPER_BUTTON_TEXTURE_IDLE, OPEN_HELPER_BUTTON_TEXTURE_HOVER);
				this.openHelperButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.openhelper"), "%n%"));

			}

		}
		
	}

	@SubscribeEvent
	public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post e) {

		if (e.getGui() instanceof MainMenuScreen) {
			if (DrippyLoadingScreen.config.getOrDefault("showcustomizationcontrols", true)) {
				if (this.openHelperButton != null) {
					this.openHelperButton.render(e.getMatrixStack(), e.getMouseX(), e.getMouseY(), e.getRenderPartialTicks());
				}
			}
		}

	}

	@SubscribeEvent
	public void onReloadSystem(CustomizationSystemReloadedEvent e) {
		if (SplashCustomizationLayer.isCustomizationHelperScreen()) {
			Minecraft.getInstance().displayGuiScreen(new CustomizationHelperScreen());
		}
	}

}
