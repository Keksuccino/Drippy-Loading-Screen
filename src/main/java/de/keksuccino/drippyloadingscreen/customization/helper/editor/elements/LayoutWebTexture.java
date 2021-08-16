package de.keksuccino.drippyloadingscreen.customization.helper.editor.elements;

import java.util.ArrayList;
import java.util.List;

import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.items.WebTextureCustomizationItem;
import de.keksuccino.konkrete.properties.PropertiesSection;

public class LayoutWebTexture extends LayoutElement {
	
	public LayoutWebTexture(WebTextureCustomizationItem parent, LayoutEditorScreen handler) {
		super(parent, true, handler);
	}

	@Override
	public void init() {
		this.stretchable = true;
		super.init();
	}

	@Override
	public List<PropertiesSection> getProperties() {
		List<PropertiesSection> l = new ArrayList<PropertiesSection>();
		
		PropertiesSection p1 = new PropertiesSection("customization");
		p1.addEntry("action", "addwebtexture");
		p1.addEntry("actionid", this.object.getActionId());
		if (this.object.delayAppearance) {
			p1.addEntry("delayappearance", "true");
			p1.addEntry("delayappearanceeverytime", "" + this.object.delayAppearanceEverytime);
			p1.addEntry("delayappearanceseconds", "" + this.object.delayAppearanceSec);
			if (this.object.fadeIn) {
				p1.addEntry("fadein", "true");
				p1.addEntry("fadeinspeed", "" + this.object.fadeInSpeed);
			}
		}
		p1.addEntry("url", ((WebTextureCustomizationItem)this.object).rawURL);
		p1.addEntry("orientation", this.object.orientation);
		if (this.stretchX) {
			p1.addEntry("x", "0");
			p1.addEntry("width", "%guiwidth%");
		} else {
			p1.addEntry("x", "" + this.object.posX);
			p1.addEntry("width", "" + this.object.width);
		}
		if (this.stretchY) {
			p1.addEntry("y", "0");
			p1.addEntry("height", "%guiheight%");
		} else {
			p1.addEntry("y", "" + this.object.posY);
			p1.addEntry("height", "" + this.object.height);
		}

		l.add(p1);
		
		return l;
	}

}
