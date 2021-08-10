package de.keksuccino.drippyloadingscreen.customization.helper;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public class CustomizationHelperScreen extends Screen {

	protected static SplashCustomizationLayer splashLayer = new SplashCustomizationLayer(false);
	
	public static boolean renderBackgroundOverlay = false;
	
	public CustomizationHelperScreen() {
		super(new StringTextComponent(""));
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
	
//	@Override
//	public void renderBackground(MatrixStack matrixStack, int vOffset) {
//
//		if (this.minecraft.world != null) {
//			if (renderBackgroundOverlay) {
//				this.fillGradient(matrixStack, 0, 0, this.width, this.height, -1072689136, -804253680);
//			}
//			net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this, matrixStack));
//		} else {
//			this.renderDirtBackground(vOffset);
//		}
//
//	}

}
