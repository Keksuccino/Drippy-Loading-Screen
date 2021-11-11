package de.keksuccino.drippyloadingscreen.customization.helper;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public class CustomizationHelperScreen extends Screen {

	protected static SplashCustomizationLayer splashLayer = new SplashCustomizationLayer(false);
	
	public static boolean renderBackgroundOverlay = false;
	
	public CustomizationHelperScreen() {
		super(new StringTextComponent(""));
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
	public void closeScreen() {
		CustomizationHelperScreen.resetScale();
		Minecraft.getInstance().displayGuiScreen(null);
	}

	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {

		splashLayer.renderLayer();
		
		CustomizationHelperUI.render(matrix, this);
		
		super.render(matrix, mouseX, mouseY, partialTicks);
		
	}

	//TODO übernehmen
	public static void resetScale() {
		if (splashLayer.scaled) {

			Minecraft mc = Minecraft.getInstance();
			MainWindow w = mc.getMainWindow();
			int mcScale = w.calcGuiScale(mc.gameSettings.guiScale, mc.getForceUnicodeFont());

			w.setGuiScale((double)mcScale);

//			int screenWidth = w.getScaledWidth();
//			int screenHeight = w.getScaledHeight();
//
//			mc.displayGuiScreen(mc.currentScreen);

			splashLayer.scaled = false;

		}
	}

}
