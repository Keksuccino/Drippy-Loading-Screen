package de.keksuccino.drippyloadingscreen.customization.helper.editor;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.vanilla.*;
import de.keksuccino.drippyloadingscreen.customization.items.vanilla.ForgeMemoryInfoSplashCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.items.vanilla.ForgeTextSplashCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.items.vanilla.ProgressBarSplashCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.api.item.CustomizationItem;
import de.keksuccino.drippyloadingscreen.api.item.CustomizationItemContainer;
import de.keksuccino.drippyloadingscreen.api.item.CustomizationItemLayoutElement;
import de.keksuccino.drippyloadingscreen.customization.CustomizationHandler;
import de.keksuccino.drippyloadingscreen.customization.placeholdervalues.PlaceholderTextValueHelper;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.EditHistory.Snapshot;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorUI.LayoutPropertiesContextMenu;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.LayoutElement;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.LayoutShape;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.LayoutSlideshow;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.LayoutSplashText;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.LayoutTexture;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.string.LayoutWebString;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.LayoutWebTexture;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.string.LayoutString;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.UIBase;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.content.FHContextMenu;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.FHNotificationPopup;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.FHTextInputPopup;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.FHYesNoPopup;
import de.keksuccino.drippyloadingscreen.customization.items.ShapeCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.items.ShapeCustomizationItem.Shape;
import de.keksuccino.drippyloadingscreen.customization.items.SlideshowCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.items.SplashTextCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.items.StringCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.items.TextureCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.items.WebStringCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.items.WebTextureCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.items.vanilla.LogoSplashCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.rendering.slideshow.SlideshowHandler;
import de.keksuccino.konkrete.gui.content.ContextMenu;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.CharacterFilter;
import de.keksuccino.konkrete.input.KeyboardData;
import de.keksuccino.konkrete.input.KeyboardHandler;
import de.keksuccino.konkrete.input.MouseInput;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.properties.PropertiesSet;
import de.keksuccino.konkrete.rendering.RenderUtils;
import de.keksuccino.konkrete.web.WebUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class LayoutEditorScreen extends Screen {
	
	public static boolean isActive = false;

	protected static final List<PropertiesSection> COPIED_ELEMENT_CACHE = new ArrayList<PropertiesSection>();
	protected static boolean initDone = false;
	
	public EditHistory history = new EditHistory(this);
	
	public List<Runnable> postRenderTasks = new ArrayList<Runnable>();
	
	protected List<LayoutElement> content = new ArrayList<LayoutElement>();
	
	protected List<LayoutElement> newContentMove;
	protected List<LayoutElement> newContentPaste = new ArrayList<LayoutElement>();
	public List<LayoutElement> deleteContentQueue = new ArrayList<LayoutElement>();
	
	protected List<LayoutElement> focusedObjects = new ArrayList<LayoutElement>();
	protected List<LayoutElement> focusedObjectsCache = new ArrayList<LayoutElement>();
	
	protected FHContextMenu multiselectRightclickMenu;
	protected LayoutPropertiesContextMenu propertiesRightclickMenu;
	
	protected String renderorder = "foreground";
	protected String requiredmods;
	protected String minimumMC;
	protected String maximumMC;
	protected String minimumDL;
	protected String maximumDL;

	protected int biggerThanWidth = 0;
	protected int biggerThanHeight = 0;
	protected int smallerThanWidth = 0;
	protected int smallerThanHeight = 0;

	//TODO übernehmen
	protected int scale = 0;
	protected boolean fadeOut = true;
	//--------------

	//TODO übernehmen
	protected int autoScalingWidth = 0;
	protected int autoScalingHeight = 0;
	//--------------

	protected boolean multiselectStretchedX = false;
	protected boolean multiselectStretchedY = false;
	protected List<ContextMenu> multiselectChilds = new ArrayList<ContextMenu>();
	
	protected Map<String, Boolean> focusChangeBlocker = new HashMap<String, Boolean>();
	protected LayoutElement topObject;
	
	public LayoutEditorUI ui = new LayoutEditorUI(this);

	public SplashCustomizationLayer splashLayer = new SplashCustomizationLayer(true);

	protected LogoLayoutSplashElement logoLayoutSplashElement;
	protected ForgeMemoryInfoLayoutSplashElement forgeMemoryInfoLayoutSplashElement;
	protected ForgeTextLayoutSplashElement forgeTextLayoutSplashElement;
	protected ProgressBarLayoutSplashElement progressBarLayoutSplashElement;
	
	public LayoutEditorScreen() {
		
		super(new TextComponent(""));

		if (!initDone) {
			KeyboardHandler.addKeyPressedListener(LayoutEditorScreen::onShortcutPressed);
			KeyboardHandler.addKeyPressedListener(LayoutEditorScreen::onArrowKeysPressed);
			initDone = true;
		}
		
		PropertiesSection sec = new PropertiesSection("customization");

		this.logoLayoutSplashElement = new LogoLayoutSplashElement(new LogoSplashCustomizationItem(this.splashLayer.logoSplashElement, sec, false), this);
//		this.forgeMemoryInfoLayoutSplashElement = new ForgeMemoryInfoLayoutSplashElement(new ForgeMemoryInfoSplashCustomizationItem(this.splashLayer.forgeMemoryInfoSplashElement, sec, false), this);
//		this.forgeTextLayoutSplashElement = new ForgeTextLayoutSplashElement(new ForgeTextSplashCustomizationItem(this.splashLayer.forgeTextSplashElement, sec, false), this);
		this.progressBarLayoutSplashElement = new ProgressBarLayoutSplashElement(new ProgressBarSplashCustomizationItem(this.splashLayer.progressBarSplashElement, sec, false), this);

		this.content.add(this.logoLayoutSplashElement);
		this.content.add(this.forgeMemoryInfoLayoutSplashElement);
		this.content.add(this.forgeTextLayoutSplashElement);
		this.content.add(this.progressBarLayoutSplashElement);
		
	}

	@Override
	protected void init() {
		
		this.ui.updateUI();
		
		if (this.multiselectRightclickMenu != null) {
			this.multiselectRightclickMenu.closeMenu();
		}
		this.multiselectRightclickMenu = new LayoutEditorUI.MultiselectContextMenu(this);
		this.multiselectRightclickMenu.setAutoclose(false);
		this.multiselectRightclickMenu.setAlwaysOnTop(true);
		
		if (this.propertiesRightclickMenu != null) {
			this.propertiesRightclickMenu.closeMenu();
		}
		this.propertiesRightclickMenu = new LayoutPropertiesContextMenu(this, true);
		this.propertiesRightclickMenu.setAutoclose(false);
		this.propertiesRightclickMenu.setAlwaysOnTop(true);

		this.focusedObjects.clear();
		
		this.focusChangeBlocker.clear();

		//TODO übernehmen
		Window w = Minecraft.getInstance().getWindow();
		if (this.scale > 0) {
			w.setGuiScale(this.scale);
		} else {
			int mcScale = w.calculateScale(Minecraft.getInstance().options.guiScale, Minecraft.getInstance().isEnforceUnicode());
			w.setGuiScale(mcScale);
		}
		this.width = w.getGuiScaledWidth();
		this.height = w.getGuiScaledHeight();
		//-------------------

		//TODO übernehmen
		//Handle auto-scaling
		if ((this.autoScalingWidth != 0) && (this.autoScalingHeight != 0)) {
			double guiWidth = this.width * w.getGuiScale();
			double guiHeight = this.height * w.getGuiScale();
			double percentX = (guiWidth / (double)this.autoScalingWidth) * 100.0D;
			double percentY = (guiHeight / (double)this.autoScalingHeight) * 100.0D;
			double newScaleX = (percentX / 100.0D) * w.getGuiScale();
			double newScaleY = (percentY / 100.0D) * w.getGuiScale();
			double newScale = Math.min(newScaleX, newScaleY);

			w.setGuiScale(newScale);
			this.width = w.getGuiScaledWidth();
			this.height = w.getGuiScaledHeight();
		}
		
	}
	
	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	protected List<PropertiesSection> getAllProperties() {
		List<PropertiesSection> l = new ArrayList<PropertiesSection>();
		
		PropertiesSection meta = new PropertiesSection("customization-meta");
		
		meta.addEntry("renderorder", this.renderorder);

		if (this.splashLayer.customBackgroundHex != null) {
			meta.addEntry("backgroundcolor", this.splashLayer.customBackgroundHex);
		}

		if (this.splashLayer.backgroundImagePath != null) {
			meta.addEntry("backgroundimage", this.splashLayer.backgroundImagePath);
		}
		
		if ((this.requiredmods != null) && !this.requiredmods.replace(" ", "").equals("")) {
			meta.addEntry("requiredmods", this.requiredmods);
		}
		
		if ((this.minimumMC != null) && !this.minimumMC.replace(" ", "").equals("")) {
			meta.addEntry("minimummcversion", this.minimumMC);
		}
		if ((this.maximumMC != null) && !this.maximumMC.replace(" ", "").equals("")) {
			meta.addEntry("maximummcversion", this.maximumMC);
		}
		
		if ((this.minimumDL != null) && !this.minimumDL.replace(" ", "").equals("")) {
			meta.addEntry("minimumdlversion", this.minimumDL);
		}
		if ((this.maximumDL != null) && !this.maximumDL.replace(" ", "").equals("")) {
			meta.addEntry("maximumdlversion", this.maximumDL);
		}
		
		if (this.biggerThanWidth != 0) {
			meta.addEntry("biggerthanwidth", "" + this.biggerThanWidth);
		}
		if (this.biggerThanHeight != 0) {
			meta.addEntry("biggerthanheight", "" + this.biggerThanHeight);
		}
		if (this.smallerThanWidth != 0) {
			meta.addEntry("smallerthanwidth", "" + this.smallerThanWidth);
		}
		if (this.smallerThanHeight != 0) {
			meta.addEntry("smallerthanheight", "" + this.smallerThanHeight);
		}
		//TODO übernehmen
		if (this.scale > 0) {
			meta.addEntry("scale", "" + this.scale);
		}
		//TODO übernehmen
		if (!this.fadeOut) {
			meta.addEntry("fadeout", "false");
		}
		//TODO übernehmen
		if ((this.autoScalingWidth != 0) && (this.autoScalingHeight != 0)) {
			meta.addEntry("autoscale_basewidth", "" + this.autoScalingWidth);
			meta.addEntry("autoscale_baseheight", "" + this.autoScalingHeight);
		}
		
		l.add(meta);
		
		//Add element props
		for (LayoutElement o : this.content) {
			l.addAll(o.getProperties());
		}
		
		return l;
	}

	protected void closeMultiselectChildMenus() {
		for (ContextMenu m : this.multiselectChilds) {
			m.closeMenu();
		}
	}
	
	public void addContent(LayoutElement object) {
		if (!this.content.contains(object)) {
			this.content.add(object);
		}
	}

	public void removeContent(LayoutElement object) {
		if (this.content.contains(object)) {
			if ((this.isFocused(object))) {
				this.focusedObjects.remove(object);
			}
			this.content.remove(object);
		}
		this.focusChangeBlocker.clear();
	}
	
	public List<LayoutElement> getContent() {
		return this.content;
	}

	@Override
	public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		
		//Handle object focus and update the top hovered object
		if (!MouseInput.isVanillaInputBlocked()) {
			if (!KeyboardHandler.isCtrlPressed() && !this.focusedObjects.isEmpty() && !this.isFocusedHovered() && !this.isFocusedDragged() && !this.isFocusedGrabberPressed() && !this.isFocusedGettingResized() && (MouseInput.isLeftMouseDown() || MouseInput.isRightMouseDown())) {
				if (((this.multiselectRightclickMenu == null) || !this.multiselectRightclickMenu.isHovered()) && ((this.propertiesRightclickMenu == null) || !this.propertiesRightclickMenu.isHovered()) && !this.isFocusChangeBlocked() && !this.ui.bar.isHovered() && !this.ui.bar.isChildOpen()) {
					this.focusedObjects.clear();
				}
			}
			LayoutElement ob = null;
			LayoutElement top = null;
			for (LayoutElement o : this.content) {
				if (o.isHovered()) {
					top = o;
					if (MouseInput.isLeftMouseDown() || MouseInput.isRightMouseDown()) {
						ob = o;
					}
				}
			}
			if (((KeyboardHandler.isCtrlPressed() && !this.isFocused(ob)) || (!this.isObjectFocused()) && (ob != null))) {
				this.setObjectFocused(ob, true, false);
			}
			this.topObject = top;
		} else {
			if (!this.ui.bar.isHovered() && !this.ui.bar.isChildOpen()) {
				this.focusedObjects.clear();
			}
		}

		this.renderEditorBackground(matrix);

		//TODO übernehmen
		this.drawGrid(matrix);
		
		//Render vanilla elements if rendering order is set to foreground
		if (this.renderorder.equals("foreground")) {
			this.renderVanillaElements(matrix, partialTicks);
		}
		
		//Renders all layout objects. The focused object is always rendered on top of all other objects.
		for (LayoutElement l : this.content) {
			if (!this.isFocused(l)) {
				if (((l instanceof VanillaLayoutSplashElement) && !CustomizationHandler.isLightModeEnabled()) || !(l instanceof VanillaLayoutSplashElement)) {
					l.render(matrix, mouseX, mouseY);
				}
			}
		}

		//Render vanilla elements if rendering order is set to background
		if (this.renderorder.equals("background")) {
			this.renderVanillaElements(matrix, partialTicks);
		}
		
		for (LayoutElement o : this.getFocusedObjects()) {
			o.render(matrix, mouseX, mouseY);
		}
		
		super.render(matrix, mouseX, mouseY, partialTicks);
		
		//Handle multiselect rightclick menu
		if (this.multiselectRightclickMenu != null) {

			if ((this.focusedObjects.size() > 1) && this.isFocusedHovered()) {
				if (MouseInput.isRightMouseDown()) {
					UIBase.openScaledContextMenuAtMouse(this.multiselectRightclickMenu);
				}
			}

			if (!PopupHandler.isPopupActive()) {
				UIBase.renderScaledContextMenu(matrix, this.multiselectRightclickMenu);
			} else {
				this.multiselectRightclickMenu.closeMenu();
			}

			if (MouseInput.isLeftMouseDown() && !this.multiselectRightclickMenu.isHovered()) {
				this.multiselectRightclickMenu.closeMenu();
			}

			if (this.multiselectRightclickMenu.isOpen()) {
				this.setFocusChangeBlocked("editor.context.multiselect", true);
			} else {
				this.setFocusChangeBlocked("editor.context.multiselect", false);
			}

		}

		//Handle properties context menu
		if (this.propertiesRightclickMenu != null) {

			if (!this.isContentHovered() && MouseInput.isRightMouseDown()) {
				UIBase.openScaledContextMenuAtMouse(this.propertiesRightclickMenu);
			}

			if (!PopupHandler.isPopupActive()) {
				UIBase.renderScaledContextMenu(matrix, this.propertiesRightclickMenu);
			} else {
				this.propertiesRightclickMenu.closeMenu();
			}

			if (MouseInput.isLeftMouseDown() && !this.propertiesRightclickMenu.isHovered()) {
				this.propertiesRightclickMenu.closeMenu();
			}

			if (this.propertiesRightclickMenu.isOpen()) {
				this.setFocusChangeBlocked("editor.context.properties", true);
			} else {
				this.setFocusChangeBlocked("editor.context.properties", false);
			}

		}
				
		//Render rightclick menus of all layout elements
		for (LayoutElement e : this.content) {
			if (e.rightclickMenu != null) {
				if (!PopupHandler.isPopupActive()) {
					UIBase.renderScaledContextMenu(matrix, e.rightclickMenu);
				}
			}
		}
		
		//Render the editor UI
		this.ui.render(matrix, this);

		//Needs to be done after other object render stuff to prevent ConcurrentModificationExceptions.
		if (this.newContentMove != null) {
			this.history.saveSnapshot(this.history.createSnapshot());
			this.content = this.newContentMove;
			this.newContentMove = null;
		}
		if (!this.newContentPaste.isEmpty()) {
			this.content.addAll(this.newContentPaste);
			this.newContentPaste.clear();
		}
		if (!this.deleteContentQueue.isEmpty()) {
			this.history.saveSnapshot(this.history.createSnapshot());
			for (LayoutElement e : this.deleteContentQueue) {
				if (e.isDestroyable()) {
					this.removeContent(e);
				}
			}
			this.deleteContentQueue.clear();
		}
		
		for (Runnable r : this.postRenderTasks) {
			r.run();
		}
		this.postRenderTasks.clear();

	}

	//TODO übernehmen
	protected void drawGrid(PoseStack matrix) {
		if (DrippyLoadingScreen.config.getOrDefault("showgrid", false)) {
			Color c = new Color(255, 255, 255, 100);
			int gridSize = DrippyLoadingScreen.config.getOrDefault("gridsize", 10);
			int lineThickness = 1;
			int verticalLines = Minecraft.getInstance().getWindow().getGuiScaledWidth() / gridSize;
			int horizontalLines = Minecraft.getInstance().getWindow().getGuiScaledHeight() / gridSize;

			//Draw vertical lines
			int i1 = 1;
			int space1 = 0;
			while (i1 <= verticalLines) {
				int minX = (gridSize * i1) + space1;
				int maxX = minX + lineThickness;
				int minY = 0;
				int maxY = Minecraft.getInstance().getWindow().getGuiScaledHeight();
				fill(matrix, minX, minY, maxX, maxY, c.getRGB());
				i1++;
				space1 += lineThickness;
			}

			//Draw horizontal lines
			int i2 = 1;
			int space2 = 0;
			while (i2 <= horizontalLines) {
				int minX = 0;
				int maxX = Minecraft.getInstance().getWindow().getGuiScaledWidth();
				int minY = (gridSize * i2) + space2;
				int maxY = minY + lineThickness;
				fill(matrix, minX, minY, maxX, maxY, c.getRGB());
				i2++;
				space2 += lineThickness;
			}
		}
	}
	
	protected void renderVanillaElements(PoseStack matrix, float partial) {
		
		this.splashLayer.renderLayer();
		
	}

	protected void renderEditorBackground(PoseStack matrix) {
		RenderSystem.enableBlend();
		Color c = new Color(239, 50, 61);
		if (this.splashLayer.customBackgroundColor != null) {
			c = this.splashLayer.customBackgroundColor;
		}
		fill(matrix, 0, 0, this.width, this.height, c.getRGB());
		if (this.splashLayer.backgroundImage != null) {
			RenderUtils.bindTexture(this.splashLayer.backgroundImage);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			blit(matrix, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
			RenderSystem.disableBlend();
		}
	}

	public boolean isFocused(LayoutElement object) {
		if (PopupHandler.isPopupActive()) {
			return false;
		}
		return (this.focusedObjects.contains(object));
	}

	public void setObjectFocused(LayoutElement object, boolean focused, boolean ignoreBlockedFocusChange) {
		if (this.isFocusChangeBlocked() && !ignoreBlockedFocusChange) {
			return;
		}
		if (!this.content.contains(object)) {
			return;
		}
		if (focused) {
			if (!this.focusedObjects.contains(object)) {
				this.focusedObjects.add(object);
			}
		} else {
			if (this.focusedObjects.contains(object)) {
				this.focusedObjects.remove(object);
			}
		}
	}

	public boolean isObjectFocused() {
		return (!this.focusedObjects.isEmpty());
	}

	public boolean isFocusedHovered() {
		for (LayoutElement o : this.focusedObjects) {
			if (o.isHovered()) {
				return true;
			}
		}
		return false;
	}

	public boolean isFocusedDragged() {
		for (LayoutElement o : this.focusedObjects) {
			if (o.isDragged()) {
				return true;
			}
		}
		return false;
	}

	public boolean isFocusedGrabberPressed() {
		for (LayoutElement o : this.focusedObjects) {
			if (o.isGrabberPressed()) {
				return true;
			}
		}
		return false;
	}

	public boolean isFocusedGettingResized() {
		for (LayoutElement o : this.focusedObjects) {
			if (o.isGettingResized()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a copy of the focused objects list.
	 */
	public List<LayoutElement> getFocusedObjects() {
		List<LayoutElement> l = new ArrayList<LayoutElement>();
		l.addAll(this.focusedObjects);
		return l;
	}

	public void clearFocusedObjects() {
		if (this.multiselectRightclickMenu != null) {
			this.multiselectRightclickMenu.closeMenu();
		}
		this.focusedObjects.clear();
	}
	
	public boolean isContentHovered() {
		for (LayoutElement o : this.content) {
			if (o.isHovered()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the LayoutObject the given object was moved above.
	 */
	public LayoutElement moveUp(LayoutElement o) {
		LayoutElement movedAbove = null;
		try {
			if (this.content.contains(o)) {
				List<LayoutElement> l = new ArrayList<LayoutElement>();
				int index = this.content.indexOf(o);
				int i = 0;
				if (index < this.content.size() - 1) {
					for (LayoutElement o2 : this.content) {
						if (o2 != o) {
							l.add(o2);
							if (i == index+1) {
								movedAbove = o2;
								l.add(o);
							}
						}
						i++;
					}
					
					this.newContentMove = l;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return movedAbove;
	}

	/**
	 * Returns the LayoutObject the object was moved behind.<br>
	 * Will <b>NOT</b> move behind {@link VanillaLayoutSplashElement}s, but will return the vanilla button the object would have been moved under.
	 */
	public LayoutElement moveDown(LayoutElement o) {
		LayoutElement movedBehind = null;
		try {
			if (this.content.contains(o)) {
				List<LayoutElement> l = new ArrayList<LayoutElement>();
				int index = this.content.indexOf(o);
				int i = 0;
				if (index > 0) {
					for (LayoutElement o2 : this.content) {
						if (o2 != o) {
							if (i == index-1) {
								l.add(o);
								movedBehind = o2;
							}
							l.add(o2);
						}
						i++;
					}
					
					if (!(movedBehind instanceof VanillaLayoutSplashElement)) {
						this.newContentMove = l;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return movedBehind;
	}

	protected void addTexture(String path) {
		File home = new File("");
		if (path == null) {
			return;
		}
		if (path.startsWith(home.getAbsolutePath())) {
			path = path.replace(home.getAbsolutePath(), "");
			if (path.startsWith("\\") || path.startsWith("/")) {
				path = path.substring(1);
			}
		}
		File f = new File(path);
		String filename = CharacterFilter.getBasicFilenameCharacterFilter().filterForAllowedChars(f.getName());
		if (f.exists()) {
			if (filename.equals(f.getName())) {
				this.history.saveSnapshot(this.history.createSnapshot());
				
				PropertiesSection sec = new PropertiesSection("customization");
				sec.addEntry("action", "addtexture");
				sec.addEntry("path", path);
				sec.addEntry("height", "100");
				sec.addEntry("y", "" + (int)(this.ui.bar.getHeight() * UIBase.getUIScale()));
				
				TextureCustomizationItem i = new TextureCustomizationItem(sec);
				this.addContent(new LayoutTexture(i, this));

			} else {
				displayNotification(Locals.localize("drippyloadingscreen.helper.creator.textures.invalidcharacters"), "", "", "", "", "", "");
			}
		} else {
			displayNotification("§c§l" + Locals.localize("drippyloadingscreen.helper.creator.invalidimage.title"), "", Locals.localize("drippyloadingscreen.helper.creator.invalidimage.desc"), "", "", "", "", "", "");
		}
	}

	protected void addWebTexture(String url) {
		String finalUrl = null;
		if (url != null) {
			url = WebUtils.filterURL(url);
			finalUrl = PlaceholderTextValueHelper.convertFromRaw(url);
		}
		if (WebUtils.isValidUrl(finalUrl)) {
			this.history.saveSnapshot(this.history.createSnapshot());
			
			PropertiesSection s = new PropertiesSection("customization");
			s.addEntry("action", "addwebtexture");
			s.addEntry("url", url);
			s.addEntry("height", "100");
			s.addEntry("y", "" + (int)(this.ui.bar.getHeight() * UIBase.getUIScale()));
			this.addContent(new LayoutWebTexture(new WebTextureCustomizationItem(s), this));
		} else {
			displayNotification(Locals.localize("drippyloadingscreen.helper.creator.web.invalidurl"), "", "", "", "", "", "");
		}
	}

	protected void addSlideshow(String name) {
		if (name == null) {
			return;
		}
		if (SlideshowHandler.slideshowExists(name)) {
			this.history.saveSnapshot(this.history.createSnapshot());
			
			PropertiesSection s = new PropertiesSection("customization");
			s.addEntry("action", "addslideshow");
			s.addEntry("name", name);
			s.addEntry("y", "" + (int)(this.ui.bar.getHeight() * UIBase.getUIScale()));
			SlideshowCustomizationItem i = new SlideshowCustomizationItem(s);
			int w = SlideshowHandler.getSlideshow(name).width;
			int h = SlideshowHandler.getSlideshow(name).height;
			double ratio = (double) w / (double) h;
			i.height = 100;
			i.width = (int)(i.height * ratio);
			
			this.addContent(new LayoutSlideshow(i, this));

		} else {
			displayNotification(Locals.localize("drippyloadingscreen.helper.creator.slideshownotfound"), "", "", "", "");
		}
	}
	
	protected void addShape(Shape shape) {
		PropertiesSection s = new PropertiesSection("customization");
		s.addEntry("action", "addshape");
		s.addEntry("shape", shape.name);
		s.addEntry("width", "100");
		s.addEntry("height", "100");
		s.addEntry("y", "" + (int)(this.ui.bar.getHeight() * UIBase.getUIScale()));
		this.history.saveSnapshot(this.history.createSnapshot());
		this.addContent(new LayoutShape(new ShapeCustomizationItem(s), this));
	}
	
	protected void addMultiSplashText(String path) {
		File home = new File("");
		if (path == null) {
			return;
		}
		if (path.startsWith(home.getAbsolutePath())) {
			path = path.replace(home.getAbsolutePath(), "");
			if (path.startsWith("\\") || path.startsWith("/")) {
				path = path.substring(1);
			}
		}
		File f = new File(path);
		if (f.exists() && f.getPath().toLowerCase().endsWith(".txt")) {
			
			this.history.saveSnapshot(this.history.createSnapshot());
			
			PropertiesSection sec = new PropertiesSection("customization");
			sec.addEntry("action", "addsplash");
			sec.addEntry("splashfilepath", path);
			sec.addEntry("y", "" + (int)(this.ui.bar.getHeight() * UIBase.getUIScale()));
			
			SplashTextCustomizationItem i = new SplashTextCustomizationItem(sec);
			this.addContent(new LayoutSplashText(i, this));
			
		} else {
			displayNotification(Locals.localize("drippyloadingscreen.helper.creator.error.invalidfile"));
		}
	}
	
	protected void addSingleSplashText(String content) {
		if (content == null) {
			return;
		}
		if (!content.replace(" ", "").equals("")) {
			
			this.history.saveSnapshot(this.history.createSnapshot());
			
			PropertiesSection sec = new PropertiesSection("customization");
			sec.addEntry("action", "addsplash");
			sec.addEntry("text", content);
			sec.addEntry("y", "" + (int)(this.ui.bar.getHeight() * UIBase.getUIScale()));
			
			SplashTextCustomizationItem i = new SplashTextCustomizationItem(sec);
			this.addContent(new LayoutSplashText(i, this));
			
		} else {
			displayNotification("§c§l" + Locals.localize("drippyloadingscreen.helper.creator.texttooshort.title"), "", Locals.localize("drippyloadingscreen.helper.creator.texttooshort.desc"), "", "", "", "");
		}
	}
	
	protected void addWebText(String url) {
		String finalUrl = null;
		if (url != null) {
			url = WebUtils.filterURL(url);
			finalUrl = PlaceholderTextValueHelper.convertFromRaw(url);
		}
		if (WebUtils.isValidUrl(finalUrl)) {
			this.history.saveSnapshot(this.history.createSnapshot());
			
			PropertiesSection s = new PropertiesSection("customization");
			s.addEntry("action", "addwebtext");
			s.addEntry("url", url);
			s.addEntry("y", "" + (int)(this.ui.bar.getHeight() * UIBase.getUIScale()));
			this.addContent(new LayoutWebString(new WebStringCustomizationItem(s), this));
		} else {
			displayNotification(Locals.localize("drippyloadingscreen.helper.creator.web.invalidurl"), "", "", "", "", "", "");
		}
	}
	
	protected void addText(String text) {
		if (text == null) {
			return;
		}
		if (text.length() > 0) {
			this.history.saveSnapshot(this.history.createSnapshot());
			
			PropertiesSection s = new PropertiesSection("customization");
			s.addEntry("action", "addtext");
			s.addEntry("value", StringUtils.convertFormatCodes(text, "&", "§"));
			s.addEntry("y", "" + (int)(this.ui.bar.getHeight() * UIBase.getUIScale()));
			StringCustomizationItem i = new StringCustomizationItem(s);
			this.addContent(new LayoutString(i, this));
		} else {
			displayNotification("§c§l" + Locals.localize("drippyloadingscreen.helper.creator.texttooshort.title"), "", Locals.localize("drippyloadingscreen.helper.creator.texttooshort.desc"), "", "", "", "");
		}
	}
	
	protected void addCustomItem(CustomizationItemContainer container) {
		
		CustomizationItem i = container.createNew();
		
		if (i != null) {
			
			this.history.saveSnapshot(this.history.createSnapshot());

			this.addContent(new CustomizationItemLayoutElement(container, i, this));
			
		}
		
	}

	protected void deleteFocusedObjects() {
		List<LayoutElement> l = new ArrayList<LayoutElement>();
		l.addAll(this.focusedObjects);
		
		if (!l.isEmpty()) {
			if (l.size() == 1) {
				if (l.get(0).isDestroyable()) {
					l.get(0).destroyObject();
				} else {
					displayNotification("§c§l" + Locals.localize("drippyloadingscreen.helper.creator.cannotdelete.title"), "", Locals.localize("drippyloadingscreen.helper.creator.cannotdelete.desc"), "", "", "");
				}
			}
			if (l.size() > 1) {
				if (DrippyLoadingScreen.config.getOrDefault("editordeleteconfirmation", true)) {
					PopupHandler.displayPopup(new FHYesNoPopup(300, new Color(0, 0, 0, 0), 240, (call) -> {
						if (call) {
							this.deleteContentQueue.addAll(l);
						}
					}, "§c§l" + Locals.localize("drippyloadingscreen.helper.creator.messages.sure"), "", Locals.localize("drippyloadingscreen.helper.creator.deleteselectedobjects"), "", "", "", "", ""));
				} else {
					this.deleteContentQueue.addAll(l);
				}
			}
		}
	}

	public void setFocusChangeBlocked(String id, Boolean b) {
		this.focusChangeBlocker.put(id, b);
	}

	public boolean isFocusChangeBlocked() {
		return this.focusChangeBlocker.containsValue(true);
	}
	
	public LayoutElement getTopHoverObject() {
		return this.topObject;
	}

	public void saveLayout() {

		if ((this instanceof PreloadedLayoutEditorScreen) && (((PreloadedLayoutEditorScreen)this).single != null)) {

			if (!CustomizationHandler.saveLayoutTo(this.getAllProperties(), ((PreloadedLayoutEditorScreen)this).single)) {
				this.saveLayoutAs();
			} else {
				Snapshot snap = this.history.createSnapshot();

				PreloadedLayoutEditorScreen neweditor = new PreloadedLayoutEditorScreen(snap.snapshot);
				neweditor.history = this.history;
				this.history.editor = neweditor;
				neweditor.single = ((PreloadedLayoutEditorScreen)this).single;

				Minecraft.getInstance().setScreen(neweditor);
			}

		} else {
			this.saveLayoutAs();
		}

	}

	public void saveLayoutAs() {
		PopupHandler.displayPopup(new FHTextInputPopup(new Color(0, 0, 0, 0), Locals.localize("drippyloadingscreen.helper.editor.ui.layout.saveas.entername"), null, 240, (call) -> {
			try {

				if ((call != null) && (call.length() > 0)) {

					String file = DrippyLoadingScreen.CUSTOMIZATION_DIR.getPath() + "/" + call + ".dllayout";
					File f = new File(file);

					if (!f.exists()) {
						if (!CustomizationHandler.saveLayoutTo(this.getAllProperties(), file)) {
							PopupHandler.displayPopup(new FHNotificationPopup(300, new Color(0, 0, 0, 0), 240, null, Locals.localize("drippyloadingscreen.helper.editor.ui.layout.saveas.failed")));
						} else {
							Snapshot snap = this.history.createSnapshot();

							PreloadedLayoutEditorScreen neweditor = new PreloadedLayoutEditorScreen(snap.snapshot);
							neweditor.history = this.history;
							this.history.editor = neweditor;
							neweditor.single = file;

							Minecraft.getInstance().setScreen(neweditor);
						}
					} else {
						PopupHandler.displayPopup(new FHNotificationPopup(300, new Color(0, 0, 0, 0), 240, null, Locals.localize("drippyloadingscreen.helper.editor.ui.layout.saveas.failed")));
					}
				} else {
					PopupHandler.displayPopup(new FHNotificationPopup(300, new Color(0, 0, 0, 0), 240, null, Locals.localize("drippyloadingscreen.helper.editor.ui.layout.saveas.failed")));
				}

			} catch (Exception e) {
				e.printStackTrace();
				PopupHandler.displayPopup(new FHNotificationPopup(300, new Color(0, 0, 0, 0), 240, null, Locals.localize("drippyloadingscreen.helper.editor.ui.layout.saveas.failed")));
			}
		}));
	}

	public void copySelectedElements() {
		List<LayoutElement> l = this.getFocusedObjects();

		LayoutEditorScreen.COPIED_ELEMENT_CACHE.clear();
		for (LayoutElement e : l) {
			if (!(e instanceof VanillaLayoutSplashElement)) {
				LayoutEditorScreen.COPIED_ELEMENT_CACHE.addAll(e.getProperties());
			}
		}
	}

	public void pasteElements() {
		if (!LayoutEditorScreen.COPIED_ELEMENT_CACHE.isEmpty()) {

			for (PropertiesSection s : COPIED_ELEMENT_CACHE) {
				s.removeEntry("actionid");
				s.addEntry("actionid", CustomizationHandler.generateRandomActionId());
			}

			PropertiesSet set = new PropertiesSet("hud");
			for (PropertiesSection s : LayoutEditorScreen.COPIED_ELEMENT_CACHE) {
				set.addProperties(s);
			}

			//Init dummy preloaded editor to use it's customization action serializer for building the copied elements
			PreloadedLayoutEditorScreen pe = new PreloadedLayoutEditorScreen(set);

			List<LayoutElement> l = new ArrayList<LayoutElement>();
			for (LayoutElement e : pe.content) {
				if (!(e instanceof VanillaLayoutSplashElement)) {
					e.handler = this;
					//Change the element position a bit to better see that the element was successfully pasted
					e.object.posX += 1;
					l.add(e);
				}
			}
			this.history.saveSnapshot(this.history.createSnapshot());
			this.newContentPaste.addAll(l);
			
			this.postRenderTasks.add(new Runnable() {
				@Override
				public void run() {
					LayoutEditorScreen.this.focusedObjects.clear();
					LayoutEditorScreen.this.focusedObjectsCache.clear();
					LayoutEditorScreen.this.focusedObjects.addAll(l);
				}
			});

		}
	}

	protected static void onShortcutPressed(KeyboardData d) {
		Screen c = Minecraft.getInstance().screen;
		
		if (c instanceof LayoutEditorScreen) {
			
			//CTRL + C
			if (d.keycode == 67) {
				if (KeyboardHandler.isCtrlPressed()) {
					if (!PopupHandler.isPopupActive()) {
						((LayoutEditorScreen) c).copySelectedElements();
					}
				}
			}
			
			//CTRL + V
			if (d.keycode == 86) {
				if (KeyboardHandler.isCtrlPressed()) {
					if (!PopupHandler.isPopupActive()) {
						((LayoutEditorScreen) c).pasteElements();
					}
				}
			}
			
			//CTRL + S
			if (d.keycode == 83) {
				if (KeyboardHandler.isCtrlPressed()) {
					if (!PopupHandler.isPopupActive()) {
						((LayoutEditorScreen) c).saveLayout();
					}
				}
			}
			
			//CTRL + Z
			if (d.keycode == 89) {
				if (KeyboardHandler.isCtrlPressed()) {
					((LayoutEditorScreen) c).history.stepBack();
				}
			}
			
			//CTRL + Y
			if (d.keycode == 90) {
				if (KeyboardHandler.isCtrlPressed()) {
					((LayoutEditorScreen) c).history.stepForward();
				}
			}
			
			//DEL
			if (((LayoutEditorScreen)c).isObjectFocused() && !PopupHandler.isPopupActive()) {
				if (d.keycode == 261) {
					((LayoutEditorScreen) c).deleteFocusedObjects();
				}
			}

			//TODO übernehmen
			//CTRL + G
			if (d.keycode == 71) {
				if (KeyboardHandler.isCtrlPressed()) {
					try {
						if (DrippyLoadingScreen.config.getOrDefault("showgrid", false)) {
							DrippyLoadingScreen.config.setValue("showgrid", false);
						} else {
							DrippyLoadingScreen.config.setValue("showgrid", true);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
		}
		
	}
	
	protected static void onArrowKeysPressed(KeyboardData d) {
		Screen c = Minecraft.getInstance().screen;
		
		if (c instanceof LayoutEditorScreen) {
			
			if (((LayoutEditorScreen) c).isObjectFocused() && !PopupHandler.isPopupActive()) {
				
				if (!((d.keycode == 263) || (d.keycode == 262) || (d.keycode == 265) || (d.keycode == 264))) {
					return;
				}
				
				Snapshot snap = ((LayoutEditorScreen) c).history.createSnapshot();
				boolean saveSnap = false;
				
				for (LayoutElement o : ((LayoutEditorScreen) c).focusedObjects) {
					if (d.keycode == 263) {
						saveSnap = true;
						
						o.setX(o.object.posX - 1);
					}
					if (d.keycode == 262) {
						saveSnap = true;
						
						o.setX(o.object.posX + 1);
					}
					if (d.keycode == 265) {
						saveSnap = true;
						
						o.setY(o.object.posY - 1);
					}
					if (d.keycode == 264) {
						saveSnap = true;
						
						o.setY(o.object.posY + 1);
					}
				}
				
				if (saveSnap) {
					((LayoutEditorScreen) c).history.saveSnapshot(snap);
				}
				
			}
			
		}
	}
	
	public static void displayNotification(String... strings) {
		PopupHandler.displayPopup(new FHNotificationPopup(300, new Color(0, 0, 0, 0), 240, null, strings));
	}
	
	public void rebuildEditor() {
		Snapshot snap = this.history.createSnapshot();
		PreloadedLayoutEditorScreen neweditor = new PreloadedLayoutEditorScreen(snap.snapshot);
		neweditor.history = this.history;
		String single = null;
		if (this instanceof PreloadedLayoutEditorScreen) {
			single = ((PreloadedLayoutEditorScreen)this).single;
		}
		neweditor.single = single;
		neweditor.history.editor = neweditor;

		Minecraft.getInstance().setScreen(neweditor);
	}

}
