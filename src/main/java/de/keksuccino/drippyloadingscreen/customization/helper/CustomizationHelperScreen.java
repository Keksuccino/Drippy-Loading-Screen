package de.keksuccino.drippyloadingscreen.customization.helper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;

import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

public class CustomizationHelperScreen extends Screen {

	protected static SplashCustomizationLayer splashLayer = new SplashCustomizationLayer(false);
	
	public static boolean renderBackgroundOverlay = false;
	
	public CustomizationHelperScreen() {
		super(new LiteralText(""));
		splashLayer.isNewLoadingScreen = true;
	}
	
	@Override
	protected void init() {

		CustomizationHelperUI.currentHelperScreen = this;
		splashLayer.updateCustomizations();
		CustomizationHelperUI.updateUI();
		
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	@Override
	public void onClose() {
		CustomizationHelperScreen.resetScale();
		MinecraftClient.getInstance().openScreen(null);
	}

	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {

		splashLayer.renderLayer();
		
		CustomizationHelperUI.render(matrix, this);
		
		super.render(matrix, mouseX, mouseY, partialTicks);
		
	}

	public static void resetScale() {
		if (splashLayer.scaled) {

			MinecraftClient mc = MinecraftClient.getInstance();
			Window w = mc.getWindow();
			int mcScale = w.calculateScaleFactor(mc.options.guiScale, mc.forcesUnicodeFont());

			w.setScaleFactor((double)mcScale);

			splashLayer.scaled = false;

		}
	}

}
