package de.keksuccino.drippyloadingscreen.api.item;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CustomizationItemRegistry {

	private static final Logger LOGGER = LogManager.getLogger("drippyloadingscreen/CustomizationItemRegistry");
	
	private Map<String, CustomizationItemContainer> elements = new TreeMap<String, CustomizationItemContainer>();
	
	private static CustomizationItemRegistry instance;

	public void register(CustomizationItemContainer container) {
		if (!elements.containsKey(container.elementIdentifier)) {
			this.elements.put(container.elementIdentifier, container);
		} else {
			LOGGER.error("ERROR: Invalid element identifier '" + container.elementIdentifier + "' found!");
			LOGGER.error("ERROR: Customization element with the same identifier already exists!");
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
