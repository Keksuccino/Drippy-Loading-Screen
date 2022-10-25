package de.keksuccino.drippyloadingscreen.customization.items;

import java.io.File;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.rendering.animation.ExternalGifAnimationRenderer;
import de.keksuccino.konkrete.resources.ExternalTextureResourceLocation;
import de.keksuccino.konkrete.resources.TextureHandler;
import net.minecraft.client.Minecraft;

public class TextureCustomizationItem extends CustomizationItemBase {
	
	public ExternalTextureResourceLocation texture;
	public ExternalGifAnimationRenderer gif;
	
	public TextureCustomizationItem(PropertiesSection item) {
		super(item);
		
		if ((this.action != null) && this.action.equalsIgnoreCase("addtexture")) {
			this.value = item.getEntryValue("path");
			if (this.value != null) {
				this.value = fixBackslashPath(this.value);
				
				File f = new File(this.value);
				if (f.exists() && f.isFile() && (f.getName().endsWith(".png") || f.getName().endsWith(".jpg") || f.getName().endsWith(".jpeg") || f.getName().endsWith(".gif"))) {
					try {
						int w = 0;
					    int h = 0;
					    double ratio;
					    
						if (f.getName().endsWith(".gif")) {
							this.gif = TextureHandler.getGifResource(this.value);
							if (this.gif != null) {
								w = this.gif.getWidth();
								h = this.gif.getHeight();
							}
						} else {
							this.texture = TextureHandler.getResource(this.value);
							if (this.texture != null) {
								w = this.texture.getWidth();
							    h = this.texture.getHeight();
							}
						}
						
						ratio = (double) w / (double) h;
					    
					    //Calculate missing width
					    if ((this.width < 0) && (this.height >= 0)) {
					    	this.width = (int)(this.height * ratio);
					    }
					    //Calculate missing height
					    if ((this.height < 0) && (this.width >= 0)) {
					    	this.height = (int)(this.width / ratio);
					    }
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void render(MatrixStack matrix) {
		if (this.shouldRender()) {
			
			int x = this.getPosX();
			int y = this.getPosY();
			
			if (this.gif != null) {
				int w = this.gif.getWidth();
				int h = this.gif.getHeight();
				int x2 = this.gif.getPosX();
				int y2 = this.gif.getPosY();
				
				RenderSystem.enableBlend();
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.opacity);
				
				this.gif.setPosX(x);
				this.gif.setPosY(y);
				this.gif.setWidth(this.width);
				this.gif.setHeight(this.height);
				
				this.gif.render(matrix);
				
				this.gif.setPosX(x2);
				this.gif.setPosY(y2);
				this.gif.setWidth(w);
				this.gif.setHeight(h);
				
				RenderSystem.disableBlend();
				
			} else if (this.texture != null) {
				
				Minecraft.getInstance().getTextureManager().bind(this.texture.getResourceLocation());
				RenderSystem.enableBlend();
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.opacity);
				blit(matrix, x, y, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
				RenderSystem.disableBlend();
				
			}
			
		}
	}
	
	@Override
	public boolean shouldRender() {
		if ((this.texture == null) && (this.gif == null)) {
			return false;
		}
		if ((this.width < 0) || (this.height < 0)) {
			return false;
		}
		return super.shouldRender();
	}

}
