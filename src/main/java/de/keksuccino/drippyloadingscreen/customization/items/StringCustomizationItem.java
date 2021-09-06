package de.keksuccino.drippyloadingscreen.customization.items;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import de.keksuccino.drippyloadingscreen.customization.placeholdervalues.PlaceholderTextValueHelper;
import de.keksuccino.drippyloadingscreen.customization.rendering.SimpleTextRenderer;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.rendering.RenderUtils;

import de.keksuccino.drippyloadingscreen.customization.rendering.RainbowText;

public class StringCustomizationItem extends CustomizationItemBase {
	
	public float scale = 1.0F;
	public boolean shadow = false;
	public Alignment alignment = Alignment.LEFT;

	public Color textColor = Color.WHITE;
	public String textColorHex = "#ffffff";
	
	public boolean rainbowMode = false;
	public String rainbowStartColorHex1 = null;
	public String rainbowStartColorHex2 = null;
	public String rainbowEndColorHex1 = null;
	public String rainbowEndColorHex2 = null;
	
	public RainbowText rainbowText = new RainbowText("");
	
	public String valueRaw;
	
	public StringCustomizationItem(PropertiesSection item) {
		super(item);

		if ((this.action != null) && this.action.equalsIgnoreCase("addtext")) {
			
			this.valueRaw = item.getEntryValue("value");
			this.updateValue();

			String tc = item.getEntryValue("textcolor");
			if ((tc != null)) {
				this.textColorHex = tc;
				this.textColor = RenderUtils.getColorFromHexString(tc);
			}
			
			String sh = item.getEntryValue("shadow");
			if ((sh != null)) {
				if (sh.equalsIgnoreCase("true")) {
					this.shadow = true;
				}
			}
			
			String sc = item.getEntryValue("scale");
			if ((sc != null) && MathUtils.isFloat(sc)) {
				this.scale = Float.parseFloat(sc);
			}
			
			String rainbow = item.getEntryValue("rainbowmode");
			if ((rainbow != null) && rainbow.equalsIgnoreCase("true")) {
				this.rainbowMode = true;
			}
			String rainbowSpeed = item.getEntryValue("rainbowspeed");
			if ((rainbowSpeed != null) && MathUtils.isFloat(rainbowSpeed)) {
				this.rainbowText.setSpeed(Float.parseFloat(rainbowSpeed));
			}
			this.rainbowStartColorHex1 = item.getEntryValue("rainbowstartcolor1");
			this.rainbowStartColorHex2 = item.getEntryValue("rainbowstartcolor2");
			this.rainbowEndColorHex1 = item.getEntryValue("rainbowendcolor1");
			this.rainbowEndColorHex2 = item.getEntryValue("rainbowendcolor2");
			
			this.updateRainbowColors();
			
		}
	}
	
	protected void updateValue() {
		
		if (this.valueRaw != null) {
			if (!isEditorActive()) {
				this.value = PlaceholderTextValueHelper.convertFromRaw(this.valueRaw);
			} else {
				this.value = StringUtils.convertFormatCodes(this.valueRaw, "&", "§");
			}
		}
		
		this.width = (int) (SimpleTextRenderer.getStringWidth(this.value) * this.scale);
		this.height = (int) (10 * this.scale);
		
		if (this.rainbowMode) {
			this.rainbowText.setShadow(this.shadow);
			this.rainbowText.setText(this.value);
		}
		
	}

	@Override
	public void render(MatrixStack matrix) {
		if (!this.shouldRender()) {
			return;
		}
		
		this.updateValue();
		
		int x = this.getPosX();
		int y = this.getPosY();

		RenderSystem.enableBlend();
		if (this.rainbowMode) {
			this.rainbowText.render(matrix, x, y);
		} else {
			if (this.shadow) {
				SimpleTextRenderer.drawStringWithShadow(matrix, this.value, x, y, this.textColor.getRGB(), this.opacity, this.scale);
			} else {
				SimpleTextRenderer.drawString(matrix, this.value, x, y, this.textColor.getRGB(), this.opacity, this.scale);
			}
		}
		RenderSystem.disableBlend();

	}
	
	public boolean allRainbowColorsSet() {
		return ((this.rainbowStartColorHex1 != null) && (this.rainbowStartColorHex2 != null) && (this.rainbowEndColorHex1 != null) && (this.rainbowEndColorHex2 != null));
	}
	
	public void updateRainbowColors() {
		if (this.allRainbowColorsSet()) {
			Color start1 = RenderUtils.getColorFromHexString(this.rainbowStartColorHex1);
			Color start2 = RenderUtils.getColorFromHexString(this.rainbowStartColorHex2);
			Color end1 = RenderUtils.getColorFromHexString(this.rainbowEndColorHex1);
			Color end2 = RenderUtils.getColorFromHexString(this.rainbowEndColorHex2);
			if ((start1 != null) && (start2 != null) && (end1 != null) && (end2 != null)) {
				this.rainbowText.setStartColorRange(start1, start2);
				this.rainbowText.setEndColorRange(end1, end2);
			}
		}
	}

}
