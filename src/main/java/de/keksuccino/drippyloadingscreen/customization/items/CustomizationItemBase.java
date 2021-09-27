package de.keksuccino.drippyloadingscreen.customization.items;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.customization.CustomizationHandler;
import de.keksuccino.drippyloadingscreen.customization.placeholdervalues.PlaceholderTextValueHelper;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.items.visibilityrequirements.VisibilityRequirementContainer;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;

public abstract class CustomizationItemBase extends GuiComponent {
	
	/**
	 * This value CANNOT BE NULL!<br>
	 * If null, {@link CustomizationItemBase#shouldRender()} will never return true.
	 */
	public String value;
	public String action;
	/**
	 * NOT similar to {@link CustomizationItemBase#getPosX()}! This is the raw value without the defined orientation and scale!
	 */
	public int posX = 0;
	/**
	 * NOT similar to {@link CustomizationItemBase#getPosY()}! This is the raw value without the defined orientation and scale!
	 */
	public int posY = 0;
	public String orientation = "top-left";
	public int width = -1;
	public int height = -1;

	public volatile boolean delayAppearance = false;
	public volatile boolean delayAppearanceEverytime = false;
	public volatile float delayAppearanceSec = 1.0F;
	public volatile boolean visible = true;
	public volatile boolean fadeIn = false;
	public volatile float fadeInSpeed = 1.0F;
	public volatile float opacity = 1.0F;

	public VisibilityRequirementContainer visibilityRequirementContainer;

	protected String actionId;

	public CustomizationItemBase(PropertiesSection properties) {
		
		this.action = properties.getEntryValue("action");

		this.actionId = properties.getEntryValue("actionid");
		if (this.actionId == null) {
			this.actionId = CustomizationHandler.generateRandomActionId();
		}
		
		String op = properties.getEntryValue("opacity");
		if ((op != null) && MathUtils.isFloat(op)) {
			this.opacity = Float.parseFloat(op);
		}

		String x = properties.getEntryValue("x");
		String y = properties.getEntryValue("y");
		if (x != null) {
			if (MathUtils.isInteger(x)) {
				this.posX = Integer.parseInt(x);
			}
		}
		if (y != null) {
			if (MathUtils.isInteger(y)) {
				this.posY = Integer.parseInt(y);
			}
		}
	
		String o = properties.getEntryValue("orientation");
		if (o != null) {
			this.orientation = o;
		}

		String w = properties.getEntryValue("width");
		if (w != null) {
			w = PlaceholderTextValueHelper.convertFromRaw(w);
			if (MathUtils.isInteger(w)) {
				this.width = Integer.parseInt(w);
			}
			if (this.width < 0) {
				this.width = 0;
			}
		}

		String h = properties.getEntryValue("height");
		if (h != null) {
			h = PlaceholderTextValueHelper.convertFromRaw(h);
			if (MathUtils.isInteger(h)) {
				this.height = Integer.parseInt(h);
			}
			if (this.height < 0) {
				this.height = 0;
			}
		}

		this.visibilityRequirementContainer = new VisibilityRequirementContainer(properties, this);

	}

	public abstract void render(PoseStack matrix);
	
	/**
	 * Should be used to get the REAL and final X-position of this item.<br>
	 * NOT similar to {@code MenuCustomizationItem.posX}! 
	 */
	public int getPosX() {
		
		int w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		int x = this.posX;

		if (orientation.equalsIgnoreCase("top-centered")) {
			x = x - (this.width / 2) + (w / 2);
		}
		if (orientation.equalsIgnoreCase("mid-centered")) {
			x = x - (this.width / 2) + (w / 2);
		}
		if (orientation.equalsIgnoreCase("bottom-centered")) {
			x = x - (this.width / 2) + (w / 2);
		}
		//-----------------------------
		if (orientation.equalsIgnoreCase("top-right")) {
			x += w - this.width;
		}
		if (orientation.equalsIgnoreCase("mid-right")) {
			x += w - this.width;
		}
		if (orientation.equalsIgnoreCase("bottom-right")) {
			x += w - this.width;
		}
		
		return x;
	}
	
	/**
	 * Should be used to get the REAL and final Y-position of this item.<br>
	 * NOT similar to {@code MenuCustomizationItem.posY}! 
	 */
	public int getPosY() {
		
		int h = Minecraft.getInstance().getWindow().getGuiScaledHeight();
		int y = this.posY;

		if (orientation.equalsIgnoreCase("mid-left")) {
			y = y - (this.height / 2) + (h / 2);
		}
		if (orientation.equalsIgnoreCase("bottom-left")) {
			y += h - this.height;
		}
		//----------------------------
		if (orientation.equalsIgnoreCase("mid-centered")) {
			y = y - (this.height / 2) + (h / 2);
		}
		if (orientation.equalsIgnoreCase("bottom-centered")) {
			y += h - this.height;
		}
		//-----------------------------
		if (orientation.equalsIgnoreCase("mid-right")) {
			y = y - (this.height / 2) + (h / 2);
		}
		if (orientation.equalsIgnoreCase("bottom-right")) {
			y += h - this.height;
		}
		
		return y;
	}
	
	public boolean shouldRender() {
		if (this.value == null) {
			return false;
		}
		if (!this.visibilityRequirementsMet()) {
			return false;
		}
		return this.visible;
	}

	public String getActionId() {
		return this.actionId;
	}

	protected static boolean isEditorActive() {
		return (Minecraft.getInstance().screen instanceof LayoutEditorScreen);
	}

	protected boolean visibilityRequirementsMet() {
		if (isEditorActive()) {
			return true;
		}
		return this.visibilityRequirementContainer.isVisible();
	}

	public static enum Alignment {
		
		LEFT("left"),
		RIGHT("right"),
		CENTERED("centered");
		
		public final String key;
		
		private Alignment(String key) {
			this.key = key;
		}
		
	}

}
