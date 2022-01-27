package de.keksuccino.drippyloadingscreen.customization.helper.editor.elements;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.custombars.LayoutCustomBarBase;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.FHNotificationPopup;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.FHTextInputPopup;
import de.keksuccino.drippyloadingscreen.customization.items.custombars.CustomBarCustomizationItemBase;
import de.keksuccino.drippyloadingscreen.customization.items.visibilityrequirements.VisibilityRequirementContainer;
import org.lwjgl.glfw.GLFW;

import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.EditHistory.Snapshot;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.UIBase;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.content.FHContextMenu;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.FHYesNoPopup;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.items.CustomizationItemBase;
import de.keksuccino.drippyloadingscreen.customization.items.vanilla.VanillaSplashCustomizationItemBase;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.KeyboardData;
import de.keksuccino.konkrete.input.KeyboardHandler;
import de.keksuccino.konkrete.input.MouseInput;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;

public abstract class LayoutElement extends GuiComponent {
	
	public CustomizationItemBase object;
	public LayoutEditorScreen handler;
	protected boolean hovered = false;
	protected boolean dragging = false;
	protected boolean resizing = false;
	protected int activeGrabber = -1;
	protected int lastGrabber;
	protected int startDiffX;
	protected int startDiffY;
	protected int startX;
	protected int startY;
	protected int startWidth;
	protected int startHeight;
	protected int orientationDiffX = 0;
	protected int orientationDiffY = 0;
	protected boolean stretchable = false;
	protected boolean stretchX = false;
	protected boolean stretchY = false;
	protected boolean orderable = true;
	protected boolean copyable = true;
	protected boolean delayable = true;
	protected boolean fadeable = true;

	protected List<LayoutElement> hoveredLayers = new ArrayList<LayoutElement>();

	public FHContextMenu rightclickMenu;

	protected AdvancedButton stretchXButton;
	protected AdvancedButton stretchYButton;

	protected AdvancedButton oLoadingProgress;
	protected AdvancedButton o1;
	protected AdvancedButton o2;
	protected AdvancedButton o3;
	protected AdvancedButton o4;
	protected AdvancedButton o5;
	protected AdvancedButton o6;
	protected AdvancedButton o7;
	protected AdvancedButton o8;
	protected AdvancedButton o9;

	protected static boolean isShiftPressed = false;
	private static boolean shiftListener = false;
	
	private final boolean destroyable;
	public boolean resizable = true;
	public boolean enableVisibilityRequirements = true;

	/** Only for internal use. Not to confuse with the action ID of {@link CustomizationItemBase}'s. */
	public final String objectId = UUID.randomUUID().toString();

	private Snapshot cachedSnapshot;
	private boolean moving = false;
	
