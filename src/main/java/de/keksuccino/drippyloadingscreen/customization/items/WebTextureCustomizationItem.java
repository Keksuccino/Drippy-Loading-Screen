package de.keksuccino.drippyloadingscreen.customization.items;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import de.keksuccino.drippyloadingscreen.customization.placeholdervalues.PlaceholderTextValueHelper;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.resources.TextureHandler;
import de.keksuccino.konkrete.resources.WebTextureResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.renderer.texture.TextureManager;

public class WebTextureCustomizationItem extends CustomizationItemBase {
	
	public WebTextureResourceLocation texture;
	public String rawURL = "";
	
	public WebTextureCustomizationItem(PropertiesSection item) {
		super(item);
		
		if ((this.action != null) && this.action.equalsIgnoreCase("addwebtexture")) {
			this.value = item.getEntryValue("url");
			if (this.value != null) {
				this.rawURL = this.value;
				this.value = PlaceholderTextValueHelper.convertFromRaw(this.value);
				try {
					this.texture = TextureHandler.getWebResource(this.value);

					if ((this.texture == null) || !this.texture.isReady()) {
						this.width = 100;
						this.height = 100;
						return;
					}
					
					int w = this.texture.getWidth();
					int h = this.texture.getHeight();
					double ratio = (double) w / (double) h;

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

	@Override
	public void render(MatrixStack matrix) {
		if (this.shouldRender()) {
			
			int x = this.getPosX();
			int y = this.getPosY();

			if (this.texture != null) {
				Minecraft.getInstance().getTextureManager().bindTexture(this.texture.getResourceLocation());
			} else {
				Minecraft.getInstance().getTextureManager().bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
			}
			
			RenderSystem.enableBlend();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.opacity);
			IngameGui.blit(matrix, x, y, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
			RenderSystem.disableBlend();
		}
	}
	
	@Override
	public boolean shouldRender() {
		if ((this.width < 0) || (this.height < 0)) {
			return false;
		}
		return super.shouldRender();
	}

}
