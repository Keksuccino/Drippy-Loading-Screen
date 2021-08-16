package de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.custombars;

import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.LayoutElement;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.ChooseFilePopup;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.FHTextInputPopup;
import de.keksuccino.drippyloadingscreen.customization.items.custombars.CustomBarCustomizationItemBase;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.rendering.RenderUtils;
import de.keksuccino.drippyloadingscreen.customization.items.custombars.CustomBarCustomizationItemBase.BarDirection;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LayoutCustomBarBase extends LayoutElement {

	public LayoutCustomBarBase(CustomBarCustomizationItemBase object, LayoutEditorScreen handler) {
		super(object, true, handler);
	}
	
	@Override
	public void init() {
		this.stretchable = true;
		super.init();
		
		this.rightclickMenu.setAutoclose(true);
		
		/** BAR COLOR **/
		AdvancedButton barColorButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("drippyloadingscreen.helper.creator.add.custombars.barcolor"), true, (press) -> {
			FHTextInputPopup pop = new FHTextInputPopup(new Color(0, 0, 0, 0), Locals.localize("drippyloadingscreen.helper.creator.add.custombars.barcolor"), null, 240, (call) -> {
				if (call != null) {
					if (!call.equals(this.getObject().barColorHex)) {
						this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
					}
					Color c = RenderUtils.getColorFromHexString(this.getObject().barColorHex);
					if (c != null) {
						this.getObject().barColorHex = call;
						this.getObject().updateItem();
					} else {
						//TODO error message anzeigen
					}
				}
			});
			if (this.getObject().barColorHex != null) {
				pop.setText(this.getObject().barColorHex);
			}
			PopupHandler.displayPopup(pop);
		});
		this.rightclickMenu.addContent(barColorButton);
		
		/** BACKGROUND COLOR **/
		AdvancedButton backgroundColorButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("drippyloadingscreen.helper.creator.add.custombars.backgroundcolor"), true, (press) -> {
			FHTextInputPopup pop = new FHTextInputPopup(new Color(0, 0, 0, 0), Locals.localize("drippyloadingscreen.helper.creator.add.custombars.backgroundcolor"), null, 240, (call) -> {
				if (call != null) {
					if (!call.equals(this.getObject().backgroundColorHex)) {
						this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
					}
					Color c = RenderUtils.getColorFromHexString(this.getObject().backgroundColorHex);
					if (c != null) {
						this.getObject().backgroundColorHex = call;
						this.getObject().updateItem();
					} else {
						//TODO error message anzeigen
					}
				}
			});
			if (this.getObject().backgroundColorHex != null) {
				pop.setText(this.getObject().backgroundColorHex);
			}
			PopupHandler.displayPopup(pop);
		});
		this.rightclickMenu.addContent(backgroundColorButton);
		
		this.rightclickMenu.addSeparator();
		
		/** BAR TEXTURE **/
		AdvancedButton barTextureButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("drippyloadingscreen.helper.creator.add.custombars.bartexture"), true, (press) -> {
			ChooseFilePopup pop = new ChooseFilePopup((call) -> {
				if (call != null) {
					if (!call.equals(this.getObject().barTexturePath)) {
						this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
					}
					if (call.replace(" ", "").equals("")) {
						this.getObject().barTexturePath = null;
						this.getObject().updateItem();
					} else {
						File f = new File(call);
						if (f.exists() && f.isFile() && (f.getPath().toLowerCase().endsWith(".jpg") || f.getPath().toLowerCase().endsWith(".jpeg") || f.getPath().toLowerCase().endsWith(".png"))) {
							this.getObject().barTexturePath = call;
							this.getObject().updateItem();
						} else {
							//TODO error popup
						}
					}
				}
			}, "jpg", "jpeg", "png");
			if (this.getObject().barTexturePath != null) {
				pop.setText(this.getObject().barTexturePath);
			}
			PopupHandler.displayPopup(pop);
		});
		barTextureButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.creator.add.custombars.bartexture.btn.desc"), "%n%"));
		this.rightclickMenu.addContent(barTextureButton);
		
		/** BACKGROUND TEXTURE **/
		AdvancedButton backgroundTextureButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("drippyloadingscreen.helper.creator.add.custombars.backgroundtexture"), true, (press) -> {
			ChooseFilePopup pop = new ChooseFilePopup((call) -> {
				if (call != null) {
					if (!call.equals(this.getObject().backgroundTexturePath)) {
						this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
					}
					if (call.replace(" ", "").equals("")) {
						this.getObject().backgroundTexturePath = null;
						this.getObject().updateItem();
					} else {
						File f = new File(call);
						if (f.exists() && f.isFile() && (f.getPath().toLowerCase().endsWith(".jpg") || f.getPath().toLowerCase().endsWith(".jpeg") || f.getPath().toLowerCase().endsWith(".png"))) {
							this.getObject().backgroundTexturePath = call;
							this.getObject().updateItem();
						} else {
							//TODO error popup
						}
					}
				}
			}, "jpg", "jpeg", "png");
			if (this.getObject().backgroundTexturePath != null) {
				pop.setText(this.getObject().backgroundTexturePath);
			}
			PopupHandler.displayPopup(pop);
		});
		backgroundTextureButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.creator.add.custombars.backgroundtexture.btn.desc"), "%n%"));
		this.rightclickMenu.addContent(backgroundTextureButton);
		
		this.rightclickMenu.addSeparator();
		
		/** DIRECTION **/
		String barDirString = Locals.localize("drippyloadingscreen.helper.creator.add.custombars.direction.left");
		CustomBarCustomizationItemBase.BarDirection d = this.getObject().direction;
		if (d == BarDirection.RIGHT) {
			barDirString = Locals.localize("drippyloadingscreen.helper.creator.add.custombars.direction.right");
		}
		if (d == BarDirection.UP) {
			barDirString = Locals.localize("drippyloadingscreen.helper.creator.add.custombars.direction.up");
		}
		if (d == BarDirection.DOWN) {
			barDirString = Locals.localize("drippyloadingscreen.helper.creator.add.custombars.direction.down");
		}
		AdvancedButton barDirectionButton = new AdvancedButton(0, 0, 0, 16, barDirString, true, (press) -> {
			BarDirection bd = this.getObject().direction;
			AdvancedButton b = (AdvancedButton) press;
			if (bd == BarDirection.LEFT) {
				this.getObject().direction = BarDirection.RIGHT;
				b.setMessage(Locals.localize("drippyloadingscreen.helper.creator.add.custombars.direction.right"));
			} else if (bd == BarDirection.RIGHT) {
				this.getObject().direction = BarDirection.UP;
				b.setMessage(Locals.localize("drippyloadingscreen.helper.creator.add.custombars.direction.up"));
			} else if (bd == BarDirection.UP) {
				this.getObject().direction = BarDirection.DOWN;
				b.setMessage(Locals.localize("drippyloadingscreen.helper.creator.add.custombars.direction.down"));
			} else if (bd == BarDirection.DOWN) {
				this.getObject().direction = BarDirection.LEFT;
				b.setMessage(Locals.localize("drippyloadingscreen.helper.creator.add.custombars.direction.left"));
			}
		});
		this.rightclickMenu.addContent(barDirectionButton);
		
		
		
	}

	@Override
	public List<PropertiesSection> getProperties() {
		List<PropertiesSection> l = new ArrayList<PropertiesSection>();
		l.add(this.getPropertiesRaw());
		return l;
	}
	
	protected PropertiesSection getPropertiesRaw() {
		
		PropertiesSection s = new PropertiesSection("customization");

		if (this.stretchX) {
			s.addEntry("x", "0");
			s.addEntry("width", "%guiwidth%");
		} else {
			s.addEntry("x", "" + this.object.posX);
			s.addEntry("width", "" + this.object.width);
		}
		if (this.stretchY) {
			s.addEntry("y", "0");
			s.addEntry("height", "%guiheight%");
		} else {
			s.addEntry("y", "" + this.object.posY);
			s.addEntry("height", "" + this.object.height);
		}
		s.addEntry("orientation", this.getObject().orientation);
		
		s.addEntry("direction", this.getObject().direction.getName());
		
		if (this.getObject().barColorHex != null) {
			s.addEntry("barcolor", this.getObject().barColorHex);
		}
		if (this.getObject().backgroundColorHex != null) {
			s.addEntry("backgroundcolor", this.getObject().backgroundColorHex);
		}
		if (this.getObject().barTexturePath != null) {
			s.addEntry("bartexture", this.getObject().barTexturePath);
		}
		if (this.getObject().backgroundTexturePath != null) {
			s.addEntry("backgroundtexture", this.getObject().backgroundTexturePath);
		}
		if (this.getObject().barEndTexturePath != null) {
			s.addEntry("barendtexture", this.getObject().barEndTexturePath);
			s.addEntry("barendtexturewidth", "" + this.getObject().barEndTextureWidth);
			s.addEntry("barendtextureheight", "" + this.getObject().barEndTextureHeight);
		}
		
		return s;
		
	}
	
	public CustomBarCustomizationItemBase getObject() {
		return (CustomBarCustomizationItemBase) this.object;
	}

}
