package de.keksuccino.drippyloadingscreen.customization.items;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import de.keksuccino.drippyloadingscreen.customization.placeholdervalues.PlaceholderTextValueHelper;
import de.keksuccino.drippyloadingscreen.customization.rendering.SimpleTextRenderer;
import de.keksuccino.drippyloadingscreen.events.CustomizationSystemReloadedEvent;
import de.keksuccino.konkrete.file.FileUtils;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SplashTextCustomizationItem extends CustomizationItemBase {

	protected static Map<String, String> splashCache = new HashMap<String, String>();
	protected static boolean init = false;
	
	public float scale = 1.0F;
	public boolean shadow = true;
	public boolean bounce = true;
	public float rotation = 20.0F;
	public Color basecolor = new Color(255, 255, 0);
	public String basecolorString = "#ffff00";
	public boolean refreshOnMenuReload = false;
	public File splashfile;
	public String text = null;
	
	protected float basescale = 1.8F;
	
	protected static boolean isNewMenu = false;
	protected boolean isNewMenuThis = false;
	protected static Screen lastScreen = null;
	
	public SplashTextCustomizationItem(PropertiesSection item) {
		super(item);
		
		if (!init) {
			MinecraftForge.EVENT_BUS.register(SplashTextCustomizationItem.class);
			init = true;
		}
		
		if ((this.action != null) && this.action.equalsIgnoreCase("addsplash")) {

			String filepath = fixBackslashPath(item.getEntryValue("splashfilepath"));
			if (filepath != null) {
				this.splashfile = new File(filepath);
				if (!this.splashfile.exists() || !this.splashfile.getPath().toLowerCase().endsWith(".txt")) {
					this.splashfile = null;
				}
			}
			
			this.text = item.getEntryValue("text");
			
			String ro = item.getEntryValue("rotation");
			if ((ro != null) && MathUtils.isFloat(ro)) {
				this.rotation = Float.parseFloat(ro);
			}
			
			String re = item.getEntryValue("refresh");
			if ((re != null) && re.equalsIgnoreCase("true")) {
				this.refreshOnMenuReload = true;
			}
			
			String co = item.getEntryValue("basecolor");
			if (co != null) {
				Color c = RenderUtils.getColorFromHexString(co);
				if (c != null) {
					this.basecolor = c;
					this.basecolorString = co;
				}
			}
			
			String sh = item.getEntryValue("shadow");
			if ((sh != null)) {
				if (sh.equalsIgnoreCase("false")) {
					this.shadow = false;
				}
			}
			
			String sc = item.getEntryValue("scale");
			if ((sc != null) && MathUtils.isFloat(sc)) {
				this.scale = Float.parseFloat(sc);
			}
			
			String b = item.getEntryValue("bouncing");
			if ((b != null) && b.equalsIgnoreCase("false")) {
				this.bounce = false;
			}
			
			this.value = "splash text";
			
			this.width = (int) (30 * basescale * this.scale);
			this.height = (int) (10 * basescale * this.scale);
			
		}
	}

	@Override
	public void render(PoseStack matrix) {
		
		if (this.isNewMenuThis) {
			isNewMenu = false;
		}
		this.isNewMenuThis = isNewMenu;
		
		this.width = (int) (30 * basescale * this.scale);
		this.height = (int) (10 * basescale * this.scale);
		
		if (this.shouldRender()) {
			
			this.renderSplash(matrix);
			
		}
		
	}

	protected void renderSplash(PoseStack matrix) {

		String splash = null;

		if ((this.splashfile != null) && (this.text == null)) {

			if (isNewMenu && this.refreshOnMenuReload) {
				splashCache.remove(this.getActionId());
			}

			if (!splashCache.containsKey(this.getActionId())) {
				List<String> l = FileUtils.getFileLines(this.splashfile);
				if (!l.isEmpty()) {
					int i = MathUtils.getRandomNumberInRange(0, l.size()-1);
					splashCache.put(this.getActionId(), l.get(i));
				}
			}

			if (splashCache.containsKey(this.getActionId())) {
				splash = splashCache.get(this.getActionId());
			}

		}

		if (this.text != null) {
			splash = this.text;
		}

		if (splash != null) {

			if (this.value != null) {
				if (!isEditorActive()) {
					splash = PlaceholderTextValueHelper.convertFromRaw(splash);
				} else {
					splash = StringUtils.convertFormatCodes(splash, "&", "§");
				}
			}

			this.value = splash;

			float f = basescale;
			if (this.bounce) {
				f = f - Mth.abs(Mth.sin((float) (System.currentTimeMillis() % 1000L) / 1000.0F * ((float) Math.PI * 2F)) * 0.1F);
			}
			f = f * 100.0F / (float) (SimpleTextRenderer.getStringWidth(splash) + 32);

			RenderSystem.enableBlend();

			matrix.pushPose();
			matrix.scale(this.scale, this.scale, this.scale);

			matrix.pushPose();
			matrix.translate(((this.getPosX() + (this.width / 2)) / this.scale), this.getPosY() / this.scale, 0.0F);
			matrix.mulPose(Vector3f.ZP.rotationDegrees(this.rotation));
			matrix.scale(f, f, f);

			int alpha = this.basecolor.getAlpha();
			int i = Mth.ceil(this.opacity * 255.0F);
			if (i < alpha) {
				alpha = i;
			}
			Color c = new Color(this.basecolor.getRed(), this.basecolor.getGreen(), this.basecolor.getBlue(), alpha);

			if (this.shadow) {
				SimpleTextRenderer.drawStringWithShadow(matrix, splash, -(SimpleTextRenderer.getStringWidth(splash) / 2), 0, c.getRGB(), 1.0F, this.opacity);
			} else {
				SimpleTextRenderer.drawString(matrix, splash, -(SimpleTextRenderer.getStringWidth(splash) / 2), 0, c.getRGB(), 1.0F, this.opacity);
			}

			matrix.popPose();
			matrix.popPose();

		}

	}
	
	@SubscribeEvent
	public static void onInitScreenPre(ScreenEvent.InitScreenEvent.Pre e) {
		Screen s = Minecraft.getInstance().screen;
		if (s != null) {
			if ((lastScreen == null) || !lastScreen.getClass().getName().equals(s.getClass().getName())) {
				isNewMenu = true;
			}
		}
		lastScreen = s;
	}
	
	@SubscribeEvent
	public static void onMenuReloaded(CustomizationSystemReloadedEvent e) {
		splashCache.clear();
	}

}
