package de.keksuccino.drippyloadingscreen.customization.items.custombars;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.items.CustomizationItemBase;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.rendering.RenderUtils;
import de.keksuccino.konkrete.resources.ExternalTextureResourceLocation;
import de.keksuccino.konkrete.resources.TextureHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.File;

public abstract class CustomBarCustomizationItemBase extends CustomizationItemBase {
	
	public BarDirection direction = BarDirection.RIGHT;
	
	public Color barColor = new Color(0, 0, 0);
	public Color backgroundColor = new Color(0, 0, 0, 50);
	
	public ResourceLocation barTexture = null;
	public ResourceLocation backgroundTexture = null;
	public ResourceLocation barEndTexture = null;
	public int barEndTextureWidth = 10;
	public int barEndTextureHeight = 10;
	
	public String barColorHex = null;
	public String backgroundColorHex = null;
	public String barTexturePath = null;
	public String backgroundTexturePath = null;
	public String barEndTexturePath = null;

	protected int currentPercentWidthHeight = 0;
	
	public CustomBarCustomizationItemBase(PropertiesSection item) {
		super(item);
		this.init(item);
	}
	
	public void init(PropertiesSection item) {
		
		this.barColorHex = item.getEntryValue("barcolor");
		this.backgroundColorHex = item.getEntryValue("backgroundcolor");
		this.barTexturePath = fixBackslashPath(item.getEntryValue("bartexture"));
		this.backgroundTexturePath = fixBackslashPath(item.getEntryValue("backgroundtexture"));
		this.barEndTexturePath = fixBackslashPath(item.getEntryValue("barendtexture"));

		String barEndWidthString = item.getEntryValue("barendtexturewidth");
		if ((barEndWidthString != null) && MathUtils.isInteger(barEndWidthString)) {
			this.barEndTextureWidth = Integer.parseInt(barEndWidthString);
		}

		String barEndHeightString = item.getEntryValue("barendtextureheight");
		if ((barEndHeightString != null) && MathUtils.isInteger(barEndHeightString)) {
			this.barEndTextureHeight = Integer.parseInt(barEndHeightString);
		}
		
		String barDirec = item.getEntryValue("direction");
		if (barDirec != null) {
			this.direction = BarDirection.byName(barDirec);
		}
		
		this.updateItem();
		
	}

	protected abstract void renderBar(MatrixStack matrix);

	protected abstract void renderBarBackground(MatrixStack matrix);
	
	public void updateItem() {
		
		if (this.barColorHex != null) {
			Color c = RenderUtils.getColorFromHexString(this.barColorHex);
			if (c != null) {
				this.barColor = c;
			}
		}

		if (this.backgroundColorHex != null) {
			Color c = RenderUtils.getColorFromHexString(this.backgroundColorHex);
			if (c != null) {
				this.backgroundColor = c;
			}
		}

		if (this.barTexturePath != null) {
			File f = new File(this.barTexturePath);
			if (f.exists() && f.isFile() && (f.getPath().toLowerCase().endsWith(".jpg") || f.getPath().toLowerCase().endsWith(".jpeg") || f.getPath().toLowerCase().endsWith(".png"))) {
				ExternalTextureResourceLocation er = TextureHandler.getResource(this.barTexturePath);
				if (er != null) {
					er.loadTexture();
					this.barTexture = er.getResourceLocation();
				}
			}
		} else {
			this.barTexture = null;
		}

		if (this.backgroundTexturePath != null) {
			File f = new File(this.backgroundTexturePath);
			if (f.exists() && f.isFile() && (f.getPath().toLowerCase().endsWith(".jpg") || f.getPath().toLowerCase().endsWith(".jpeg") || f.getPath().toLowerCase().endsWith(".png"))) {
				ExternalTextureResourceLocation er = TextureHandler.getResource(this.backgroundTexturePath);
				if (er != null) {
					er.loadTexture();
					this.backgroundTexture = er.getResourceLocation();
				}
			}
		} else {
			this.backgroundTexture = null;
		}

		if (this.barEndTexturePath != null) {
			File f = new File(this.barEndTexturePath);
			if (f.exists() && f.isFile() && (f.getPath().toLowerCase().endsWith(".jpg") || f.getPath().toLowerCase().endsWith(".jpeg") || f.getPath().toLowerCase().endsWith(".png"))) {
				ExternalTextureResourceLocation er = TextureHandler.getResource(this.barEndTexturePath);
				if (er != null) {
					er.loadTexture();
					this.barEndTexture = er.getResourceLocation();
				}
			}
		} else {
			this.barEndTexture = null;
		}
		
	}
	
	protected boolean isEditor() {
		return (Minecraft.getInstance().currentScreen instanceof LayoutEditorScreen);
	}
	
	public static enum BarDirection {
		LEFT("left"),
		RIGHT("right"),
		UP("up"),
		DOWN("down");
		
		private String name;
		
		BarDirection(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
		
		public static BarDirection byName(String name) {
			for (BarDirection d : BarDirection.values()) {
				if (d.name.equals(name)) {
					return d;
				}
			}
			return BarDirection.LEFT;
		}
	}

}