	protected static final long H_RESIZE_CURSOR = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR);
	protected static final long V_RESIZE_CURSOR = GLFW.glfwCreateStandardCursor(GLFW.GLFW_VRESIZE_CURSOR);
	protected static final long NORMAL_CURSOR = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
	
	public LayoutElement(@Nonnull CustomizationItemBase object, boolean destroyable, @Nonnull LayoutEditorScreen handler) {
		this.handler = handler;
		this.object = object;
		this.destroyable = destroyable;

		if (!shiftListener) {
			KeyboardHandler.addKeyPressedListener(new Consumer<KeyboardData>() {
				@Override
				public void accept(KeyboardData t) {
					if ((t.keycode == 340) || (t.keycode == 344)) {
						isShiftPressed = true;
					}
				}
			});
			KeyboardHandler.addKeyReleasedListener(new Consumer<KeyboardData>() {
				@Override
				public void accept(KeyboardData t) {
					if ((t.keycode == 340) || (t.keycode == 344)) {
						isShiftPressed = false;
					}
				}
			});
			shiftListener = true;
		}
		
		this.init();
	}

	public void init() {
		
		this.rightclickMenu = new FHContextMenu();
		this.rightclickMenu.setAlwaysOnTop(true);

		/** COPY ELEMENT ID **/
		AdvancedButton copyIdButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.editor.items.copyid"), true, (press) -> {
			Minecraft.getInstance().keyboardHandler.setClipboard(this.object.getActionId());
		});
		copyIdButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.editor.items.copyid.btn.desc"), "%n%"));
		this.rightclickMenu.addContent(copyIdButton);

		/** ORIENTATION **/
		FHContextMenu orientationMenu = new FHContextMenu();
		orientationMenu.setAutoclose(true);
		this.rightclickMenu.addChild(orientationMenu);

		oLoadingProgress = new AdvancedButton(0, 0, 0, 16, "loading-progress", (press) -> {
			this.handler.setObjectFocused(this, false, true);
			FHTextInputPopup pop = new FHTextInputPopup(new Color(0, 0, 0, 0), Locals.localize("drippyloadingscreen.helper.editor.items.orientation.loading-progress.setidentifier"), null, 240, (call) -> {
				if (call != null) {
					LayoutElement l = this.handler.getElementByActionId(call);
					if (l != null) {
						if (l instanceof LayoutCustomBarBase) {
							this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
							this.object.orientationElementIdentifier = call;
							this.object.orientationElement = l.object;
							this.handler.history.setPreventSnapshotSaving(true);
							this.setOrientation("loading-progress");
							this.handler.history.setPreventSnapshotSaving(false);
						} else {
							PopupHandler.displayPopup(new FHNotificationPopup(300, new Color(0, 0, 0, 0), 240, null, Locals.localize("drippyloadingscreen.helper.editor.items.orientation.loading-progress.setidentifier.invalidtype")));
						}
					} else {
						PopupHandler.displayPopup(new FHNotificationPopup(300, new Color(0, 0, 0, 0), 240, null, Locals.localize("drippyloadingscreen.helper.editor.items.orientation.loading-progress.setidentifier.identifiernotfound")));
					}
				}
			});
			if (this.object.orientationElementIdentifier != null) {
				pop.setText(this.object.orientationElementIdentifier);
			}
			PopupHandler.displayPopup(pop);
			orientationMenu.closeMenu();
		});
		oLoadingProgress.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.editor.items.orientation.loading-progress.btn.desc"), "%n%"));
		orientationMenu.addContent(oLoadingProgress);

		orientationMenu.addSeparator();

		o1 = new AdvancedButton(0, 0, 0, 16, "top-left", (press) -> {
			this.handler.setObjectFocused(this, false, true);
			this.setOrientation("top-left");
			orientationMenu.closeMenu();
		});
		orientationMenu.addContent(o1);

		o2 = new AdvancedButton(0, 0, 0, 16, "mid-left", (press) -> {
			this.handler.setObjectFocused(this, false, true);
			this.setOrientation("mid-left");
			orientationMenu.closeMenu();
		});
		orientationMenu.addContent(o2);

		o3 = new AdvancedButton(0, 0, 0, 16, "bottom-left", (press) -> {
			this.handler.setObjectFocused(this, false, true);
			this.setOrientation("bottom-left");
			orientationMenu.closeMenu();
		});
		orientationMenu.addContent(o3);

		o4 = new AdvancedButton(0, 0, 0, 16, "top-centered", (press) -> {
			this.handler.setObjectFocused(this, false, true);
			this.setOrientation("top-centered");
			orientationMenu.closeMenu();
		});
		orientationMenu.addContent(o4);

		o5 = new AdvancedButton(0, 0, 0, 16, "mid-centered", (press) -> {
			this.handler.setObjectFocused(this, false, true);
			this.setOrientation("mid-centered");
			orientationMenu.closeMenu();
		});
		orientationMenu.addContent(o5);

		o6 = new AdvancedButton(0, 0, 0, 16, "bottom-centered", (press) -> {
			this.handler.setObjectFocused(this, false, true);
			this.setOrientation("bottom-centered");
			orientationMenu.closeMenu();
		});
		orientationMenu.addContent(o6);

		o7 = new AdvancedButton(0, 0, 0, 16, "top-right", (press) -> {
			this.handler.setObjectFocused(this, false, true);
			this.setOrientation("top-right");
			orientationMenu.closeMenu();
		});
		orientationMenu.addContent(o7);

		o8 = new AdvancedButton(0, 0, 0, 16, "mid-right", (press) -> {
			this.handler.setObjectFocused(this, false, true);
			this.setOrientation("mid-right");
			orientationMenu.closeMenu();
		});
		orientationMenu.addContent(o8);

		o9 = new AdvancedButton(0, 0, 0, 16, "bottom-right", (press) -> {
			this.handler.setObjectFocused(this, false, true);
			this.setOrientation("bottom-right");
			orientationMenu.closeMenu();
		});
		orientationMenu.addContent(o9);

		AdvancedButton orientationButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.creator.items.setorientation"), true, (press) -> {
			orientationMenu.setParentButton((AdvancedButton) press);
			orientationMenu.openMenuAt(0, press.y);
		});
		orientationButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.creator.items.orientation.btndesc"), "%n%"));
		this.rightclickMenu.addContent(orientationButton);

		/** LAYERS **/
		FHContextMenu layersMenu = new FHContextMenu();
		layersMenu.setAutoclose(true);
		this.rightclickMenu.addChild(layersMenu);

		AdvancedButton layersButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.creator.items.chooselayer"), true, (press) -> {

			layersMenu.getContent().clear();

			for (LayoutElement o : this.hoveredLayers) {
				String label = o.object.value;
				if (label == null) {
					label = "Element";
				} else {
					if (Minecraft.getInstance().font.width(label) > 200) {
						label = Minecraft.getInstance().font.plainSubstrByWidth(label, 200) + "..";
					}
				}
				AdvancedButton btn = new AdvancedButton(0, 0, 0, 0, label, (press2) -> {
					this.handler.clearFocusedObjects();
					this.handler.setObjectFocused(o, true, true);
				});
				layersMenu.addContent(btn);
			}

			layersMenu.setParentButton((AdvancedButton) press);
			layersMenu.openMenuAt(0, press.y);

		});
		this.rightclickMenu.addContent(layersButton);

		/** STRETCH **/
		FHContextMenu stretchMenu = new FHContextMenu();
		stretchMenu.setAutoclose(true);
		this.rightclickMenu.addChild(stretchMenu);

		stretchXButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.creator.object.stretch.x"), true, (press) -> {
			if (this.stretchX) {
				this.setStretchedX(false, true);
			} else {
				this.setStretchedX(true, true);
			}
		});
		stretchMenu.addContent(stretchXButton);

		stretchYButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.creator.object.stretch.y"), true, (press) -> {
			if (this.stretchY) {
				this.setStretchedY(false, true);
			} else {
				this.setStretchedY(true, true);
			}
		});
		stretchMenu.addContent(stretchYButton);
		
		AdvancedButton stretchButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.creator.object.stretch"), true, (press) -> {
			stretchMenu.setParentButton((AdvancedButton) press);
			stretchMenu.openMenuAt(0, press.y);
		});
		if (this.stretchable) {
			this.rightclickMenu.addContent(stretchButton);
		}

		/** MOVE UP **/
		AdvancedButton moveUpButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.creator.object.moveup"), (press) -> {
			LayoutElement o = this.handler.moveUp(this);
			if (o != null) {
				((AdvancedButton)press).setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.creator.object.moveup.desc", Locals.localize("drippyloadingscreen.helper.creator.object.moveup.desc.subtext", o.object.value)), "%n%"));
			}
		});
		moveUpButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.creator.object.moveup.desc", ""), "%n%"));
		if (this.orderable) {
			this.rightclickMenu.addContent(moveUpButton);
		}

		/** MOVE DOWN **/
		AdvancedButton moveDownButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.creator.object.movedown"), (press) -> {
			LayoutElement o = this.handler.moveDown(this);
			if (o != null) {
				if (o.isVanillaElement()) {
					((AdvancedButton)press).setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.creator.object.movedown.desc", Locals.localize("drippyloadingscreen.helper.creator.object.movedown.desc.subtext.vanillabutton")), "%n%"));
				} else {
					((AdvancedButton)press).setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.creator.object.movedown.desc", Locals.localize("drippyloadingscreen.helper.creator.object.movedown.desc.subtext", o.object.value)), "%n%"));
				}
			}
		});
		moveDownButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.creator.object.movedown.desc", ""), "%n%"));
		if (this.orderable) {
			this.rightclickMenu.addContent(moveDownButton);
		}

