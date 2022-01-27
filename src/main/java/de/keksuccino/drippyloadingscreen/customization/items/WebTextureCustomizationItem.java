//TODO übernehmen
package de.keksuccino.drippyloadingscreen.customization.items;

import java.net.HttpURLConnection;
import java.net.URL;

import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.placeholdervalues.PlaceholderTextValueHelper;
import de.keksuccino.konkrete.annotations.OptifineFix;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.rendering.RenderUtils;
import de.keksuccino.konkrete.resources.TextureHandler;
import de.keksuccino.konkrete.resources.WebTextureResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.TextureManager;

public class WebTextureCustomizationItem extends CustomizationItemBase {

	public volatile WebTextureResourceLocation texture;
	public String rawURL = "";
	public volatile boolean ready = false;

	@OptifineFix
	//FIX: Web textures need to be loaded in the main thread if OF is installed
	public WebTextureCustomizationItem(PropertiesSection item) {
		super(item);

		if ((this.action != null) && this.action.equalsIgnoreCase("addwebtexture")) {
			this.value = item.getEntryValue("url");
			if (this.value != null) {
				this.rawURL = this.value;
				this.value = PlaceholderTextValueHelper.convertFromRaw(this.value);

				if ((this.width <= 0) && (this.height <= 0)) {
					this.width = 100;
				}

				new Thread(() -> {
					try {

						if (isValidUrl(this.value)) {

							this.texture = TextureHandler.getWebResource(this.value, false);
							Minecraft.getInstance().execute(() -> {
								try {
									texture.loadTexture();
								} catch (Exception e) {
									e.printStackTrace();
								}
							});

							//Wait for the texture to load
							long startTime = System.currentTimeMillis();
							while (true) {
								long currentTime = System.currentTimeMillis();
								if ((startTime+15000) < currentTime) {
									break;
								}
								if (texture.isReady()) {
									if (texture.getResourceLocation() != null) {
										break;
									}
								}
								try {
									Thread.sleep(100);
								} catch (Exception e) {}
							}

							if ((this.texture != null) && (texture.getResourceLocation() == null)) {
								this.texture = null;
								DrippyLoadingScreen.LOGGER.error("[DRIPPY LOADING SCREEN] Web texture loaded but resource location was still null! Unable to use web texture!");
							}

							if ((this.texture == null) || !this.texture.isReady()) {
								if (this.width <= 0) {
									this.width = 100;
								}
								if (this.height <= 0) {
									this.width = 100;
								}
								this.ready = true;
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
						}

						this.ready = true;

					} catch (Exception e) {
						e.printStackTrace();
					}
				}).start();

			}
		}

	}

	@Override
	public void render(PoseStack matrix) {
		if (this.shouldRender()) {

			int x = this.getPosX();
			int y = this.getPosY();

			if (this.isTextureReady()) {
				RenderUtils.bindTexture(this.texture.getResourceLocation());
			} else if (isEditorActive()) {
				RenderUtils.bindTexture(TextureManager.INTENTIONAL_MISSING_TEXTURE);
			}

			if (this.isTextureReady() || isEditorActive()) {
				RenderSystem.enableBlend();
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.opacity);
				GuiComponent.blit(matrix, x, y, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
				RenderSystem.disableBlend();
			}

			if (!this.ready && isEditorActive()) {
				drawCenteredString(matrix, Minecraft.getInstance().font, "§lLOADING TEXTURE..", this.getPosX() + (this.width / 2), this.getPosY() + (this.height / 2) - (Minecraft.getInstance().font.lineHeight / 2), -1);
			}

		}
	}

	public boolean isTextureReady() {
		return ((this.texture != null) && (this.texture.isReady()) && (this.texture.getResourceLocation() != null) && this.ready);
	}

	@Override
	public boolean shouldRender() {
		if ((this.width < 0) || (this.height < 0)) {
			return false;
		}
		return super.shouldRender();
	}

	public static boolean isValidUrl(String url) {
		if ((url != null) && (url.startsWith("http://") || url.startsWith("https://"))) {
			try {
				URL u = new URL(url);
				HttpURLConnection c = (HttpURLConnection)u.openConnection();
				c.addRequestProperty("User-Agent", "Mozilla/4.0");
				c.setRequestMethod("HEAD");
				int r = c.getResponseCode();
				if (r == 200) {
					return true;
				}
			} catch (Exception e1) {
				try {
					URL u = new URL(url);
					HttpURLConnection c = (HttpURLConnection)u.openConnection();
					c.addRequestProperty("User-Agent", "Mozilla/4.0");
					int r = c.getResponseCode();
					if (r == 200) {
						return true;
					}
				} catch (Exception e2) {}
			}
			return false;
		}
		return false;
	}

}
