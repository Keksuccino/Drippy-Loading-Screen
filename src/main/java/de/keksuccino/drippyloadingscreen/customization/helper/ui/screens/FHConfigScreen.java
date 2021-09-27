package de.keksuccino.drippyloadingscreen.customization.helper.ui.screens;

import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.konkrete.config.ConfigEntry;
import de.keksuccino.konkrete.gui.screens.ConfigScreen;
import de.keksuccino.konkrete.localization.Locals;
import net.minecraft.client.gui.screens.Screen;

public class FHConfigScreen extends ConfigScreen {

	public FHConfigScreen(Screen parent) {
		super(DrippyLoadingScreen.config, Locals.localize("drippyloadingscreen.config"), parent);
	}
	
	@Override
	protected void init() {
		
		super.init();
		
		for (String s : this.config.getCategorys()) {
			this.setCategoryDisplayName(s, Locals.localize("drippyloadingscreen.config.categories." + s));
		}
		
		for (ConfigEntry e : this.config.getAllAsEntry()) {
			this.setValueDisplayName(e.getName(), Locals.localize("drippyloadingscreen.config." + e.getName()));
			this.setValueDescription(e.getName(), Locals.localize("drippyloadingscreen.config." + e.getName() + ".desc"));
		}
		
	}
	
}
