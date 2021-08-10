//TODO fixen
package de.keksuccino.drippyloadingscreen.customization.rendering;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.keksuccino.drippyloadingscreen.utils.TextComponentUtils;
import de.keksuccino.konkrete.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;

public class RainbowText extends AbstractGui {
	
	public static final Color MISSING_COLOR = Color.BLACK;
	
	protected String text;
	protected float speed = 1.0F;
	protected boolean shadow = true;
	
	protected int colorRange = 133;
	protected List<Color> startColorRange = new ArrayList<Color>();
	protected List<Color> endColorRange = new ArrayList<Color>();
	
	public Color startColor1 = new Color(255, 0, 0);
	public Color startColor2 = new Color(234, 0, 255);
	public Color endColor1 = new Color(255, 230, 0);
	public Color endColor2 = new Color(255, 0, 162);
	
	protected int currentColor = 0;
	protected boolean reversed = false;
	protected long lastMsTime = 0;
	
	public RainbowText(@Nonnull String text) {
		this.text = text;
		this.setStartColorRange(this.startColor1, this.startColor2);
		this.setEndColorRange(this.endColor1, this.endColor2);
	}
	
	public void render(MatrixStack matrix, int x, int y) {
		if (this.text != null) {

			FontRenderer font = Minecraft.getInstance().fontRenderer;
			Color start = MISSING_COLOR;
			Color end = MISSING_COLOR;
			if (this.currentColor <= (this.startColorRange.size()-1)) {
				start = this.startColorRange.get(this.currentColor);
			}
			if (this.currentColor <= (this.endColorRange.size()-1)) {
				end = this.endColorRange.get(this.currentColor);
			}
			
			ITextComponent c = TextComponentUtils.getWithGradient(start, this.text, end);
			if (this.shadow) {
				font.drawTextWithShadow(matrix, c.func_241878_f(), x, y, -1);
			} else {
				font.func_238422_b_(matrix, c.func_241878_f(), x, y, -1);
			}
			
			long ms = System.currentTimeMillis();
			if ((this.lastMsTime + (30 / this.speed)) <= ms) {
				
				if (this.reversed) {
					this.currentColor--;
				} else {
					this.currentColor++;
				}
				
				if (this.currentColor <= 0) {
					this.reversed = false;
					this.currentColor = 0;
				}
				if (this.currentColor >= this.startColorRange.size()) {
					this.reversed = true;
					this.currentColor = this.startColorRange.size()-1;
				}
				
				this.lastMsTime = ms;
			}
			
		}
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return this.text;
	}
	
	public void setSpeed(float speed) {
		this.speed = speed;
		this.setStartColorRange(this.startColor1, this.startColor2);
		this.setEndColorRange(this.endColor1, this.endColor2);
	}
	
	public float getSpeed() {
		return this.speed;
	}
	
	public void setShadow(boolean shadow) {
		this.shadow = shadow;
	}
	
	/**
	 * Sets the color range from the first to the last <b>start</b> color.<br>
	 * Start colors are the beginning of the text gradient.
	 */
	public void setStartColorRange(Color start, Color end) {
		this.startColor1 = start;
		this.startColor2 = end;
		this.startColorRange = getGradientColorRange(start, end, (int) (this.colorRange / this.speed));
		this.currentColor = 0;
	}
	
	/**
	 * Sets the color range from the first to the last <b>end</b> color.<br>
	 * End colors are the ending of the text gradient.
	 */
	public void setEndColorRange(Color start, Color end) {
		this.endColor1 = start;
		this.endColor2 = end;
		this.endColorRange = getGradientColorRange(start, end, (int) (this.colorRange / this.speed));
		this.currentColor = 0;
	}
	
	public Color getFirstStartColor() {
		return this.startColor1;
	}
	
	public Color getFirstEndColor() {
		return this.endColor1;
	}
	
	public Color getSecondStartColor() {
		return this.startColor2;
	}
	
	public Color getSecondEndColor() {
		return this.endColor2;
	}
	
	protected static List<Color> getGradientColorRange(Color start, Color end, int range) {

		List<Color> l = new ArrayList<Color>();
		
		int i = 0;
		while (i < range) {

			int red = (int)(start.getRed() + ((float)(end.getRed() - start.getRed())) / (range - 1) * i);
			int green = (int)(start.getGreen() + ((float)(end.getGreen() - start.getGreen())) / (range - 1) * i);
			int blue = (int)(start.getBlue() + ((float)(end.getBlue() - start.getBlue())) / (range - 1) * i);

			//TODO change to direct color
			l.add(RenderUtils.getColorFromHexString(TextComponentUtils.toHexString(red, green, blue)));
			
			i++;

		}

		return l;

	}

}
