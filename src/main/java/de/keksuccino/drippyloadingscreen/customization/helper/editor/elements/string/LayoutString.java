package de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.string;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements.ForgeTextSplashElement;
import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.LayoutElement;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.DynamicValueInputPopup;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.FHTextInputPopup;
import de.keksuccino.drippyloadingscreen.customization.items.StringCustomizationItem;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.CharacterFilter;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;

public class LayoutString extends LayoutElement {

	protected AdvancedButton alignmentLeftBtn;
	protected AdvancedButton alignmentRightBtn;
	protected AdvancedButton alignmentCenteredBtn;
	
	public LayoutString(StringCustomizationItem parent, LayoutEditorScreen handler) {
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
		
		AdvancedButton editTextB = new AdvancedButton(0, 0, 0, 16, Locals.localize("drippyloadingscreen.helper.creator.items.string.edit"), true, (press) -> {
			DynamicValueInputPopup i = new DynamicValueInputPopup(new Color(0, 0, 0, 0), "§l" + Locals.localize("drippyloadingscreen.helper.creator.items.string.edit") + ":", null, 240, this::setTextCallback);
			i.setText(StringUtils.convertFormatCodes(this.object.value, "§", "&"));
			PopupHandler.displayPopup(i);
		});
		editTextB.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.editor.elements.text.onlybasicchars"), "%n%"));
		this.rightclickMenu.addContent(editTextB);
		
		this.rightclickMenu.addSeparator();

		//TODO fixen
//		/** RAINBOW MODE **/
//		String rainbowToggleString = Locals.localize("drippyloadingscreen.helper.editor.elements.string.rainbow.on");
//		if (!this.getObject().rainbowMode) {
//			rainbowToggleString = Locals.localize("drippyloadingscreen.helper.editor.elements.string.rainbow.off");
//		}
//		AdvancedButton rainbowToggleButton = new AdvancedButton(0, 0, 0, 16, rainbowToggleString, true, (press) -> {
//			if (this.getObject().rainbowMode) {
//				((AdvancedButton)press).setMessage(Locals.localize("drippyloadingscreen.helper.editor.elements.string.rainbow.off"));
//				this.getObject().rainbowMode = false;
//			} else {
//				((AdvancedButton)press).setMessage(Locals.localize("drippyloadingscreen.helper.editor.elements.string.rainbow.on"));
//				this.getObject().rainbowMode = true;
//			}
//		});
//		rainbowToggleButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.editor.elements.string.rainbow.btn.desc"), "%n%"));
//		this.rightclickMenu.addContent(rainbowToggleButton);
//
//		AdvancedButton rainbowColorButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("drippyloadingscreen.helper.editor.elements.string.rainbow.colors"), true, (press) -> {
//			PopupHandler.displayPopup(new SetRainbowStringColorPopup(this));
//		}) {
//			@Override
//			public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
//				if (!LayoutString.this.getObject().rainbowMode) {
//					this.active = false;
//				} else {
//					this.active = true;
//				}
//				super.render(matrixStack, mouseX, mouseY, partialTicks);
//			}
//		};
//		this.rightclickMenu.addContent(rainbowColorButton);
//
//		AdvancedButton rainbowFadeSpeedButton = new AdvancedButton(0, 0, 0, 16, Locals.localize("drippyloadingscreen.helper.editor.elements.string.rainbow.speed"), true, (press) -> {
//			FHTextInputPopup pop = new FHTextInputPopup(new Color(0, 0, 0, 0), Locals.localize("drippyloadingscreen.helper.editor.elements.string.rainbow.speed"), CharacterFilter.getDoubleCharacterFiler(), 240, (call) -> {
//				if (call != null) {
//					float newSpeed = 1.0F;
//					if (!call.replace(" ", "").equals("")) {
//						if (MathUtils.isFloat(call)) {
//							newSpeed = Float.parseFloat(call);
//						}
//					}
//					if (newSpeed != this.getObject().rainbowText.getSpeed()) {
//						this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
//					}
//					this.getObject().rainbowText.setSpeed(newSpeed);
//				}
//			});
//			pop.setText("" + this.getObject().rainbowText.getSpeed());
//			PopupHandler.displayPopup(pop);
//		}) {
//			@Override
//			public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
//				if (!LayoutString.this.getObject().rainbowMode) {
//					this.active = false;
//				} else {
//					this.active = true;
//				}
//				super.render(matrixStack, mouseX, mouseY, partialTicks);
//			}
//		};
//		this.rightclickMenu.addContent(rainbowFadeSpeedButton);

	}
	
