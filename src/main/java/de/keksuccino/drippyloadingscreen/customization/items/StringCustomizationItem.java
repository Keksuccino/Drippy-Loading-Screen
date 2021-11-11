package de.keksuccino.drippyloadingscreen.customization.items;

import java.awt.Color;

import net.minecraft.client.util.math.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import de.keksuccino.drippyloadingscreen.customization.placeholdervalues.PlaceholderTextValueHelper;
import de.keksuccino.drippyloadingscreen.customization.rendering.SimpleTextRenderer;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.rendering.RenderUtils;

public class StringCustomizationItem extends CustomizationItemBase {
	
	public float scale = 1.0F;
	public boolean shadow = false;
	public Alignment alignment = Alignment.LEFT;

	public Color textColor = Color.WHITE;
	public String textColorHex = "#ffffff";
	
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
		this.height = (int) (SimpleTextRenderer.getStringHeight() * this.scale);
		
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
		if (this.shadow) {
			SimpleTextRenderer.drawStringWithShadow(matrix, this.value, x, y, this.textColor.getRGB(), this.opacity, this.scale);
		} else {
			SimpleTextRenderer.drawString(matrix, this.value, x, y, this.textColor.getRGB(), this.opacity, this.scale);
		}
		RenderSystem.disableBlend();

	}

}
