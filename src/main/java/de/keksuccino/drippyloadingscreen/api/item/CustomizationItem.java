package de.keksuccino.drippyloadingscreen.api.item;

import java.util.Map;

import javax.annotation.Nullable;

import de.keksuccino.drippyloadingscreen.customization.helper.ui.content.FHContextMenu;
import de.keksuccino.drippyloadingscreen.customization.items.CustomizationItemBase;
import de.keksuccino.konkrete.properties.PropertiesSection;

public abstract class CustomizationItem extends CustomizationItemBase {

	public CustomizationItemContainer container;
	
	public CustomizationItem(PropertiesSection props, CustomizationItemContainer container) {
		super(props);
		this.container = container;
		
		this.value = this.container.displayName;
	}
	
	/**
	 * Called when the editor gets updated or initialized.<br>
	 * Can be used to add custom entries to the rightclick context menu of the element and do other stuff that needs to be done on init.
	 * 
	 * @param layoutElement Handles everything around the customization item in the editor and renders the item-related editor UI like the border when the item is focused.
	 * @param rightclickContextMenu The context menu that appears when you rightclick the item in the editor. This menu already contains default actions every item needs and is part of the <b>layoutElement</b>.
	 */
	public abstract void onInitEditor(CustomizationItemLayoutElement layoutElement, FHContextMenu rightclickContextMenu);
	
	/**
	 * Returns a {@link Map} with the <b>current</b> properties of the customization item.<br>
	 * This is used by the editor to write the customization properties to the layout file.<br><br>
	 * 
	 * Can be used to add custom properties to the item.<br>
	 * Default properties ({@code orientation}, {@code x}, {@code y}, {@code width}) and {@code height} get added automatically, so you don't need to add these.<br><br>
	 * 
	 * The first {@link String} of the {@link Map} is the properties key and the second one the actual value.
	 */
	@Nullable
	public abstract Map<String, String> getProperties();
	
}
