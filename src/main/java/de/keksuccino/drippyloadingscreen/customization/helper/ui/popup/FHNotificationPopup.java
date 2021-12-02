package de.keksuccino.drippyloadingscreen.customization.helper.ui.popup;

import java.awt.Color;

import de.keksuccino.drippyloadingscreen.customization.helper.ui.UIBase;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.screens.popup.NotificationPopup;

public class FHNotificationPopup extends NotificationPopup {

	public FHNotificationPopup(int width, Color color, int backgroundAlpha, Runnable callback, String... text) {
		super(width, color, backgroundAlpha, callback, text);
	}

	@Override
	protected void colorizePopupButton(AdvancedButton b) {
		UIBase.colorizeButton(b);
	}

}
