package de.keksuccino.drippyloadingscreen.customization.items.vanilla;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements.ForgeTextSplashElement;
import de.keksuccino.konkrete.properties.PropertiesSection;

public class ForgeTextSplashCustomizationItem extends VanillaSplashCustomizationItemBase {

	protected boolean isDefaultPos = false;

	public ForgeTextSplashCustomizationItem(ForgeTextSplashElement element, PropertiesSection props, boolean isSecondItemOfThisType) {
		super(element, "Forge Loading Status", props, isSecondItemOfThisType);

		if (this.isOriginalOrientation) {
			this.orientation = "bottom-left";
		}

		String textColorString = props.getEntryValue("textcolor");
		if (textColorString != null) {
			element.customTextColorHex = textColorString;
		}

	}
	
	@Override
	public void render(MatrixStack matrix) {

		ForgeTextSplashElement he = (ForgeTextSplashElement) this.element;

		if (this.isOriginalOrientation) {
			this.isDefaultPos = true;
		}
		
		if (!this.orientation.equals("bottom-left")) {
			this.isOriginalOrientation = false;
		}

		if (this.posX == Integer.MAX_VALUE) {
			this.posX = 10;
			this.isOriginalPosX = true;
		}
		if ((this.posX != 10) || !this.isOriginalOrientation) {
			this.isOriginalPosX = false;
		}

		if (this.posY == Integer.MAX_VALUE) {
			this.posY = -10;
			this.isOriginalPosY = true;
		}
		if ((this.posY != -10) || !this.isOriginalOrientation) {
			this.isOriginalPosY = false;
		}

		if (this.posX != 10) {
			this.isDefaultPos = false;
		}
		
		super.render(matrix);
	}

}
