package de.keksuccino.drippyloadingscreen.customization.helper;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class CustomizationHelperScreen extends Screen {

	protected static SplashCustomizationLayer splashLayer = new SplashCustomizationLayer(false);
	
	public static boolean renderBackgroundOverlay = false;
	
	public CustomizationHelperScreen() {
		super(new TextComponent(""));
	}

	@Override
	protected void init() {

		//TODO übernehmen
		CustomizationHelperUI.currentHelperScreen = this;
		splashLayer.updateCustomizations();
		CustomizationHelperUI.updateUI();
		//-----------------
		
	}

	//TODO übernehmen
	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {

		splashLayer.renderLayer();
		
		CustomizationHelperUI.render(matrix, this);
		
		super.render(matrix, mouseX, mouseY, partialTicks);
		
	}

	//TODO übernehmen
	public static void resetScale() {
		if (splashLayer.scaled) {

			Minecraft mc = Minecraft.getInstance();
			Window w = mc.getWindow();
			int mcScale = w.calculateScale(mc.options.guiScale, mc.isEnforceUnicode());

			w.setGuiScale((double)mcScale);

			splashLayer.scaled = false;

		}
	}

}