	@Override
	protected void renderBorder(MatrixStack matrix, int mouseX, int mouseY) {
		//horizontal line top
		fill(matrix, this.getStringPosX(), this.getStringPosY(), this.getStringPosX() + this.object.width, this.getStringPosY() + 1, Color.BLUE.getRGB());
		//horizontal line bottom
		fill(matrix, this.getStringPosX(), this.getStringPosY() + this.object.height, this.getStringPosX() + this.object.width + 1, this.getStringPosY() + this.object.height + 1, Color.BLUE.getRGB());
		//vertical line left
		fill(matrix, this.getStringPosX(), this.getStringPosY(), this.getStringPosX() + 1, this.getStringPosY() + this.object.height, Color.BLUE.getRGB());
		//vertical line right
		fill(matrix, this.getStringPosX() + this.object.width, this.getStringPosY(), this.getStringPosX() + this.object.width + 1, this.getStringPosY() + this.object.height, Color.BLUE.getRGB());
	
		//Render pos and size values
		FontRenderer font = Minecraft.getInstance().fontRenderer;
		RenderUtils.setScale(matrix, 0.5F);
		font.drawString(matrix, Locals.localize("drippyloadingscreen.helper.creator.items.border.orientation")+ ": " + this.object.orientation, this.getStringPosX()*2, (this.getStringPosY()*2) - 44, Color.WHITE.getRGB());
		font.drawString(matrix, Locals.localize("drippyloadingscreen.helper.creator.items.string.border.scale") + ": " + this.getStringScale(), this.getStringPosX()*2, (this.getStringPosY()*2) - 35, Color.WHITE.getRGB());
		font.drawString(matrix, Locals.localize("drippyloadingscreen.helper.creator.items.string.border.alignment") + ": " + this.getObject().alignment.key, this.getStringPosX()*2, (this.getStringPosY()*2) - 26, Color.WHITE.getRGB());
		font.drawString(matrix, Locals.localize("drippyloadingscreen.helper.creator.items.border.posx") + ": " + this.getStringPosX(), this.getStringPosX()*2, (this.getStringPosY()*2) - 17, Color.WHITE.getRGB());
		font.drawString(matrix, Locals.localize("drippyloadingscreen.helper.creator.items.border.width") + ": " + this.object.width, this.getStringPosX()*2, (this.getStringPosY()*2) - 8, Color.WHITE.getRGB());
		font.drawString(matrix, Locals.localize("drippyloadingscreen.helper.creator.items.border.posy") + ": " + this.getStringPosY(), ((this.getStringPosX() + this.object.width)*2)+3, ((this.getStringPosY() + this.object.height)*2) - 14, Color.WHITE.getRGB());
		font.drawString(matrix, Locals.localize("drippyloadingscreen.helper.creator.items.border.height") + ": " + this.object.height, ((this.getStringPosX() + this.object.width)*2)+3, ((this.getStringPosY() + this.object.height)*2) - 5, Color.WHITE.getRGB());
		RenderUtils.postScale(matrix);
	}
	
//	@Override
//	protected void renderHighlightBorder(MatrixStack matrix) {
//		Color c = new Color(0, 200, 255, 255);
//
//		//horizontal line top
//		AbstractGui.fill(matrix, this.getStringPosX(), this.getStringPosY(), this.getStringPosX() + this.object.width, this.getStringPosY() + 1, c.getRGB());
//		//horizontal line bottom
//		AbstractGui.fill(matrix, this.getStringPosX(), this.getStringPosY() + this.object.height, this.getStringPosX() + this.object.width + 1, this.getStringPosY() + this.object.height + 1, c.getRGB());
//		//vertical line left
//		AbstractGui.fill(matrix, this.getStringPosX(), this.getStringPosY(), this.getStringPosX() + 1, this.getStringPosY() + this.object.height, c.getRGB());
//		//vertical line right
//		AbstractGui.fill(matrix, this.getStringPosX() + this.object.width, this.getStringPosY(), this.getStringPosX() + this.object.width + 1, this.getStringPosY() + this.object.height, c.getRGB());
//	}
	
	private int getStringPosX() {
//		return (int)(this.object.getPosX() * this.getStringScale());
		return this.object.getPosX();
	}
	
