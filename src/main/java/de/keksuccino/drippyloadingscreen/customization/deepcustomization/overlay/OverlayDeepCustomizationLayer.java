package de.keksuccino.drippyloadingscreen.customization.deepcustomization.overlay;

import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.drippyloadingscreen.customization.deepcustomization.overlay.background.OverlayBackgroundElement;
import de.keksuccino.drippyloadingscreen.customization.deepcustomization.overlay.forge.forgelog.OverlayForgeLogElement;
import de.keksuccino.drippyloadingscreen.customization.deepcustomization.overlay.forge.forgememory.OverlayForgeMemoryElement;
import de.keksuccino.drippyloadingscreen.customization.deepcustomization.overlay.logo.OverlayLogoElement;
import de.keksuccino.drippyloadingscreen.customization.deepcustomization.overlay.progressbar.OverlayProgressBarElement;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationLayer;

public class OverlayDeepCustomizationLayer extends DeepCustomizationLayer {

    public OverlayDeepCustomizationLayer() {

        super(DrippyOverlayScreen.class.getName());

        this.registerElement(new OverlayLogoElement(this));
        this.registerElement(new OverlayProgressBarElement(this));
        this.registerElement(new OverlayBackgroundElement(this));

        //Forge ---------->
        this.registerElement(new OverlayForgeMemoryElement(this));
        this.registerElement(new OverlayForgeLogElement(this));

    }

}
