package de.keksuccino.drippyloadingscreen.customization.items.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements.ForgeMemoryInfoSplashElement;
import de.keksuccino.konkrete.properties.PropertiesSection;

public class ForgeMemoryInfoSplashCustomizationItem extends VanillaSplashCustomizationItemBase {

	protected boolean isDefaultPos = false;

	public ForgeMemoryInfoSplashCustomizationItem(ForgeMemoryInfoSplashElement element, PropertiesSection props, boolean isSecondItemOfThisType) {
		super(element, "Forge Memory Info", props, isSecondItemOfThisType);

		if (this.isOriginalOrientation) {
			this.orientation = "top-left";
		}

		String textColorString = props.getEntryValue("textcolor");
		if (textColorString != null) {
			element.customTextColorHex = textColorString;
		}
		
	}
	
	@Override
	public void render(PoseStack matrix) {

		ForgeMemoryInfoSplashElement he = (ForgeMemoryInfoSplashElement) this.element;

		if (this.isOriginalOrientation) {
			this.isDefaultPos = true;
		}
		
		if (!this.orientation.equals("top-left")) {
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
			this.posY = 10;
			this.isOriginalPosY = true;
		}
		if ((this.posY != 10) || !this.isOriginalOrientation) {
			this.isOriginalPosY = false;
		}

		if (this.posX != 10) {
			this.isDefaultPos = false;
		}
		
		super.render(matrix);
	}

}
