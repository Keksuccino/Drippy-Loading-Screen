package de.keksuccino.drippyloadingscreen.customization.helper.ui.popup;

import java.awt.Color;
import java.util.function.Consumer;

import de.keksuccino.drippyloadingscreen.customization.helper.ui.content.DynamicValueTextfield;
import de.keksuccino.konkrete.input.CharacterFilter;
import net.minecraft.client.MinecraftClient;

public class DynamicValueInputPopup extends FHTextInputPopup {

	public DynamicValueInputPopup(Color color, String title, CharacterFilter filter, int alpha) {
		super(color, title, filter, alpha);
	}
	
	public DynamicValueInputPopup(Color color, String title, CharacterFilter filter, int backgroundAlpha, Consumer<String> callback) {
		super(color, title, filter, backgroundAlpha, callback);
	}

	@Override
	protected void init(Color color, String title, CharacterFilter filter, Consumer<String> callback) {
		
		super.init(color, title, filter, callback);
		
		this.textField = new DynamicValueTextfield(MinecraftClient.getInstance().textRenderer, 0, 0, 200, 20, true, filter);
		this.textField.setFocusUnlocked(true);
		this.textField.setFocused(false);
		this.textField.setMaxLength(1000);
		
	}
	

}
