package de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.vanilla;

import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.FHTextInputPopup;
import de.keksuccino.drippyloadingscreen.customization.items.vanilla.ProgressBarSplashCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements.ProgressBarSplashElement;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.konkrete.properties.PropertiesSection;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ProgressBarLayoutSplashElement extends VanillaLayoutSplashElement {

	public ProgressBarLayoutSplashElement(ProgressBarSplashCustomizationItem object, LayoutEditorScreen handler) {
		super(object, handler);
	}
	
	@Override
	public void init() {
		
		this.scaleable = false;
		
		super.init();

		this.rightclickMenu.addSeparator();

		/** BAR COLOR **/
		AdvancedButton barColorButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.editor.elements.vanilla.progressbar.color"), (press) -> {
			ProgressBarSplashElement se = (ProgressBarSplashElement) this.getVanillaObject().element;
			FHTextInputPopup pop = new FHTextInputPopup(new Color(0, 0, 0, 0), Locals.localize("drippyloadingscreen.helper.editor.elements.vanilla.progressbar.color"), null, 240, (call) -> {
				if (call != null) {
					if (!call.equals(se.customBarColorHex)) {
						this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
					}
					se.customBarColorHex = call;
				}
			});
			if (se.customBarColorHex != null) {
				pop.setText(se.customBarColorHex);
			}
			PopupHandler.displayPopup(pop);
		});
		barColorButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.editor.elements.vanilla.progressbar.color.btn.desc"), "%n%"));
		this.rightclickMenu.addContent(barColorButton);
		
	}

	@Override
	public List<PropertiesSection> getProperties() {
		List<PropertiesSection> l = new ArrayList<PropertiesSection>();
		PropertiesSection p = new PropertiesSection("customization");

		p.addEntry("action", "editprogressbar");
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

		ProgressBarSplashElement se = (ProgressBarSplashElement) this.getVanillaObject().element;

		if (se.customBarColorHex != null) {
			p.addEntry("barcolor", se.customBarColorHex);
		}
		
		if (p.getEntries().size() > 1) {
			l.add(p);
		}
		
		return l;
	}
	
	@Override
	public void resetElement() {
		PropertiesSection props = new PropertiesSection("customization");
		this.handler.splashLayer.progressBarSplashElement.onReloadCustomizations();
		this.object = new ProgressBarSplashCustomizationItem(this.handler.splashLayer.progressBarSplashElement, props, false);
	}

}