	private int getStringPosY() {
//		return (int)(this.object.getPosY() * this.getStringScale());
		return this.object.getPosY();
	}
	
	private float getStringScale() {
		return ((StringCustomizationItem)this.object).scale;
	}
	
	public StringCustomizationItem getObject() {
		return ((StringCustomizationItem)this.object);
	}
	
	@Override
	public boolean isGrabberPressed() {
		return false;
	}
	
	@Override
	public int getActiveResizeGrabber() {
		return -1;
	}

//	@Override
//	protected void setOrientation(String pos) {
//		super.setOrientation(pos);
//		if (this.getObject().alignment == Alignment.CENTERED) {
//			if (this.object.orientation.endsWith("-right")) {
//				this.object.posX += this.object.width;
//			}
//			if (this.object.orientation.endsWith("-centered")) {
//				this.object.posX += this.object.width / 2;
//			}
//		} else if (this.getObject().alignment == Alignment.RIGHT) {
//			if (this.object.orientation.endsWith("-right")) {
//				this.object.posX += this.object.width;
//			}
//			if (this.object.orientation.endsWith("-left")) {
//				this.object.posX += this.object.width;
//			}
//			if (this.object.orientation.endsWith("-centered")) {
//				this.object.posX += this.object.width / 2;
//			}
//		} else if (this.getObject().alignment == Alignment.LEFT) {
//			if (this.object.orientation.endsWith("-centered")) {
//				this.object.posX += this.object.width / 2;
//			}
//		}
//	}
	
	public void setScale(float scale) {
		if (this.getObject().scale != scale) {
			this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
		}
		((StringCustomizationItem)this.object).scale = scale;
//		this.setWidth((int)(Minecraft.getInstance().fontRenderer.getStringWidth(this.object.value)*scale));
//		this.setHeight((int)(7*scale));
	}
	
	public void setText(String text) {
		if (!this.getObject().valueRaw.equals(text)) {
			this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
		}
		
		this.getObject().valueRaw = text;
		this.getObject().value = text;
		this.setScale(this.getStringScale());
	}
	
	private void setTextCallback(String text) {
		if (text == null) {
			return;
		}
		if (text.length() > 0) {
			this.setText(StringUtils.convertFormatCodes(text, "&", "§"));
		} else {
			LayoutEditorScreen.displayNotification("§c§l" + Locals.localize("drippyloadingscreen.helper.creator.texttooshort.title"), "", Locals.localize("drippyloadingscreen.helper.creator.texttooshort.desc"), "", "", "", "");
		}
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
	protected void updateHovered(int mouseX, int mouseY) {
		if ((mouseX >= this.getStringPosX()) && (mouseX <= this.getStringPosX() + this.object.width) && (mouseY >= this.getStringPosY()) && mouseY <= this.getStringPosY() + this.object.height) {
			this.hovered = true;
		} else {
			this.hovered = false;
		}
	}
	
	@Override
	public List<PropertiesSection> getProperties() {
		List<PropertiesSection> l = new ArrayList<PropertiesSection>();
		
		PropertiesSection p1 = new PropertiesSection("customization");
		p1.addEntry("action", "addtext");
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
		p1.addEntry("value", this.object.value);
		p1.addEntry("x", "" + this.object.posX);
		p1.addEntry("y", "" + this.object.posY);
		p1.addEntry("orientation", this.object.orientation);
		p1.addEntry("scale", "" + this.getObject().scale);
		p1.addEntry("shadow", "" + this.getObject().shadow);
		p1.addEntry("alignment", "" + this.getObject().alignment.key);
		p1.addEntry("textcolor", this.getObject().textColorHex);
		
		if (this.getObject().rainbowMode) {
			p1.addEntry("rainbowmode", "true");
		}
		if (this.getObject().rainbowText.getSpeed() != 1.0F) {
			p1.addEntry("rainbowspeed", "" + this.getObject().rainbowText.getSpeed());
		}
		if (this.getObject().allRainbowColorsSet()) {
			p1.addEntry("rainbowstartcolor1", this.getObject().rainbowStartColorHex1);
			p1.addEntry("rainbowendcolor1", this.getObject().rainbowEndColorHex1);
			p1.addEntry("rainbowstartcolor2", this.getObject().rainbowStartColorHex2);
			p1.addEntry("rainbowendcolor2", this.getObject().rainbowEndColorHex2);
		}

		this.addVisibilityPropertiesTo(p1);
		
		l.add(p1);
		
		return l;
	}

}
