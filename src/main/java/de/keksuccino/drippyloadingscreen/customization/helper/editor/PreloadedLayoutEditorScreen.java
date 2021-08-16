package de.keksuccino.drippyloadingscreen.customization.helper.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.keksuccino.drippyloadingscreen.api.item.CustomizationItem;
import de.keksuccino.drippyloadingscreen.api.item.CustomizationItemContainer;
import de.keksuccino.drippyloadingscreen.api.item.CustomizationItemLayoutElement;
import de.keksuccino.drippyloadingscreen.api.item.CustomizationItemRegistry;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.*;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.custombars.LayoutCustomProgressBar;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.string.LayoutString;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.string.LayoutWebString;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.vanilla.LogoLayoutSplashElement;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.vanilla.ProgressBarLayoutSplashElement;
import de.keksuccino.drippyloadingscreen.customization.items.*;
import de.keksuccino.drippyloadingscreen.customization.items.ShapeCustomizationItem.Shape;
import de.keksuccino.drippyloadingscreen.customization.items.custombars.CustomProgressBarCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.items.vanilla.LogoSplashCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.items.vanilla.ProgressBarSplashCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.rendering.slideshow.SlideshowHandler;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.properties.PropertiesSet;
import de.keksuccino.konkrete.resources.ExternalTextureResourceLocation;
import de.keksuccino.konkrete.resources.TextureHandler;

public class PreloadedLayoutEditorScreen extends LayoutEditorScreen {

	public String single;
	
