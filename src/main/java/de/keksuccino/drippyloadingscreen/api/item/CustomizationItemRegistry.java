package de.keksuccino.drippyloadingscreen.api.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.keksuccino.drippyloadingscreen.logger.Logging;

public class CustomizationItemRegistry {
	
	private Map<String, CustomizationItemContainer> elements = new TreeMap<String, CustomizationItemContainer>();
	
	private static CustomizationItemRegistry instance;

	public void register(CustomizationItemContainer container) {
		if (!elements.containsKey(container.elementIdentifier)) {
			this.elements.put(container.elementIdentifier, container);
		} else {
			Logging.error("CustomizationElementRegistry#register():",
					"Invalid element identifier '" + container.elementIdentifier + "' found!",
					"Customization element with the same identifier already exists!");
		}
	}
	
	public Map<String, CustomizationItemContainer> getElements() {
		return this.elements;
	}
	
	public List<CustomizationItemContainer> getElementsAsList() {
		List<CustomizationItemContainer> l = new ArrayList<CustomizationItemContainer>();
		l.addAll(this.elements.values());
		return l;
	}
	
	public CustomizationItemContainer getElement(String identifier) {
		return this.elements.get(identifier);
	}
	
	public boolean elementExists(String identifier) {
		return this.elements.containsKey(identifier);
	}
	
	public static CustomizationItemRegistry getInstance() {
		if (instance == null) {
			instance = new CustomizationItemRegistry();
		}
		return instance;
	}

}
