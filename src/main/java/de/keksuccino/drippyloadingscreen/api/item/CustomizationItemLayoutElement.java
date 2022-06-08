package de.keksuccino.drippyloadingscreen.api.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.LayoutElement;
import de.keksuccino.drippyloadingscreen.logger.Logging;
import de.keksuccino.konkrete.properties.PropertiesSection;

public class CustomizationItemLayoutElement extends LayoutElement {

	public CustomizationItemContainer container;
	
	public CustomizationItemLayoutElement(CustomizationItemContainer container, CustomizationItem elementInstance, LayoutEditorScreen handler) {
		super(elementInstance, true, handler);
		this.container = container;
		elementInstance.onInitEditor(this, this.rightclickMenu);
	}

	@Override
	public List<PropertiesSection> getProperties() {
		
		List<PropertiesSection> l = new ArrayList<PropertiesSection>();
		PropertiesSection sec = new PropertiesSection("customization");
		
		sec.addEntry("action", "add_" + this.container.elementIdentifier);
		
		sec.addEntry("actionid", this.object.getActionId());
		
		sec.addEntry("orientation", this.object.orientation);
		
		sec.addEntry("x", "" + this.object.posX);
		
		sec.addEntry("y", "" + this.object.posY);
		
		sec.addEntry("width", "" + this.object.width);
		
		sec.addEntry("height", "" + this.object.height);
		
		Map<String, String> customProps = this.getCustomizationElement().getProperties();
		if (customProps != null) {
			for (Map.Entry<String, String> m : customProps.entrySet()) {
				if (!sec.hasEntry(m.getKey())) {
					sec.addEntry(m.getKey(), m.getValue());
				} else {
					printPropertiesKeyError(m.getKey());
				}
			}
		}
		
		l.add(sec);
		
		return l;
		
	}
	
	protected void printPropertiesKeyError(String key) {
		Logging.error("CustomizationLayoutElement#getProperties():",
				"Invalid properties key '" + key + "' found for customization element '" + this.container.elementIdentifier + "'!",
				"This key already exists or is reserved by the system and can't be used!");
	}
	
	protected CustomizationItem getCustomizationElement() {
		return (CustomizationItem) this.object;
	}

}
