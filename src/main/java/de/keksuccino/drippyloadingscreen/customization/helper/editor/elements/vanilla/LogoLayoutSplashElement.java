package de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.vanilla;

import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.items.vanilla.LogoSplashCustomizationItem;
import de.keksuccino.konkrete.properties.PropertiesSection;

import java.util.ArrayList;
import java.util.List;

public class LogoLayoutSplashElement extends VanillaLayoutSplashElement {

	public LogoLayoutSplashElement(LogoSplashCustomizationItem object, LayoutEditorScreen handler) {
		super(object, handler);
	}
	
	@Override
	public void init() {
		
		this.scaleable = false;
		
		super.init();

		this.rightclickMenu.addSeparator();
		
	}

	@Override
	public List<PropertiesSection> getProperties() {
		List<PropertiesSection> l = new ArrayList<PropertiesSection>();
		PropertiesSection p = new PropertiesSection("customization");
		
		p.addEntry("action", "editlogo");
		if (!this.getVanillaObject().isOriginalOrientation) {
			p.addEntry("orientation", this.object.orientation);
		}
		if (!this.getVanillaObject().isOriginalPosX) {
			p.addEntry("x", "" + this.object.posX);
		}
		if (!this.getVanillaObject().isOriginalPosY) {
			p.addEntry("y", "" + this.object.posY);
		}
		if (!this.getVanillaObject().vanillaVisible) {
			p.addEntry("visible", "" + this.getVanillaObject().vanillaVisible);
		}
		
		if (p.getEntries().size() > 1) {
			l.add(p);
		}
		
		return l;
	}
	
	@Override
	public void resetElement() {
		PropertiesSection props = new PropertiesSection("customization");
		this.handler.splashLayer.logoSplashElement.onReloadCustomizations();
		this.object = new LogoSplashCustomizationItem(this.handler.splashLayer.logoSplashElement, props, false);
	}

}