//		/** VISIBILITY REQUIREMENTS **/
//		AdvancedButton visibilityRequirementsButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements"), (press) -> {
//			PopupHandler.displayPopup(new VisibilityRequirementsPopup(this.object));
//		});
//		visibilityRequirementsButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.btn.desc", ""), "%n%"));
//		if (this.enableVisibilityRequirements) {
//			this.rightclickMenu.addContent(visibilityRequirementsButton);
//		}
		
		/** COPY **/
		AdvancedButton copyButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.editor.ui.edit.copy"), (press) -> {
			this.handler.copySelectedElements();
		});
		if (this.copyable) {
			this.rightclickMenu.addContent(copyButton);
		}
		
		/** DESTROY **/
		AdvancedButton destroyButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.creator.items.delete"), true, (press) -> {
			this.destroyObject();
		});
		if (this.destroyable) {
			this.rightclickMenu.addContent(destroyButton);
		}
		
		this.rightclickMenu.addSeparator();

	}
	
	protected void setOrientation(String pos) {
		this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
		
		if (pos.startsWith("top-")) {
			this.object.orientation = pos;
			this.object.posX = 0;
			this.object.posY = (int) (this.handler.ui.bar.getHeight() * UIBase.getUIScale());
		} else {
			this.object.orientation = pos;
			this.object.posX = 0;
			this.object.posY = 0;
		}
		
	}
	
	protected int orientationMouseX(int mouseX) {
		if (this.object.orientation.endsWith("-centered")) {
			return mouseX - (this.handler.width / 2);
		}
		if (this.object.orientation.endsWith("-right")) {
			return mouseX - this.handler.width;
		}
		return mouseX;
	}
	
	protected int orientationMouseY(int mouseY) {
		if (this.object.orientation.startsWith("mid-")) {
			return mouseY - (this.handler.height / 2);
		}
		if (this.object.orientation.startsWith("bottom-")) {
			return mouseY - this.handler.height;
		}
		return mouseY;
	}

	public void setStretchedX(boolean b, boolean saveSnapshot) {
		if (this.isOrientationSupportedByStretchAction(b, this.stretchY)) {
			if (saveSnapshot) {
				this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
			}
			this.stretchX = b;
			String stretchXLabel = Locals.localize("drippyloadingscreen.helper.creator.object.stretch.x");
			if (this.stretchX) {
				stretchXLabel = "§a" + stretchXLabel;
			}
			if (this.stretchXButton != null) {
				this.stretchXButton.setMessage(stretchXLabel);
			}
		}
	}

	public void setStretchedY(boolean b, boolean saveSnapshot) {
		if (this.isOrientationSupportedByStretchAction(this.stretchX, b)) {
			if (saveSnapshot) {
				this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
			}
			this.stretchY = b;
			String stretchYLabel = Locals.localize("drippyloadingscreen.helper.creator.object.stretch.y");
			if (this.stretchY) {
				stretchYLabel = "§a" + stretchYLabel;
			}
			if (this.stretchYButton != null) {
				this.stretchYButton.setMessage(stretchYLabel);
			}
		}
	}

	private boolean isOrientationSupportedByStretchAction(boolean stX, boolean stY) {
		try {
			if (stX && !stY) {
				if (!this.object.orientation.equals("top-left") && !this.object.orientation.equals("mid-left") && !this.object.orientation.equals("bottom-left")) {
					LayoutEditorScreen.displayNotification(Locals.localize("drippyloadingscreen.helper.creator.object.stretch.unsupportedorientation", "top-left, mid-left, bottom-left"));
					return false;
				}
			}
			if (stY && !stX) {
				if (!this.object.orientation.equals("top-left") && !this.object.orientation.equals("top-centered") && !this.object.orientation.equals("top-right")) {
					LayoutEditorScreen.displayNotification(Locals.localize("drippyloadingscreen.helper.creator.object.stretch.unsupportedorientation", "top-left, top-centered, top-right"));
					return false;
				}
			}
			if (stX && stY) {
				if (!this.object.orientation.equals("top-left")) {
					LayoutEditorScreen.displayNotification(Locals.localize("drippyloadingscreen.helper.creator.object.stretch.unsupportedorientation", "top-left"));
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private void handleStretch() {
		try {
			if (this.stretchX) {
				this.object.posX = 0;
				this.object.width = Minecraft.getInstance().screen.width;
			}
			if (this.stretchY) {
				this.object.posY = 0;
				this.object.height = Minecraft.getInstance().screen.height;
			}
			if (this.stretchX || this.stretchY) {
				this.oLoadingProgress.active = false;
			}
			if (this.stretchX && !this.stretchY) {
				this.o1.active = true;
				this.o2.active = true;
				this.o3.active = true;
				this.o4.active = false;
				this.o5.active = false;
				this.o6.active = false;
				this.o7.active = false;
				this.o8.active = false;
				this.o9.active = false;
			}
			if (this.stretchY && !this.stretchX) {
				this.o1.active = true;
				this.o2.active = false;
				this.o3.active = false;
				this.o4.active = true;
				this.o5.active = false;
				this.o6.active = false;
				this.o7.active = true;
				this.o8.active = false;
				this.o9.active = false;
			}
			if (this.stretchX && this.stretchY) {
				this.o1.active = true;
				this.o2.active = false;
				this.o3.active = false;
				this.o4.active = false;
				this.o5.active = false;
				this.o6.active = false;
				this.o7.active = false;
				this.o8.active = false;
				this.o9.active = false;
			}
			if (!this.stretchX && !this.stretchY) {
				this.o1.active = true;
				this.o2.active = true;
				this.o3.active = true;
				this.o4.active = true;
				this.o5.active = true;
				this.o6.active = true;
				this.o7.active = true;
				this.o8.active = true;
				this.o9.active = true;
				this.oLoadingProgress.active = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void render(PoseStack matrix, int mouseX, int mouseY) {
		this.updateHovered(mouseX, mouseY);

		//Render the customization item
        try {
        	
			this.object.render(matrix);

			this.handleStretch();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		// Render the border around the object if it's focused (starts to render one tick after the object got focused)
		if (this.handler.isFocused(this)) {
			this.renderBorder(matrix, mouseX, mouseY);
		} else {
			if ((this.handler.getTopHoverObject() == this) && (!this.handler.isObjectFocused() || (!this.handler.isFocusedHovered() && !this.handler.isFocusedDragged() && !this.handler.isFocusedGettingResized() && !this.handler.isFocusedGrabberPressed()))) {
				this.renderHighlightBorder(matrix);
			}
		}
		
		//Reset cursor to default
		if ((this.activeGrabber == -1) && (!MouseInput.isLeftMouseDown() || PopupHandler.isPopupActive())) {
			GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), NORMAL_CURSOR);
		}
				
		//Update dragging state
		if (this.isLeftClicked() && !(this.resizing || this.isGrabberPressed())) {
			this.dragging = true;
		} else {
			if (!MouseInput.isLeftMouseDown()) {
				this.dragging = false;
			}
		}
		
		//Handles the resizing process
		if ((this.isGrabberPressed() || this.resizing) && !this.isDragged() && this.handler.isFocused(this)) {
			if (this.handler.getFocusedObjects().size() == 1) {
				if (!this.resizing) {
					this.cachedSnapshot = this.handler.history.createSnapshot();

					this.lastGrabber = this.getActiveResizeGrabber();
				}
				this.resizing = true;
				this.handleResize(this.orientationMouseX(mouseX), this.orientationMouseY(mouseY));
			}
		}
		
		//Moves the object with the mouse motion if dragged
		if (this.isDragged() && this.handler.isFocused(this)) {
			if (this.handler.getFocusedObjects().size() == 1) {
				if (!this.moving) {
					this.cachedSnapshot = this.handler.history.createSnapshot();
				}

				this.moving = true;
				
				if ((mouseX >= 5) && (mouseX <= this.handler.width -5)) {
					if (!this.stretchX) {
						this.object.posX = this.orientationMouseX(mouseX) - this.startDiffX;
					}
				}
				if ((mouseY >= 5) && (mouseY <= this.handler.height -5)) {
					if (!this.stretchY) {
						this.object.posY = this.orientationMouseY(mouseY) - this.startDiffY;
					}
				}
			}
		}
		if (!this.isDragged()) {
			this.startDiffX = this.orientationMouseX(mouseX) - this.object.posX;
			this.startDiffY = this.orientationMouseY(mouseY) - this.object.posY;

			if (((this.startX != this.object.posX) || (this.startY != this.object.posY)) && this.moving) {
				if (this.cachedSnapshot != null) {
					this.handler.history.saveSnapshot(this.cachedSnapshot);
				}
			}

			this.moving = false;
		}

		if (!MouseInput.isLeftMouseDown()) {
			
			if (((this.startWidth != this.object.width) || (this.startHeight != this.object.height)) && this.resizing) {
				if (this.cachedSnapshot != null) {
					this.handler.history.saveSnapshot(this.cachedSnapshot);
				}
			}

			this.startX = this.object.posX;
			this.startY = this.object.posY;
			
			this.startWidth = this.object.width;
			this.startHeight = this.object.height;
			this.resizing = false;
			
		}

		//Handle rightclick context menu
        if (this.rightclickMenu != null) {
        	
        	if (this.isRightClicked() && this.handler.isFocused(this)) {
            	if (this.handler.getFocusedObjects().size() == 1) {
            		UIBase.openScaledContextMenuAtMouse(this.rightclickMenu);
                	this.hoveredLayers.clear();
                	for (LayoutElement o : this.handler.getContent()) {
                		if (o.isHoveredOrFocused()) {
                			this.hoveredLayers.add(o);
                		}
                	}
            	}
            }

        	if ((MouseInput.isLeftMouseDown() && !this.rightclickMenu.isHoveredOrFocused())) {
        		this.rightclickMenu.closeMenu();
        	}
        	if (MouseInput.isRightMouseDown() && !this.isHoveredOrFocused() && !this.rightclickMenu.isHoveredOrFocused()) {
        		this.rightclickMenu.closeMenu();
        	}
        	
        	if (this.rightclickMenu.isOpen()) {
        		this.handler.setFocusChangeBlocked(objectId, true);
        	} else {
        		this.handler.setFocusChangeBlocked(objectId, false);
        	}

        }
        
	}
	
	protected void renderBorder(PoseStack matrix, int mouseX, int mouseY) {
		//horizontal line top
		GuiComponent.fill(matrix, this.object.getPosX(), this.object.getPosY(), this.object.getPosX() + this.object.width, this.object.getPosY() + 1, Color.BLUE.getRGB());
		//horizontal line bottom
		GuiComponent.fill(matrix, this.object.getPosX(), this.object.getPosY() + this.object.height - 1, this.object.getPosX() + this.object.width, this.object.getPosY() + this.object.height, Color.BLUE.getRGB());
		//vertical line left
		GuiComponent.fill(matrix, this.object.getPosX(), this.object.getPosY(), this.object.getPosX() + 1, this.object.getPosY() + this.object.height, Color.BLUE.getRGB());
		//vertical line right
		GuiComponent.fill(matrix, this.object.getPosX() + this.object.width - 1, this.object.getPosY(), this.object.getPosX() + this.object.width, this.object.getPosY() + this.object.height, Color.BLUE.getRGB());
		
		int w = 4;
		int h = 4;
		
		int yHorizontal = this.object.getPosY() + (this.object.height / 2) - (h / 2);
		int xHorizontalLeft = this.object.getPosX() - (w / 2);
		int xHorizontalRight = this.object.getPosX() + this.object.width - (w / 2);
		
		int xVertical = this.object.getPosX() + (this.object.width / 2) - (w / 2);
		int yVerticalTop = this.object.getPosY() - (h / 2);
		int yVerticalBottom = this.object.getPosY() + this.object.height - (h / 2);

		if (!this.stretchX && this.resizable) {
			//grabber left
			GuiComponent.fill(matrix, xHorizontalLeft, yHorizontal, xHorizontalLeft + w, yHorizontal + h, Color.BLUE.getRGB());
			//grabber right
			GuiComponent.fill(matrix, xHorizontalRight, yHorizontal, xHorizontalRight + w, yHorizontal + h, Color.BLUE.getRGB());
		}
		if (!this.stretchY && this.resizable) {
			//grabber top
			GuiComponent.fill(matrix, xVertical, yVerticalTop, xVertical + w, yVerticalTop + h, Color.BLUE.getRGB());
			//grabber bottom
			GuiComponent.fill(matrix, xVertical, yVerticalBottom, xVertical + w, yVerticalBottom + h, Color.BLUE.getRGB());
		}

		//Update cursor and active grabber when grabber is hovered
		if ((mouseX >= xHorizontalLeft) && (mouseX <= xHorizontalLeft + w) && (mouseY >= yHorizontal) && (mouseY <= yHorizontal + h)) {
			if (!this.stretchX && this.resizable) {
				GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), H_RESIZE_CURSOR);
				this.activeGrabber = 0;
			} else {
				this.activeGrabber = -1;
			}
		} else if ((mouseX >= xHorizontalRight) && (mouseX <= xHorizontalRight + w) && (mouseY >= yHorizontal) && (mouseY <= yHorizontal + h)) {
			if (!this.stretchX && this.resizable) {
				GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), H_RESIZE_CURSOR);
				this.activeGrabber = 1;
			} else {
				this.activeGrabber = -1;
			}
		} else if ((mouseX >= xVertical) && (mouseX <= xVertical + w) && (mouseY >= yVerticalTop) && (mouseY <= yVerticalTop + h)) {
			if (!this.stretchY && this.resizable) {
				GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), V_RESIZE_CURSOR);
				this.activeGrabber = 2;
			} else {
				this.activeGrabber = -1;
			}
		} else if ((mouseX >= xVertical) && (mouseX <= xVertical + w) && (mouseY >= yVerticalBottom) && (mouseY <= yVerticalBottom + h)) {
			if (!this.stretchY && this.resizable) {
				GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), V_RESIZE_CURSOR);
				this.activeGrabber = 3;
			} else {
				this.activeGrabber = -1;
			}
		} else {
			this.activeGrabber = -1;
		}
		
		//Render pos and size values
		RenderUtils.setScale(matrix, 0.5F);
		GuiComponent.drawString(matrix, Minecraft.getInstance().font, Locals.localize("drippyloadingscreen.helper.creator.items.border.orientation") + ": " + this.object.orientation, this.object.getPosX()*2, (this.object.getPosY()*2) - 26, Color.WHITE.getRGB());
		GuiComponent.drawString(matrix, Minecraft.getInstance().font, Locals.localize("drippyloadingscreen.helper.creator.items.border.posx") + ": " + this.object.getPosX(), this.object.getPosX()*2, (this.object.getPosY()*2) - 17, Color.WHITE.getRGB());
		GuiComponent.drawString(matrix, Minecraft.getInstance().font, Locals.localize("drippyloadingscreen.helper.creator.items.border.width") + ": " + this.object.width, this.object.getPosX()*2, (this.object.getPosY()*2) - 8, Color.WHITE.getRGB());
		
		GuiComponent.drawString(matrix, Minecraft.getInstance().font, Locals.localize("drippyloadingscreen.helper.creator.items.border.posy") + ": " + this.object.getPosY(), ((this.object.getPosX() + this.object.width)*2)+3, ((this.object.getPosY() + this.object.height)*2) - 14, Color.WHITE.getRGB());
		GuiComponent.drawString(matrix, Minecraft.getInstance().font, Locals.localize("drippyloadingscreen.helper.creator.items.border.height") + ": " + this.object.height, ((this.object.getPosX() + this.object.width)*2)+3, ((this.object.getPosY() + this.object.height)*2) - 5, Color.WHITE.getRGB());
		RenderUtils.postScale(matrix);
	}
	
	protected void renderHighlightBorder(PoseStack matrix) {
		Color c = new Color(0, 200, 255, 255);
		
		//horizontal line top
		GuiComponent.fill(matrix, this.object.getPosX(), this.object.getPosY(), this.object.getPosX() + this.object.width, this.object.getPosY() + 1, c.getRGB());
		//horizontal line bottom
		GuiComponent.fill(matrix, this.object.getPosX(), this.object.getPosY() + this.object.height - 1, this.object.getPosX() + this.object.width, this.object.getPosY() + this.object.height, c.getRGB());
		//vertical line left
		GuiComponent.fill(matrix, this.object.getPosX(), this.object.getPosY(), this.object.getPosX() + 1, this.object.getPosY() + this.object.height, c.getRGB());
		//vertical line right
		GuiComponent.fill(matrix, this.object.getPosX() + this.object.width - 1, this.object.getPosY(), this.object.getPosX() + this.object.width, this.object.getPosY() + this.object.height, c.getRGB());
	}
	
	/**
	 * <b>Returns:</b><br><br>
	 * 
	 * -1 if NO grabber is currently pressed<br>
	 * 0 if the LEFT grabber is pressed<br>
	 * 1 if the RIGHT grabber is pressed<br>
	 * 2 if the TOP grabber is pressed<br>
	 * 3 if the BOTTOM grabber is pressed
	 * 
	 */
	public int getActiveResizeGrabber() {
		return this.activeGrabber;
	}
	
	public boolean isGrabberPressed() {
		return ((this.getActiveResizeGrabber() != -1) && MouseInput.isLeftMouseDown());
	}

	protected int getAspectWidth(int startW, int startH, int height) {
		double ratio = (double) startW / (double) startH;
		return (int)(height * ratio);
	}

	protected int getAspectHeight(int startW, int startH, int width) {
		double ratio = (double) startW / (double) startH;
		return (int)(width / ratio);
	}
	
	protected void handleResize(int mouseX, int mouseY) {
		int g = this.lastGrabber;
		int diffX;
		int diffY;
		int sX = this.startX;
		int sY = this.startY;
		int newX = this.object.posX;
		int newY = this.object.posY;

		if (this.object.orientation.equals("top-left")) {
			//nuffin
		}
		if (this.object.orientation.equals("mid-left")) {
			if (g == 2) { //top grabbed
				sY = this.startY - (this.startHeight / 2);
			}
			if (g == 3) { //bottom grabbed
				sY = this.startY - (this.startHeight / 2);
			}
		}
		if (this.object.orientation.equals("bottom-left")) {
			if (g == 2) { //top grabbed
				sY = this.startY - this.startHeight;
			}
			if (g == 3) { //bottom grabbed
				sY = this.startY - this.startHeight;
			}
		}
		if (this.object.orientation.equals("top-centered")) {
			if (g == 0) { //left grabbed
				sX = this.startX - (this.startWidth / 2);
			}
			if (g == 1) { //right grabbed
				sX = this.startX - (this.startWidth / 2);
			}
		}
		if (this.object.orientation.equals("mid-centered")) {
			if (g == 0) { //left grabbed
				sX = this.startX - (this.startWidth / 2);
			}
			if (g == 1) { //right grabbed
				sX = this.startX - (this.startWidth / 2);
			}
			if (g == 2) { //top grabbed
				sY = this.startY - (this.startHeight / 2);
			}
			if (g == 3) { //bottom grabbed
				sY = this.startY - (this.startHeight / 2);
			}
		}
		if (this.object.orientation.equals("bottom-centered")) {
			if (g == 0) { //left grabbed
				sX = this.startX - (this.startWidth / 2);
			}
			if (g == 1) { //right grabbed
				sX = this.startX - (this.startWidth / 2);
			}
			if (g == 2) { //top grabbed
				sY = this.startY - this.startHeight;
			}
			if (g == 3) { //bottom grabbed
				sY = this.startY - this.startHeight;
			}
		}
		if (this.object.orientation.equals("top-right")) {
			if (g == 0) { //left grabbed
				sX = this.startX - this.startWidth;
			}
			if (g == 1) { //right grabbed
				sX = this.startX - this.startWidth;
			}
		}
		if (this.object.orientation.equals("mid-right")) {
			if (g == 0) { //left grabbed
				sX = this.startX - this.startWidth;
			}
			if (g == 1) { //right grabbed
				sX = this.startX - this.startWidth;
			}
			if (g == 2) { //top grabbed
				sY = this.startY - (this.startHeight / 2);
			}
			if (g == 3) { //bottom grabbed
				sY = this.startY - (this.startHeight / 2);
			}
		}
		if (this.object.orientation.equals("bottom-right")) {
			if (g == 0) { //left grabbed
				sX = this.startX - this.startWidth;
			}
			if (g == 1) { //right grabbed
				sX = this.startX - this.startWidth;
			}
			if (g == 2) { //top grabbed
				sY = this.startY - this.startHeight;
			}
			if (g == 3) { //bottom grabbed
				sY = this.startY - this.startHeight;
			}
		}
		if (this.object.orientation.equals("loading-progress")) {
			if ((this.object.orientationElement != null) && (this.object.orientationElement instanceof CustomBarCustomizationItemBase)) {
				if (g == 0) { //left grabbed
					sX = this.startX + ((CustomBarCustomizationItemBase) this.object.orientationElement).progressEndX;
				}
				if (g == 1) { //right grabbed
					sX = this.startX + ((CustomBarCustomizationItemBase) this.object.orientationElement).progressEndX;
				}
				if (g == 2) { //top grabbed
					sY = this.startY + ((CustomBarCustomizationItemBase) this.object.orientationElement).progressEndY;
				}
				if (g == 3) { //bottom grabbed
					sY = this.startY + ((CustomBarCustomizationItemBase) this.object.orientationElement).progressEndY;
				}
			}
		}
		
		//X difference
		if (mouseX > sX) {
			diffX = Math.abs(mouseX - sX);
		} else {
			diffX = Math.negateExact(sX - mouseX);
		}
		//Y difference
		if (mouseY > sY) {
			diffY = Math.abs(mouseY - sY);
		} else {
			diffY = Math.negateExact(sY - mouseY);
		}

		if (this.object.orientation.equals("top-left")) {
			if (g == 0) { //left grabbed
				newX = sX + diffX;
			}
			if (g == 2) { //top grabbed
				newY = sY + diffY;
			}
		}
		if (this.object.orientation.equals("mid-left")) {
			if (g == 0) { //left grabbed
				newX = sX + diffX;
			}
			if (g == 2) { //top grabbed
				newY = sY + diffY;
				newY = newY + (int)(((double)this.startHeight / 2.0D) - ((double)diffY / 2.0D));
			}
			if (g == 3) { //bottom grabbed
				int dY = diffY - this.startHeight;
				newY = sY;
				newY = newY + (int)(((double)this.startHeight / 2.0D) + ((double)dY / 2.0D));
			}
		}
		if (this.object.orientation.equals("bottom-left")) {
			if (g == 0) { //left grabbed
				newX = sX + diffX;
			}
			if (g == 3) { //bottom grabbed
				newY = sY + diffY;
			}
		}
		if (this.object.orientation.equals("top-centered")) {
			if (g == 0) { //left grabbed
				newX = sX + diffX;
				newX = newX + (int)(((double)this.startWidth / 2.0D) - ((double)diffX / 2.0D));
			}
			if (g == 1) { //right grabbed
				int dX = diffX - this.startWidth;
				newX = sX;
				newX = newX + (int)(((double)this.startWidth / 2.0D) + ((double)dX / 2.0D));
			}
			if (g == 2) { //top grabbed
				newY = sY + diffY;
			}
		}
		if (this.object.orientation.equals("mid-centered")) {
			if (g == 0) { //left grabbed
				newX = sX + diffX;
				newX = newX + (int)(((double)this.startWidth / 2.0D) - ((double)diffX / 2.0D));
			}
			if (g == 1) { //right grabbed
				int dX = diffX - this.startWidth;
				newX = sX;
				newX = newX + (int)(((double)this.startWidth / 2.0D) + ((double)dX / 2.0D));
			}
			if (g == 2) { //top grabbed
				newY = sY + diffY;
				newY = newY + (int)(((double)this.startHeight / 2.0D) - ((double)diffY / 2.0D));
			}
			if (g == 3) { //bottom grabbed
				int dY = diffY - this.startHeight;
				newY = sY;
				newY = newY + (int)(((double)this.startHeight / 2.0D) + ((double)dY / 2.0D));
			}
		}
		if (this.object.orientation.equals("bottom-centered")) {
			if (g == 0) { //left grabbed
				newX = sX + diffX;
				newX = newX + (int)(((double)this.startWidth / 2.0D) - ((double)diffX / 2.0D));
			}
			if (g == 1) { //right grabbed
				int dX = diffX - this.startWidth;
				newX = sX;
				newX = newX + (int)(((double)this.startWidth / 2.0D) + ((double)dX / 2.0D));
			}
			if (g == 3) { //bottom grabbed
				newY = sY + diffY;
			}
		}
		if (this.object.orientation.equals("top-right")) {
			if (g == 1) { //right grabbed
				newX = sX + diffX;
			}
			if (g == 2) { //top grabbed
				newY = sY + diffY;
			}
		}
		if (this.object.orientation.equals("mid-right")) {
			if (g == 1) { //right grabbed
				newX = sX + diffX;
			}
			if (g == 2) { //top grabbed
				newY = sY + diffY;
				newY = newY + (int)(((double)this.startHeight / 2.0D) - ((double)diffY / 2.0D));
			}
			if (g == 3) { //bottom grabbed
				int dY = diffY - this.startHeight;
				newY = sY;
				newY = newY + (int)(((double)this.startHeight / 2.0D) + ((double)dY / 2.0D));
			}
		}
		if (this.object.orientation.equals("bottom-right")) {
			if (g == 1) { //right grabbed
				newX = sX + diffX;
			}
			if (g == 3) { //bottom grabbed
				newY = sY + diffY;
			}
		}
		if (this.object.orientation.equals("loading-progress")) {
			if ((this.object.orientationElement != null) && (this.object.orientationElement instanceof CustomBarCustomizationItemBase)) {
				if (g == 0) { //left grabbed
					newX = sX + diffX - ((CustomBarCustomizationItemBase) this.object.orientationElement).progressEndX;
				}
				if (g == 2) { //top grabbed
					newY = sY + diffY - ((CustomBarCustomizationItemBase) this.object.orientationElement).progressEndY;
				}
			}
		}

		if (!this.stretchX) {
			if (g == 0) { //left
				int w = this.startWidth + this.getOpponentInt(diffX);
				if (w >= 1) {
					
					//ori
					this.object.posX = newX;
					
					this.object.width = w;
					if (isShiftPressed) {
						int h = this.getAspectHeight(this.startWidth, this.startHeight, w);
						if (h >= 1) {
							this.object.height = h;
						}
					}
				}
			}
			if (g == 1) { //right
				int w = this.object.width + (diffX - this.object.width);
				if (w >= 1) {
					
					this.object.posX = newX;
					
					this.object.width = w;
					if (isShiftPressed) {
						int h = this.getAspectHeight(this.startWidth, this.startHeight, w);
						if (h >= 1) {
							this.object.height = h;
						}
					}
				}
			}
		}

		if (!this.stretchY) {
			if (g == 2) { //top
				int h = this.startHeight + this.getOpponentInt(diffY);
				if (h >= 1) {
					
					//ori
					this.object.posY = newY;
					
					this.object.height = h;
					if (isShiftPressed) {
						int w = this.getAspectWidth(this.startWidth, this.startHeight, h);
						if (w >= 1) {
							this.object.width = w;
						}
					}
				}
			}
			if (g == 3) { //bottom
				int h = this.object.height + (diffY - this.object.height);
				if (h >= 1) {
					
					this.object.posY = newY;
					
					this.object.height = h;
					if (isShiftPressed) {
						int w = this.getAspectWidth(this.startWidth, this.startHeight, h);
						if (w >= 1) {
							this.object.width = w;
						}
					}
				}
			}
		}
	}
	
	private int getOpponentInt(int i) {
		if (Math.abs(i) == i) {
			return Math.negateExact(i);
		} else {
			return Math.abs(i);
		}
	}
	
	protected void updateHovered(int mouseX, int mouseY) {
		if ((mouseX >= this.object.getPosX()) && (mouseX <= this.object.getPosX() + this.object.width) && (mouseY >= this.object.getPosY()) && mouseY <= this.object.getPosY() + this.object.height) {
			this.hovered = true;
		} else {
			this.hovered = false;
		}
	}
	
	public boolean isDragged() {
		return this.dragging;
	}

	public boolean isGettingResized() {
		return this.resizing;
	}
	
	public boolean isLeftClicked() {
		return (this.isHoveredOrFocused() && MouseInput.isLeftMouseDown());
	}
	
	public boolean isRightClicked() {
		return (this.isHoveredOrFocused() && MouseInput.isRightMouseDown());
	}
	
	public boolean isHoveredOrFocused() {
		return this.hovered;
	}
	
	/**
	 * Sets the BASE position of this object (NOT the absolute position!)
	 */
	public void setX(int x) {
		this.object.posX = x;
	}
	
	/**
	 * Sets the BASE position of this object (NOT the absolute position!)
	 */
	public void setY(int y) {
		this.object.posY = y;
	}
	
	/**
	 * Returns the ABSOLUTE position of this object (NOT the base position!)
	 */
	public int getX() {
		return this.object.getPosX();
	}
	
	/**
	 * Returns the ABSOLUTE position of this object (NOT the base position!)
	 */
	public int getY() {
		return this.object.getPosY();
	}
	
	public void setWidth(int width) {
		this.object.width = width;
	}
	
	public void setHeight(int height) {
		this.object.height = height;
	}
	
	public int getWidth() {
		return this.object.width;
	}
	
	public int getHeight() {
		return this.object.height;
	}
	
	public boolean isDestroyable() {
		return this.destroyable;
	}

	public boolean isStretchable() {
		return this.stretchable;
	}

	public void destroyObject() {
		if (!this.destroyable) {
			return;
		}
		if (DrippyLoadingScreen.config.getOrDefault("editordeleteconfirmation", true)) {
			PopupHandler.displayPopup(new FHYesNoPopup(300, new Color(0, 0, 0, 0), 240, (call) -> {
				if (call) {
					this.handler.deleteContentQueue.add(this);
				}
			}, "§c§l" + Locals.localize("drippyloadingscreen.helper.creator.messages.sure"), "", Locals.localize("drippyloadingscreen.helper.creator.deleteobject"), "", "", "", "", ""));
		} else {
			this.handler.deleteContentQueue.add(this);
		}
	}

	public void resetObjectStates() {
		hovered = false;
		dragging = false;
		resizing = false;
		activeGrabber = -1;
		if (this.rightclickMenu != null) {
			this.rightclickMenu.closeMenu();
		}
		this.handler.setFocusChangeBlocked(objectId, false);
		this.handler.setObjectFocused(this, false, true);
	}
	
	public boolean isVanillaElement() {
		if (this.object != null) {
			return (this.object instanceof VanillaSplashCustomizationItemBase);
		}
		return false;
	}

	public abstract List<PropertiesSection> getProperties();

	protected void addVisibilityPropertiesTo(PropertiesSection sec) {

		VisibilityRequirementContainer c = this.object.visibilityRequirementContainer;

		if (c.vrCheckForActiveSlot) {
			sec.addEntry("vr:showif:activeslot", "" + c.vrShowIfActiveSlot);
			sec.addEntry("vr:value:activeslot", "" + c.vrActiveSlot);
		}
		if (c.vrCheckForItemInMainHand) {
			sec.addEntry("vr:showif:iteminmainhand", "" + c.vrShowIfItemInMainHand);
		}
		if (c.vrCheckForItemInOffHand) {
			sec.addEntry("vr:showif:iteminoffhand", "" + c.vrShowIfItemInOffHand);
		}
		if (c.vrCheckForActiveItemType) {
			sec.addEntry("vr:showif:activeitemtype", "" + c.vrShowIfActiveItemType);
			sec.addEntry("vr:value:activeitemtype", "" + c.vrActiveItemType);
		}
		if (c.vrCheckForActiveItemName) {
			sec.addEntry("vr:showif:activeitemname", "" + c.vrShowIfActiveItemName);
			sec.addEntry("vr:value:activeitemname", "" + c.vrActiveItemName);
		}
		if (c.vrCheckForSingleplayer) {
			sec.addEntry("vr:showif:singleplayer", "" + c.vrShowIfSingleplayer);
		}
		if (c.vrCheckForMultiplayer) {
			sec.addEntry("vr:showif:multiplayer", "" + c.vrShowIfMultiplayer);
		}
		if (c.vrCheckForPlayerOnGround) {
			sec.addEntry("vr:showif:playeronground", "" + c.vrShowIfPlayerOnGround);
		}
		if (c.vrCheckForPlayerUnderwater) {
			sec.addEntry("vr:showif:playerunderwater", "" + c.vrShowIfPlayerUnderwater);
		}
		if (c.vrCheckForPlayerIsRidingHorse) {
			sec.addEntry("vr:showif:playerisridinghorse", "" + c.vrShowIfPlayerIsRidingHorse);
		}
		if (c.vrCheckForPlayerIsRidingEntity) {
			sec.addEntry("vr:showif:playerisridingentity", "" + c.vrShowIfPlayerIsRidingEntity);
		}
		if (c.vrCheckForPlayerIsInWater) {
			sec.addEntry("vr:showif:playerisinwater", "" + c.vrShowIfPlayerIsInWater);
		}
		if (c.vrCheckForPlayerIsRunning) {
			sec.addEntry("vr:showif:playerisrunning", "" + c.vrShowIfPlayerIsRunning);
		}
		if (c.vrCheckForSlotItemName) {
			sec.addEntry("vr:showif:slotitemname", "" + c.vrShowIfSlotItemName);
			sec.addEntry("vr:value:slotitemname", c.vrSlotItemNameSlot + ":" + c.vrSlotItemName);
		}
		if (c.vrCheckForDebugOpen) {
			sec.addEntry("vr:showif:debugopen", "" + c.vrShowIfDebugOpen);
		}
		if (c.vrCheckForGamePaused) {
			sec.addEntry("vr:showif:gamepaused", "" + c.vrShowIfGamePaused);
		}
		if (c.vrCheckForRaining) {
			sec.addEntry("vr:showif:raining", "" + c.vrShowIfRaining);
		}
		if (c.vrCheckForThundering) {
			sec.addEntry("vr:showif:thundering", "" + c.vrShowIfThundering);
		}
		if (c.vrCheckForHealthLowerThan) {
			sec.addEntry("vr:showif:healthlowerthan", "" + c.vrShowIfHealthLowerThan);
			sec.addEntry("vr:value:healthlowerthan", "" + c.vrHealthLowerThan);
		}
		if (c.vrCheckForHealthLowerThanPercent) {
			sec.addEntry("vr:showif:healthlowerthanpercent", "" + c.vrShowIfHealthLowerThanPercent);
			sec.addEntry("vr:value:healthlowerthanpercent", "" + c.vrHealthLowerThanPercent);
		}
		if (c.vrCheckForFoodLowerThan) {
			sec.addEntry("vr:showif:foodlowerthan", "" + c.vrShowIfFoodLowerThan);
			sec.addEntry("vr:value:foodlowerthan", "" + c.vrFoodLowerThan);
		}
		if (c.vrCheckForFoodLowerThanPercent) {
			sec.addEntry("vr:showif:foodlowerthanpercent", "" + c.vrShowIfFoodLowerThanPercent);
			sec.addEntry("vr:value:foodlowerthanpercent", "" + c.vrFoodLowerThanPercent);
		}
		if (c.vrCheckForWithered) {
			sec.addEntry("vr:showif:withered", "" + c.vrShowIfWithered);
		}
		if (c.vrCheckForSurvival) {
			sec.addEntry("vr:showif:survival", "" + c.vrShowIfSurvival);
		}
		if (c.vrCheckForCreative) {
			sec.addEntry("vr:showif:creative", "" + c.vrShowIfCreative);
		}
		if (c.vrCheckForAdventure) {
			sec.addEntry("vr:showif:adventure", "" + c.vrShowIfAdventure);
		}
		if (c.vrCheckForSpectator) {
			sec.addEntry("vr:showif:spectator", "" + c.vrShowIfSpectator);
		}
		if (c.vrCheckForPoisoned) {
			sec.addEntry("vr:showif:poisoned", "" + c.vrShowIfPoisoned);
		}
		if (c.vrCheckForBadStomach) {
			sec.addEntry("vr:showif:badstomach", "" + c.vrShowIfBadStomach);
		}
		if (c.vrCheckForWorldTimeHour) {
			sec.addEntry("vr:showif:worldtimehour", "" + c.vrShowIfWorldTimeHour);
			String worldTimeHourVal = "";
			for (int i : c.vrWorldTimeHour) {
				worldTimeHourVal += i + ",";
			}
			if (worldTimeHourVal.length() > 0) {
				worldTimeHourVal = worldTimeHourVal.substring(0, worldTimeHourVal.length() -1);
			}
			if (worldTimeHourVal.length() > 0) {
				sec.addEntry("vr:showif:worldtimehour", "" + c.vrShowIfWorldTimeHour);
				sec.addEntry("vr:value:worldtimehour", worldTimeHourVal);
			}
		}
		if (c.vrCheckForWorldTimeMinute) {
			sec.addEntry("vr:showif:worldtimeminute", "" + c.vrShowIfWorldTimeMinute);
			String worldTimeMinuteVal = "";
			for (int i : c.vrWorldTimeMinute) {
				worldTimeMinuteVal += i + ",";
			}
			if (worldTimeMinuteVal.length() > 0) {
				worldTimeMinuteVal = worldTimeMinuteVal.substring(0, worldTimeMinuteVal.length() -1);
			}
			if (worldTimeMinuteVal.length() > 0) {
				sec.addEntry("vr:showif:worldtimeminute", "" + c.vrShowIfWorldTimeMinute);
				sec.addEntry("vr:value:worldtimeminute", worldTimeMinuteVal);
			}
		}
		if (c.vrCheckForRealTimeHour) {
			sec.addEntry("vr:showif:realtimehour", "" + c.vrShowIfRealTimeHour);
			String val = "";
			for (int i : c.vrRealTimeHour) {
				val += i + ",";
			}
			if (val.length() > 0) {
				val = val.substring(0, val.length() -1);
			}
			if (val.length() > 0) {
				sec.addEntry("vr:showif:realtimehour", "" + c.vrShowIfRealTimeHour);
				sec.addEntry("vr:value:realtimehour", val);
			}
		}
		if (c.vrCheckForRealTimeMinute) {
			sec.addEntry("vr:showif:realtimeminute", "" + c.vrShowIfRealTimeMinute);
			String val = "";
			for (int i : c.vrRealTimeMinute) {
				val += i + ",";
			}
			if (val.length() > 0) {
				val = val.substring(0, val.length() -1);
			}
			if (val.length() > 0) {
				sec.addEntry("vr:showif:realtimeminute", "" + c.vrShowIfRealTimeMinute);
				sec.addEntry("vr:value:realtimeminute", val);
			}
		}
		if (c.vrCheckForRealTimeSecond) {
			sec.addEntry("vr:showif:realtimesecond", "" + c.vrShowIfRealTimeSecond);
			String val = "";
			for (int i : c.vrRealTimeSecond) {
				val += i + ",";
			}
			if (val.length() > 0) {
				val = val.substring(0, val.length() -1);
			}
			if (val.length() > 0) {
				sec.addEntry("vr:showif:realtimesecond", "" + c.vrShowIfRealTimeSecond);
				sec.addEntry("vr:value:realtimesecond", val);
			}
		}

	}

}
