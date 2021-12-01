package de.keksuccino.drippyloadingscreen.api.item;

import de.keksuccino.konkrete.properties.PropertiesSection;

public abstract class CustomizationItemContainer {
	
	public final String elementIdentifier;
	public String displayName;
	
	public CustomizationItemContainer(String elementIdentifier, String displayName) {
		this.elementIdentifier = elementIdentifier;
		this.displayName = displayName;
	}
	
	/**
	 * Called when the button to create a new instance of this item is pressed.<br>
	 * This is a blank/not customized instance.
	 */
	public abstract CustomizationItem createNew();
	
	/**
	 * Called to construct an instance of this item using a {@link PropertiesSection} containing customizations for this item.<br>
	 * This is used to create the customized instances of items you see in the real/final HUD.
	 * 
	 * @param properties All properties from the layout file that need to be applied to the item. Will always contain all serialized properties that were previously written to the layout file via {@link CustomizationItem#getProperties()}.
	 */
	public abstract CustomizationItem constructWithProperties(PropertiesSection properties);
	
}
