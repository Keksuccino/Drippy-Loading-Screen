package de.keksuccino.drippyloadingscreen.customization.helper.ui.popup;

import java.io.File;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.keksuccino.drippyloadingscreen.customization.helper.ui.UIBase;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.screens.popup.FilePickerPopup;
import de.keksuccino.konkrete.gui.screens.popup.Popup;
import net.minecraft.client.gui.screen.Screen;

public class FHFilePickerPopup extends FilePickerPopup {

	public FHFilePickerPopup(String directory, String home, Popup fallback, boolean checkForLastPath, Consumer<File> callback, String[] filetypes) {
		super(directory, home, fallback, checkForLastPath, callback, filetypes);
	}
	
	public FHFilePickerPopup(String directory, String home, Popup fallback, boolean checkForLastPath, Consumer<File> callback) {
		super(directory, home, fallback, checkForLastPath, callback);
	}
	
	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, Screen renderIn) {
		super.render(matrix, mouseX, mouseY, renderIn);
	}
	
	@Override
	protected void colorizePopupButton(AdvancedButton b) {
		UIBase.colorizeButton(b);
	}

}
