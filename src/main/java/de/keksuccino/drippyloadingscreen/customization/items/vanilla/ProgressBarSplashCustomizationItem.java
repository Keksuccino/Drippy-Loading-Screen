package de.keksuccino.drippyloadingscreen.customization.items.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements.ProgressBarSplashElement;
import de.keksuccino.konkrete.properties.PropertiesSection;

public class ProgressBarSplashCustomizationItem extends VanillaSplashCustomizationItemBase {

	protected boolean isDefaultPos = false;

	public ProgressBarSplashCustomizationItem(ProgressBarSplashElement element, PropertiesSection props, boolean isSecondItemOfThisType) {
		super(element, "Progress Bar", props, isSecondItemOfThisType);

		if (this.isOriginalOrientation) {
			this.orientation = "bottom-centered";
		}

		String barColorString = props.getEntryValue("barcolor");
		if (barColorString != null) {
			element.customBarColorHex = barColorString;
		}
		
	}
	
	@Override
	public void render(PoseStack matrix) {

		ProgressBarSplashElement he = (ProgressBarSplashElement) this.element;

		if (this.isOriginalOrientation) {
			this.isDefaultPos = true;
		}
		
		if (!this.orientation.equals("bottom-centered")) {
			this.isOriginalOrientation = false;
		}

		if (this.posX == Integer.MAX_VALUE) {
			this.posX = 0;
			this.isOriginalPosX = true;
		}
		if ((this.posX != 0) || !this.isOriginalOrientation) {
			this.isOriginalPosX = false;
		}

		if (this.posY == Integer.MAX_VALUE) {
			this.posY = -50;
			this.isOriginalPosY = true;
		}
		if ((this.posY != -50) || !this.isOriginalOrientation) {
			this.isOriginalPosY = false;
		}

		if (this.posX != 0) {
			this.isDefaultPos = false;
		}
		
		super.render(matrix);
	}

	//TODO übernehmen
	@Override
	public String getActionId() {
		return "vanilla:progressbar";
	}

}
