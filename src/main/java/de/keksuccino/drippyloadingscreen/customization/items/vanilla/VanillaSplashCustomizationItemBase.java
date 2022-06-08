package de.keksuccino.drippyloadingscreen.customization.items.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.customization.items.CustomizationItemBase;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements.SplashElementBase;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class VanillaSplashCustomizationItemBase extends CustomizationItemBase {

	private static final Logger LOGGER = LogManager.getLogger("drippyloadingscreen/VanillaSplashCustomizationItemBase");

	public SplashElementBase element;
	public boolean isSecondItemOfThisType;
	
	public boolean isOriginalPosX = false;
	public boolean isOriginalPosY = false;
	public boolean isOriginalOrientation = true;
	
	public float rotation = 0.0F;
	public float scale = 1.0F;
	public boolean vanillaVisible = true;
	public boolean fireEvents = true;
	
	public VanillaSplashCustomizationItemBase(@NotNull SplashElementBase element, String elementDisplayName, @NotNull PropertiesSection props, boolean isSecondItemOfThisType) {
		super(props);
		
		this.isSecondItemOfThisType = isSecondItemOfThisType;
		this.element = element;
		this.value = elementDisplayName;
		
		if (props == null) {
			LOGGER.error("ERROR: Properties parameter is NULL!");
			return;
		}
		
		if (element == null) {
			LOGGER.error("ERROR: Element parameter is NULL!");
			return;
		}

		String sc = props.getEntryValue("scale");
		if ((sc != null) && MathUtils.isFloat(sc)) {
			this.scale = Float.parseFloat(sc);
		}
		
		//unused
		String ro = props.getEntryValue("rotation");
		if ((ro != null) && MathUtils.isFloat(ro)) {
			this.rotation = Float.parseFloat(ro);
		}
		
		String vs = props.getEntryValue("visible");
		if ((vs != null) && vs.equalsIgnoreCase("false")) {
			this.vanillaVisible = false;
		}
		
		String ori = props.getEntryValue("orientation");
		if (ori != null) {
			this.isOriginalOrientation = false;
		}
		
		if (this.isOriginalOrientation) {
			this.posX = Integer.MAX_VALUE;
			this.posY = Integer.MAX_VALUE;
		}
		
		String x = props.getEntryValue("x");
		String y = props.getEntryValue("y");
		if (x != null) {
			if (MathUtils.isInteger(x)) {
				this.posX = Integer.parseInt(x);
			}
		}
		if (y != null) {
			if (MathUtils.isInteger(y)) {
				this.posY = Integer.parseInt(y);
			}
		}
		
		this.width = this.element.width;
		this.height = this.element.height;
		
	}

	@Override
	public void render(PoseStack matrix) {
		
		if (this.element == null) {
			LOGGER.error("ERROR: Element field is NULL!");
			return;
		}
		
		this.width = this.element.width;
		this.height = this.element.height;
		
		if (!(this.isOriginalPosX && this.isSecondItemOfThisType)) {
			this.element.x = this.getPosX();
		}
		if (!(this.isOriginalPosY && this.isSecondItemOfThisType)) {
			this.element.y = this.getPosY();
		}
		
		this.element.scale = this.scale;
		
		if (!this.vanillaVisible || !this.isSecondItemOfThisType) {
			this.element.visible = this.vanillaVisible;
		}
		
		if (!this.fireEvents || !this.isSecondItemOfThisType) {
			this.element.fireEvents = this.fireEvents;
		}
		
	}

}
