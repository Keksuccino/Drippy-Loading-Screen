package de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.string;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.LayoutElement;
import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.content.FHContextMenu;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.FHTextInputPopup;
import de.keksuccino.drippyloadingscreen.customization.items.CustomizationItemBase.Alignment;
import de.keksuccino.drippyloadingscreen.customization.items.WebStringCustomizationItem;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.CharacterFilter;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.rendering.RenderUtils;


public class LayoutWebString extends LayoutElement {

	protected AdvancedButton alignmentLeftBtn;
	protected AdvancedButton alignmentRightBtn;
	protected AdvancedButton alignmentCenteredBtn;
	
	public LayoutWebString(WebStringCustomizationItem parent, LayoutEditorScreen handler) {
		super(parent, true, handler);
		this.setScale(this.getStringScale());
	}
	
	@Override
	public void init() {
		super.init();
		
		AdvancedButton scaleB = new AdvancedButton(0, 0, 0, 16, Locals.localize("drippyloadingscreen.helper.creator.items.string.setscale"), true, (press) -> {
			PopupHandler.displayPopup(new FHTextInputPopup(new Color(0, 0, 0, 0), "§l" + Locals.localize("drippyloadingscreen.helper.creator.items.string.setscale") + ":", CharacterFilter.getDoubleCharacterFiler(), 240, this::setScaleCallback));
		});
		this.rightclickMenu.addContent(scaleB);
		
		String sLabel = Locals.localize("drippyloadingscreen.helper.creator.items.string.setshadow");
		if (this.getObject().shadow) {
			sLabel = Locals.localize("drippyloadingscreen.helper.creator.items.string.setnoshadow");
		}
		AdvancedButton shadowB = new AdvancedButton(0, 0, 0, 16, sLabel, true, (press) -> {
			if (this.getObject().shadow) {
				((AdvancedButton)press).setMessage(Locals.localize("drippyloadingscreen.helper.creator.items.string.setshadow"));
				this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
				
				this.getObject().shadow = false;
			} else {
				((AdvancedButton)press).setMessage(Locals.localize("drippyloadingscreen.helper.creator.items.string.setnoshadow"));
				this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
				
				this.getObject().shadow = true;
			}
		});
		this.rightclickMenu.addContent(shadowB);

		/** TEXT COLOR **/
		AdvancedButton textColorButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.editor.elements.text.color"), (press) -> {
			FHTextInputPopup pop = new FHTextInputPopup(new Color(0, 0, 0, 0), Locals.localize("drippyloadingscreen.helper.editor.elements.text.color"), null, 240, (call) -> {
				if (call != null) {
					if (!call.equals(this.getObject().textColorHex)) {
						this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
					}
					this.getObject().textColorHex = call;
					this.getObject().textColor = RenderUtils.getColorFromHexString(call);
				}
			});
			if (this.getObject().textColorHex != null) {
				pop.setText(this.getObject().textColorHex);
			}
			PopupHandler.displayPopup(pop);
		});
		textColorButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.editor.elements.text.color.btn.desc"), "%n%"));
		this.rightclickMenu.addContent(textColorButton);

		FHContextMenu alignmentMenu = new FHContextMenu();
		alignmentMenu.setAutoclose(true);
		this.rightclickMenu.addChild(alignmentMenu);

		String al = Locals.localize("drippyloadingscreen.helper.creator.items.string.alignment.left");
		if (this.getObject().alignment == Alignment.LEFT) {
			al = "§a" + al;
		}
		alignmentLeftBtn = new AdvancedButton(0, 0, 0, 0, al, true, (press) -> {
			this.getObject().alignment = Alignment.LEFT;
			((AdvancedButton)press).setMessage("§a" + Locals.localize("drippyloadingscreen.helper.creator.items.string.alignment.left"));
			alignmentRightBtn.setMessage(Locals.localize("drippyloadingscreen.helper.creator.items.string.alignment.right"));
			alignmentCenteredBtn.setMessage(Locals.localize("drippyloadingscreen.helper.creator.items.string.alignment.centered"));
		});
		alignmentMenu.addContent(alignmentLeftBtn);

		String ar = Locals.localize("drippyloadingscreen.helper.creator.items.string.alignment.right");
		if (this.getObject().alignment == Alignment.RIGHT) {
			ar = "§a" + ar;
		}
		alignmentRightBtn = new AdvancedButton(0, 0, 0, 0, ar, true, (press) -> {
			this.getObject().alignment = Alignment.RIGHT;
			((AdvancedButton)press).setMessage("§a" + Locals.localize("drippyloadingscreen.helper.creator.items.string.alignment.right"));
			alignmentLeftBtn.setMessage(Locals.localize("drippyloadingscreen.helper.creator.items.string.alignment.left"));
			alignmentCenteredBtn.setMessage(Locals.localize("drippyloadingscreen.helper.creator.items.string.alignment.centered"));
		});
		alignmentMenu.addContent(alignmentRightBtn);

		String ac = Locals.localize("drippyloadingscreen.helper.creator.items.string.alignment.centered");
		if (this.getObject().alignment == Alignment.CENTERED) {
			ac = "§a" + ac;
		}
		alignmentCenteredBtn = new AdvancedButton(0, 0, 0, 0, ac, true, (press) -> {
			this.getObject().alignment = Alignment.CENTERED;
			((AdvancedButton)press).setMessage("§a" + Locals.localize("drippyloadingscreen.helper.creator.items.string.alignment.centered"));
			alignmentRightBtn.setMessage(Locals.localize("drippyloadingscreen.helper.creator.items.string.alignment.right"));
			alignmentLeftBtn.setMessage(Locals.localize("drippyloadingscreen.helper.creator.items.string.alignment.left"));
		});
		alignmentMenu.addContent(alignmentCenteredBtn);

		AdvancedButton alignmentBtn = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.creator.items.string.alignment"), true, (press) -> {
			alignmentMenu.setParentButton((AdvancedButton) press);
			alignmentMenu.openMenuAt(0, press.y);
		});
		alignmentBtn.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.creator.items.string.alignment.desc"), "%n%"));
		this.rightclickMenu.addContent(alignmentBtn);
		
		String mLabel = Locals.localize("drippyloadingscreen.helper.creator.webstring.multiline");
		if (this.getObject().multiline) {
			mLabel = Locals.localize("drippyloadingscreen.helper.creator.webstring.nomultiline");
		}
		AdvancedButton multilineB = new AdvancedButton(0, 0, 0, 16, mLabel, true, (press) -> {
			if (this.getObject().multiline) {
				((AdvancedButton)press).setMessage(Locals.localize("drippyloadingscreen.helper.creator.webstring.multiline"));
				this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
				
				this.getObject().multiline = false;
				this.getObject().updateContent(this.getObject().value);
			} else {
				((AdvancedButton)press).setMessage(Locals.localize("drippyloadingscreen.helper.creator.webstring.nomultiline"));
				this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
				
				this.getObject().multiline = true;
				this.getObject().updateContent(this.getObject().value);
			}
		});
		this.rightclickMenu.addContent(multilineB);
		
	}
	
	private float getStringScale() {
		return ((WebStringCustomizationItem)this.object).scale;
	}
	
	private WebStringCustomizationItem getObject() {
		return ((WebStringCustomizationItem)this.object);
	}
	
	@Override
	public boolean isGrabberPressed() {
		return false;
	}
	
	@Override
	public int getActiveResizeGrabber() {
		return -1;
	}
	
	public void setScale(float scale) {
		if (this.getObject().scale != scale) {
			this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
		}
		
		((WebStringCustomizationItem)this.object).scale = scale;
	}

	public void updateContent(String url) {
		this.getObject().updateContent(url);
	}
	
	private void setScaleCallback(String scale) {
		if (scale == null) {
			return;
		}
		if (MathUtils.isFloat(scale)) {
			this.setScale(Float.valueOf(scale));
		} else {
			LayoutEditorScreen.displayNotification("§c§l" + Locals.localize("drippyloadingscreen.helper.creator.items.string.scale.invalidvalue.title"), "", Locals.localize("drippyloadingscreen.helper.creator.items.string.scale.invalidvalue.desc"), "", "", "", "", "");
		}
	}
	
	@Override
	public List<PropertiesSection> getProperties() {
		List<PropertiesSection> l = new ArrayList<PropertiesSection>();
		
		PropertiesSection p1 = new PropertiesSection("customization");
		p1.addEntry("action", "addwebtext");
		p1.addEntry("actionid", this.object.getActionId());
		if (this.object.delayAppearance) {
			p1.addEntry("delayappearance", "true");
			p1.addEntry("delayappearanceeverytime", "" + this.object.delayAppearanceEverytime);
			p1.addEntry("delayappearanceseconds", "" + this.object.delayAppearanceSec);
			if (this.object.fadeIn) {
				p1.addEntry("fadein", "true");
				p1.addEntry("fadeinspeed", "" + this.object.fadeInSpeed);
			}
		}
		p1.addEntry("url", ((WebStringCustomizationItem)this.object).rawURL);
		p1.addEntry("x", "" + this.object.posX);
		p1.addEntry("y", "" + this.object.posY);
		p1.addEntry("orientation", this.object.orientation);
		p1.addEntry("scale", "" + this.getObject().scale);
		p1.addEntry("shadow", "" + this.getObject().shadow);
		p1.addEntry("multiline", "" + this.getObject().multiline);
		p1.addEntry("alignment", this.getObject().alignment.key);
		p1.addEntry("textcolor", this.getObject().textColorHex);

		this.addVisibilityPropertiesTo(p1);

		l.add(p1);
		
		return l;
	}

}
