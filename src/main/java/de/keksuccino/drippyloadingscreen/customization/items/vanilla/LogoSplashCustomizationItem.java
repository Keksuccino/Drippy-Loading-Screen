package de.keksuccino.drippyloadingscreen.customization.items.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements.LogoSplashElement;
import de.keksuccino.konkrete.properties.PropertiesSection;

public class LogoSplashCustomizationItem extends VanillaSplashCustomizationItemBase {

	protected boolean isDefaultPos = false;
	
	public LogoSplashCustomizationItem(LogoSplashElement element, PropertiesSection props, boolean isSecondItemOfThisType) {
		super(element, "Logo", props, isSecondItemOfThisType);

		if (this.isOriginalOrientation) {
			this.orientation = "mid-centered";
		}
		
	}
	
	@Override
	public void render(PoseStack matrix) {

		LogoSplashElement he = (LogoSplashElement) this.element;

		if (this.isOriginalOrientation) {
			this.isDefaultPos = true;
		}
		
		if (!this.orientation.equals("mid-centered")) {
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
			this.posY = 0;
			this.isOriginalPosY = true;
		}
		if ((this.posY != 0) || !this.isOriginalOrientation) {
			this.isOriginalPosY = false;
		}

		if (this.posX != 0) {
			this.isDefaultPos = false;
		}
		
		super.render(matrix);
	}

}
