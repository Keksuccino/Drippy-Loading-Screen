package de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.vanilla;

import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.FHTextInputPopup;
import de.keksuccino.drippyloadingscreen.customization.items.vanilla.ForgeTextSplashCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements.ForgeTextSplashElement;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.konkrete.properties.PropertiesSection;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ForgeTextLayoutSplashElement extends VanillaLayoutSplashElement {

	public ForgeTextLayoutSplashElement(ForgeTextSplashCustomizationItem object, LayoutEditorScreen handler) {
		super(object, handler);
	}
	
	@Override
	public void init() {
		
		this.scaleable = true;
		
		super.init();

		this.rightclickMenu.addSeparator();

		/** TEXT COLOR **/
		AdvancedButton textColorButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.editor.elements.text.color"), (press) -> {
			ForgeTextSplashElement se = (ForgeTextSplashElement) this.getVanillaObject().element;
			FHTextInputPopup pop = new FHTextInputPopup(new Color(0, 0, 0, 0), Locals.localize("drippyloadingscreen.helper.editor.elements.text.color"), null, 240, (call) -> {
				if (call != null) {
					if (!call.equals(se.customTextColorHex)) {
						this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
					}
					se.customTextColorHex = call;
				}
			});
			if (se.customTextColorHex != null) {
				pop.setText(se.customTextColorHex);
			}
			PopupHandler.displayPopup(pop);
		});
		textColorButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.editor.elements.text.color.btn.desc"), "%n%"));
		this.rightclickMenu.addContent(textColorButton);
		
	}

	@Override
	public List<PropertiesSection> getProperties() {
		List<PropertiesSection> l = new ArrayList<PropertiesSection>();
		PropertiesSection p = new PropertiesSection("customization");
		
		p.addEntry("action", "editforgestatustext");
		if (!this.getVanillaObject().isOriginalOrientation) {
			p.addEntry("orientation", this.object.orientation);
			if (this.object.orientation.equals("loading-progress") && (this.object.orientationElementIdentifier != null)) {
				p.addEntry("orientation_element", this.object.orientationElementIdentifier);
			}
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
		if (this.getVanillaObject().scale != 1.0F) {
			p.addEntry("scale", "" + this.getVanillaObject().scale);
		}

		ForgeTextSplashElement se = (ForgeTextSplashElement) this.getVanillaObject().element;

		if (se.customTextColorHex != null) {
			p.addEntry("textcolor", se.customTextColorHex);
		}
		
		if (p.getEntries().size() > 1) {
			l.add(p);
		}
		
		return l;
	}
	
	@Override
	public void resetElement() {
		PropertiesSection props = new PropertiesSection("customization");
//		this.handler.splashLayer.forgeTextSplashElement.onReloadCustomizations();
//		this.object = new ForgeTextSplashCustomizationItem(this.handler.splashLayer.forgeTextSplashElement, props, false);
	}

}
