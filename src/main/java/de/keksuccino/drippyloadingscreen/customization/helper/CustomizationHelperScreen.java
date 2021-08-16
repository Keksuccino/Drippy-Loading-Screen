package de.keksuccino.drippyloadingscreen.customization.helper;

import net.minecraft.client.util.math.MatrixStack;

import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

public class CustomizationHelperScreen extends Screen {

	protected static SplashCustomizationLayer splashLayer = new SplashCustomizationLayer(false);
	
	public static boolean renderBackgroundOverlay = false;
	
	public CustomizationHelperScreen() {
		super(new LiteralText(""));
	}
	
	@Override
	protected void init() {
		
		CustomizationHelperUI.currentHelperScreen = this;
		CustomizationHelperUI.updateUI();
		splashLayer.updateCustomizations();
		
	}
	
	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {

		splashLayer.renderLayer();
		
		CustomizationHelperUI.render(matrix, this);
		
		super.render(matrix, mouseX, mouseY, partialTicks);
		
	}

}