	public PreloadedLayoutEditorScreen(PropertiesSet properties) {
		super();
		
		List<LayoutElement> con = new ArrayList<LayoutElement>();
		List<LayoutElement> vanillaCon = new ArrayList<LayoutElement>();

		List<PropertiesSection> l = properties.getPropertiesOfType("customization-meta");
		if (!l.isEmpty()) {
			PropertiesSection meta = l.get(0);
			
			this.requiredmods = meta.getEntryValue("requiredmods");
			this.minimumDL = meta.getEntryValue("minimumdlversion");
			this.maximumDL = meta.getEntryValue("maximumdlversion");
			this.minimumMC = meta.getEntryValue("minimummcversion");
			this.maximumMC = meta.getEntryValue("maximummcversion");
			
			String order = meta.getEntryValue("renderorder");
			if ((order != null) && order.equalsIgnoreCase("background")) {
				this.renderorder = "background";
			}

			this.splashLayer.customBackgroundHex = meta.getEntryValue("backgroundcolor");

			String backgroundImageString = meta.getEntryValue("backgroundimage");
			if (backgroundImageString != null) {
				File f = new File(backgroundImageString);
				if (f.isFile() && (f.getPath().toLowerCase().endsWith(".jpg") || f.getPath().toLowerCase().endsWith(".jpeg") || f.getPath().toLowerCase().endsWith(".png"))) {
					this.splashLayer.backgroundImagePath = backgroundImageString;
					ExternalTextureResourceLocation tex = TextureHandler.getResource(backgroundImageString);
					tex.loadTexture();
					this.splashLayer.backgroundImage = tex.getResourceLocation();
				}
			}

			String biggerthanwidth = meta.getEntryValue("biggerthanwidth");
			if (biggerthanwidth != null) {
				biggerthanwidth = biggerthanwidth.replace(" ", "");
				if (MathUtils.isInteger(biggerthanwidth)) {
					int i = Integer.parseInt(biggerthanwidth);
					this.biggerThanWidth = i;
				}
			}

			String biggerthanheight = meta.getEntryValue("biggerthanheight");
			if (biggerthanheight != null) {
				biggerthanheight = biggerthanheight.replace(" ", "");
				if (MathUtils.isInteger(biggerthanheight)) {
					int i = Integer.parseInt(biggerthanheight);
					this.biggerThanHeight = i;
				}
			}

			String smallerthanwidth = meta.getEntryValue("smallerthanwidth");
			if (smallerthanwidth != null) {
				smallerthanwidth = smallerthanwidth.replace(" ", "");
				if (MathUtils.isInteger(smallerthanwidth)) {
					int i = Integer.parseInt(smallerthanwidth);
					this.smallerThanWidth = i;
				}
			}

			String smallerthanheight = meta.getEntryValue("smallerthanheight");
			if (smallerthanheight != null) {
				smallerthanheight = smallerthanheight.replace(" ", "");
				if (MathUtils.isInteger(smallerthanheight)) {
					int i = Integer.parseInt(smallerthanheight);
					this.smallerThanHeight = i;
				}
			}
			
			this.single = meta.getEntryValue("path");
		}

		boolean logoSet = false;
		boolean progressBarSet = false;

		for (PropertiesSection sec : properties.getPropertiesOfType("customization")) {
			String action = sec.getEntryValue("action");
			if (action != null) {
				
				/** ########################### VANILLA ELEMENT HANDLING ########################### **/

				/** LOGO **/
				if (action.equalsIgnoreCase("editlogo")) {
					this.logoLayoutSplashElement = new LogoLayoutSplashElement(new LogoSplashCustomizationItem(this.splashLayer.logoSplashElement, sec, false), this);
					vanillaCon.add(this.logoLayoutSplashElement);
					logoSet = true;
				}

				/** PROGRESS BAR **/
				if (action.equalsIgnoreCase("editprogressbar")) {
					this.progressBarLayoutSplashElement = new ProgressBarLayoutSplashElement(new ProgressBarSplashCustomizationItem(this.splashLayer.progressBarSplashElement, sec, false), this);
					vanillaCon.add(this.progressBarLayoutSplashElement);
					progressBarSet = true;
				}
				
				/** ########################### ITEM HANDLING ########################### **/
				
				if (action.equalsIgnoreCase("addtext")) {
					con.add(new LayoutString(new StringCustomizationItem(sec), this));
				}

				if (action.equalsIgnoreCase("addwebtext")) {
					con.add(new LayoutWebString(new WebStringCustomizationItem(sec), this));
				}

				if (action.equalsIgnoreCase("addtexture")) {
					LayoutTexture o = new LayoutTexture(new TextureCustomizationItem(sec), this);
					int i = isObjectStretched(sec);
					if (i == 3) {
						o.setStretchedX(true, false);
						o.setStretchedY(true, false);
					}
					if (i == 2) {
						o.setStretchedY(true, false);
					}
					if (i == 1) {
						o.setStretchedX(true, false);
					}
					con.add(o);
				}

				if (action.equalsIgnoreCase("addwebtexture")) {
					LayoutWebTexture o = new LayoutWebTexture(new WebTextureCustomizationItem(sec), this);
					int i = isObjectStretched(sec);
					if (i == 3) {
						o.setStretchedX(true, false);
						o.setStretchedY(true, false);
					}
					if (i == 2) {
						o.setStretchedY(true, false);
					}
					if (i == 1) {
						o.setStretchedX(true, false);
					}
					con.add(o);
				}

				if (action.equalsIgnoreCase("addslideshow")) {
					String name = sec.getEntryValue("name");
					if (name != null) {
						if (SlideshowHandler.slideshowExists(name)) {
							LayoutSlideshow ls = new LayoutSlideshow(new SlideshowCustomizationItem(sec), this);
							int i = isObjectStretched(sec);
							if (i == 3) {
								ls.setStretchedX(true, false);
								ls.setStretchedY(true, false);
							}
							if (i == 2) {
								ls.setStretchedY(true, false);
							}
							if (i == 1) {
								ls.setStretchedX(true, false);
							}
							con.add(ls);
						}
					}
				}

				if (action.equalsIgnoreCase("addshape")) {
					String shape = sec.getEntryValue("shape");
					if (shape != null) {
						Shape sh = Shape.byName(shape);
						if (sh != null) {
							LayoutShape ls = new LayoutShape(new ShapeCustomizationItem(sec), this);
							int i = isObjectStretched(sec);
							if (i == 3) {
								ls.setStretchedX(true, false);
								ls.setStretchedY(true, false);
							}
							if (i == 2) {
								ls.setStretchedY(true, false);
							}
							if (i == 1) {
								ls.setStretchedX(true, false);
							}
							con.add(ls);
						}
					}
				}

				if (action.equalsIgnoreCase("addsplash")) {
					con.add(new LayoutSplashText(new SplashTextCustomizationItem(sec), this));
				}

				if (action.equalsIgnoreCase("addcustomprogressbar")) {
					LayoutCustomProgressBar o = new LayoutCustomProgressBar(new CustomProgressBarCustomizationItem(sec), this);
					int i = isObjectStretched(sec);
					if (i == 3) {
						o.setStretchedX(true, false);
						o.setStretchedY(true, false);
					}
					if (i == 2) {
						o.setStretchedY(true, false);
					}
					if (i == 1) {
						o.setStretchedX(true, false);
					}
					con.add(o);
				}
				
				/** ########################### CUSTOM ITEM HANDLING ########################### **/
				
				if (action.startsWith("add_")) {
					String id = action.split("[_]", 2)[1];
					CustomizationItemContainer c = CustomizationItemRegistry.getInstance().getElement(id);
					
					if (c != null) {
						
						CustomizationItem i = c.constructWithProperties(sec);
						con.add(new CustomizationItemLayoutElement(c, i, this));
						
					}
				}

			}
		}
		
		PropertiesSection dummySec = new PropertiesSection("customization");
		if (!logoSet) {
			this.logoLayoutSplashElement = new LogoLayoutSplashElement(new LogoSplashCustomizationItem(this.splashLayer.logoSplashElement, dummySec, false), this);
			vanillaCon.add(this.logoLayoutSplashElement);
		}
		if (!progressBarSet) {
			this.progressBarLayoutSplashElement = new ProgressBarLayoutSplashElement(new ProgressBarSplashCustomizationItem(this.splashLayer.progressBarSplashElement, dummySec, false), this);
			vanillaCon.add(this.progressBarLayoutSplashElement);
		}

		this.content.clear();
		this.content.addAll(vanillaCon);
		this.content.addAll(con);
		
	}

	/**
	 * Returns:<br>
	 * 0 for FALSE<br>
	 * 1 for HORIZONTALLY STRETCHED<br>
	 * 2 for VERTICALLY STRETCHED<br>
	 * 3 for BOTH
	 */
	public static int isObjectStretched(PropertiesSection sec) {
		String w = sec.getEntryValue("width");
		String h = sec.getEntryValue("height");
		String x = sec.getEntryValue("x");
		String y = sec.getEntryValue("y");
		
		boolean stretchX = false;
		if ((w != null) && (x != null)) {
			if (w.equals("%guiwidth%") && x.equals("0")) {
				stretchX = true;
			}
		}
		boolean stretchY = false;
		if ((h != null) && (y != null)) {
			if (h.equals("%guiheight%") && y.equals("0")) {
				stretchY = true;
			}
		}
		
		if (stretchX && stretchY) {
			return 3;
		}
		if (stretchY) {
			return 2;
		}
		if (stretchX) {
			return 1;
		}
		
		return 0;
	}

}
