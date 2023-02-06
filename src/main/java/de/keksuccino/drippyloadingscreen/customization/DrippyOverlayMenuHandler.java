package de.keksuccino.drippyloadingscreen.customization;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.customization.deepcustomization.overlay.background.OverlayBackgroundItem;
import de.keksuccino.drippyloadingscreen.customization.deepcustomization.overlay.logo.OverlayLogoItem;
import de.keksuccino.drippyloadingscreen.customization.deepcustomization.overlay.progressbar.OverlayProgressBarItem;
import de.keksuccino.fancymenu.menu.button.ButtonCachedEvent;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerBase;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationElement;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationLayer;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationLayerRegistry;
import de.keksuccino.konkrete.properties.PropertiesSection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

public class DrippyOverlayMenuHandler extends MenuHandlerBase {

    private static final Logger LOGGER = LogManager.getLogger();

    public boolean showLogo = true;
    public boolean showProgressBar = true;
    public Color customProgressBarColor = null;
    public Color customBackgroundColor = null;

    public OverlayProgressBarItem progressBarItem = null;
    public OverlayLogoItem logoItem = null;

    public DrippyOverlayMenuHandler() {
        super(DrippyOverlayScreen.class.getName());
    }

    @Override
    public void onButtonsCached(ButtonCachedEvent e) {
        if (this.shouldCustomize(e.getScreen())) {
            if (MenuCustomization.isMenuCustomizable(e.getScreen())) {

                try {

                    //Reset all deep customization fields
                    this.showLogo = true;
                    this.showProgressBar = true;
                    this.customProgressBarColor = null;
                    this.customBackgroundColor = null;
                    this.logoItem = (OverlayLogoItem) DeepCustomizationLayerRegistry.getLayerByMenuIdentifier(this.getMenuIdentifier()).getElementByIdentifier("drippy_overlay_logo").constructDefaultItemInstance();
                    this.progressBarItem = (OverlayProgressBarItem) DeepCustomizationLayerRegistry.getLayerByMenuIdentifier(this.getMenuIdentifier()).getElementByIdentifier("drippy_overlay_progress_bar").constructDefaultItemInstance();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                super.onButtonsCached(e);

            }
        }
    }

    @Override
    protected void applyLayout(PropertiesSection sec, String renderOrder, ButtonCachedEvent e) {

        String action = sec.getEntryValue("action");

        //Disable unsupported element types in Drippy layouts
        if (action.equals("custom_layout_element:fancymenu_customization_player_entity")) {
            return;
        }
        if (action.equals("custom_layout_element:fancymenu_extension:audio_item")) {
            return;
        }

        super.applyLayout(sec, renderOrder, e);

        DeepCustomizationLayer layer = DeepCustomizationLayerRegistry.getLayerByMenuIdentifier(this.getMenuIdentifier());
        if (layer != null) {

            if (action != null) {

                if (action.startsWith("deep_customization_element:")) {
                    String elementId = action.split("[:]", 2)[1];
                    DeepCustomizationElement element = layer.getElementByIdentifier(elementId);
                    if (element != null) {
                        DeepCustomizationItem i = element.constructCustomizedItemInstance(sec);
                        if (i != null) {

                            if (elementId.equals("drippy_overlay_logo")) {
                                this.showLogo = !(i.hidden);
                                this.logoItem = (OverlayLogoItem) i;
                            }
                            if (elementId.equals("drippy_overlay_progress_bar")) {
                                this.showProgressBar = !(i.hidden);
                                this.customProgressBarColor = ((OverlayProgressBarItem)i).hexColor;
                                this.progressBarItem = (OverlayProgressBarItem) i;
                            }
                            if (elementId.equals("drippy_overlay_background")) {
                                this.customBackgroundColor = ((OverlayBackgroundItem)i).hexColor;
                            }

                            //Forge -------------->
//                            if (elementId.equals("title_screen_forge_copyright")) {
//                                this.showForgeNotificationCopyright = !(i.hidden);
//                            }
//                            if (elementId.equals("title_screen_forge_top")) {
//                                this.showForgeNotificationTop = !(i.hidden);
//                            }

                        }
                    }
                }

            }

        }

    }

    @Override
    protected void renderBackground(PoseStack matrix, Screen s) {
        super.renderBackground(matrix, s);
        if (Minecraft.getInstance().getOverlay() == null) {
            if (this.shouldCustomize(s)) {
                if (!MenuCustomization.isMenuCustomizable(s)) {
                    return;
                }
                if ((this.logoItem != null) && !this.logoItem.hidden) {
                    this.logoItem.render(matrix, s);
                }
                if ((this.progressBarItem != null) && !this.progressBarItem.hidden) {
                    this.progressBarItem.render(matrix, s);
                }
            }
        }
    }

}
