//TODO übernehmen
package de.keksuccino.drippyloadingscreen.api.item.v2;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.LayoutElement;
import de.keksuccino.konkrete.properties.PropertiesSection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This gets used in the layout editor to handle an item.<br>
 * There's one {@link LayoutEditorElement} per item.<br><br>
 *
 * Used to <b>serialize the item instance</b> when the layout gets saved,
 * allows the user to <b>customize the item</b> (via the right-click context menu, etc.) and
 * renders the {@link CustomizationItem} instance to the editor.
 */
public abstract class LayoutEditorElement extends LayoutElement {

    private static final Logger LOGGER = LogManager.getLogger("drippyloadingscreen/LayoutEditorElement");

    public final CustomizationItemContainer parentItemContainer;

    /**
     * @param parentContainer The parent {@link CustomizationItemContainer} instance.
     * @param customizationItemInstance The {@link CustomizationItem} instance this {@link LayoutEditorElement} should handle.
     * @param destroyable If the element is destroyable using the 'Delete' button in the right-click context menu of the element.
     * @param handler The {@link LayoutEditorScreen} instance that handles this element.
     */
    public LayoutEditorElement(@NotNull CustomizationItemContainer parentContainer, @NotNull CustomizationItem customizationItemInstance, boolean destroyable, @NotNull LayoutEditorScreen handler) {
        super(customizationItemInstance, destroyable, handler);
        this.parentItemContainer = parentContainer;
    }

    /**
     * Called when the {@link LayoutElement} gets initialized in the layout editor.<br><br>
     *
     * Here you can add your own entries to the {@link LayoutElement#rightclickMenu} (right-click context menu) of the element and
     * do other things that could be important before the element can be used and gets rendered.
     */
    @Override
    public void init() {
        super.init();
    }

    /**
     * Renders the {@link LayoutElement} in the layout editor.<br>
     * Will also render the {@link CustomizationItem} and is used as ticker to handle resizing, moving and other things of the element.<br><br>
     *
     * You don't need to touch this method in most cases.
     *
     * @param matrix The {@link PoseStack} used to render {@link LayoutEditorElement}s.
     * @param mouseX X position of the mouse.
     * @param mouseY Y position of the mouse.
     */
    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY) {
        super.render(matrix, mouseX, mouseY);
    }

    /**
     * Returns the customized item instance, serialized to a {@link SimplePropertiesSection}.<br><br>
     *
     * You need to add all variables of the customized item as entries to the {@link SimplePropertiesSection} here.<br>
     * This {@link SimplePropertiesSection} will get saved to the layout file, to later deserialize it in
     * {@link CustomizationItemContainer#constructCustomizedItemInstance(PropertiesSection)} to construct a customized instance of the item.
     *
     * @return The serialized item instance.
     */
    public abstract SimplePropertiesSection serializeItem();

    @Override
    public List<PropertiesSection> getProperties() {
        List<PropertiesSection> l = new ArrayList<>();
        PropertiesSection sec = this.serializeItem();
        if (sec == null) {
            sec = new SimplePropertiesSection();
        }
        if (sec.hasEntry("action")) {
            LOGGER.warn("WARNING: Entry key 'action' for serialized customization item instances is reserved by the system. Overriding entry!");
            sec.removeEntry("action");
        }
        sec.addEntry("action", "custom_layout_element:" + this.parentItemContainer.getIdentifier());
        sec.addEntry("actionid", this.object.getActionId());
        if (this.object.delayAppearance) {
            sec.addEntry("delayappearance", "true");
            sec.addEntry("delayappearanceeverytime", "" + this.object.delayAppearanceEverytime);
            sec.addEntry("delayappearanceseconds", "" + this.object.delayAppearanceSec);
            if (this.object.fadeIn) {
                sec.addEntry("fadein", "true");
                sec.addEntry("fadeinspeed", "" + this.object.fadeInSpeed);
            }
        }
        sec.addEntry("x", "" + this.object.posX);
        sec.addEntry("y", "" + this.object.posY);
        sec.addEntry("orientation", this.object.orientation);
        if (this.object.orientation.equals("element") && (this.object.orientationElementIdentifier != null)) {
            sec.addEntry("orientation_element", this.object.orientationElementIdentifier);
        }
        if (this.stretchX) {
            sec.addEntry("x", "0");
            sec.addEntry("width", "%guiwidth%");
        } else {
            sec.addEntry("x", "" + this.object.posX);
            sec.addEntry("width", "" + this.object.width);
        }
        if (this.stretchY) {
            sec.addEntry("y", "0");
            sec.addEntry("height", "%guiheight%");
        } else {
            sec.addEntry("y", "" + this.object.posY);
            sec.addEntry("height", "" + this.object.height);
        }
//        this.addVisibilityPropertiesTo(sec);
        l.add(sec);
        return l;
    }

    public class SimplePropertiesSection extends PropertiesSection {

        public SimplePropertiesSection() {
            super("customization");
        }

    }

}
