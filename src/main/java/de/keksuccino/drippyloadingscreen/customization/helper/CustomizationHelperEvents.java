package de.keksuccino.drippyloadingscreen.customization.helper;

import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.UIBase;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import de.keksuccino.drippyloadingscreen.events.CustomizationSystemReloadedEvent;
import de.keksuccino.konkrete.events.SubscribeEvent;
import de.keksuccino.konkrete.events.client.GuiScreenEvent;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.Identifier;

public class CustomizationHelperEvents {
	
	private static final Identifier OPEN_HELPER_BUTTON_TEXTURE_IDLE = new Identifier("drippyloadingscreen", "/helper/cus_button_normal.png");
	private static final Identifier OPEN_HELPER_BUTTON_TEXTURE_HOVER = new Identifier("drippyloadingscreen", "/helper/cus_button_hover.png");
	
	protected AdvancedButton openHelperButton;
	
	@SubscribeEvent
	public void onInitScreenPost(GuiScreenEvent.InitGuiEvent.Post e) {
		
		if (e.getGui() instanceof TitleScreen) {

			if (DrippyLoadingScreen.config.getOrDefault("showcustomizationcontrols", true)) {

				int btnwidth = (int) (88 * UIBase.getUIScale());
				int btnheight = (int) (70 * UIBase.getUIScale());

				this.openHelperButton = new AdvancedButton(0, 90 , btnwidth, btnheight, "", false, (press) -> {
					MinecraftClient.getInstance().setScreen(new CustomizationHelperScreen());
				});
				this.openHelperButton.setBackgroundTexture(OPEN_HELPER_BUTTON_TEXTURE_IDLE, OPEN_HELPER_BUTTON_TEXTURE_HOVER);
				this.openHelperButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.openhelper"), "%n%"));

				e.addWidget(openHelperButton);

			}

		}
		
	}

	@SubscribeEvent
	public void onReloadSystem(CustomizationSystemReloadedEvent e) {
		if (SplashCustomizationLayer.isCustomizationHelperScreen()) {
			MinecraftClient.getInstance().setScreen(new CustomizationHelperScreen());
		}
	}

}
