package de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.custombars;

import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.items.custombars.CustomProgressBarCustomizationItem;
import de.keksuccino.konkrete.properties.PropertiesSection;

public class LayoutCustomProgressBar extends LayoutCustomBarBase {

	public LayoutCustomProgressBar(CustomProgressBarCustomizationItem object, LayoutEditorScreen handler) {
		super(object, handler);
	}
	
	@Override
	protected PropertiesSection getPropertiesRaw() {
		PropertiesSection s = super.getPropertiesRaw();
		s.addEntry("action", "addcustomprogressbar");
		return s;
	}

}
