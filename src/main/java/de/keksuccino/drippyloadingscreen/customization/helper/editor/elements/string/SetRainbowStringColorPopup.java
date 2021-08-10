package de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.string;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import de.keksuccino.drippyloadingscreen.customization.helper.ui.UIBase;
import de.keksuccino.drippyloadingscreen.customization.items.StringCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.rendering.RainbowText;
import de.keksuccino.drippyloadingscreen.utils.TextComponentUtils;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.content.AdvancedTextField;
import de.keksuccino.konkrete.gui.screens.popup.Popup;
import de.keksuccino.konkrete.input.KeyboardData;
import de.keksuccino.konkrete.input.KeyboardHandler;
import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.konkrete.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class SetRainbowStringColorPopup extends Popup {
	
	protected LayoutString element;
	
	protected AdvancedTextField firstStartColorTextField;
	protected AdvancedTextField secondStartColorTextField;
	protected AdvancedTextField firstEndColorTextField;
	protected AdvancedTextField secondEndColorTextField;
	
	protected AdvancedButton doneButton;
	protected AdvancedButton cancelButton;
	
	protected int width = 250;
	protected String title = Locals.localize("drippyloadingscreen.helper.editor.elements.string.rainbow.colors");
	
	public SetRainbowStringColorPopup(LayoutString element) {
		super(240);
		this.element = element;
		this.init();
		this.setInputs();
	}
	
	protected void init() {
		
		this.firstStartColorTextField = new AdvancedTextField(Minecraft.getInstance().fontRenderer, 0, 0, 100, 20, true, null);
		this.firstStartColorTextField.setCanLoseFocus(true);
		this.firstStartColorTextField.setFocused2(false);
		this.firstStartColorTextField.setMaxStringLength(15);
		
		this.secondStartColorTextField = new AdvancedTextField(Minecraft.getInstance().fontRenderer, 0, 0, 100, 20, true, null);
		this.secondStartColorTextField.setCanLoseFocus(true);
		this.secondStartColorTextField.setFocused2(false);
		this.secondStartColorTextField.setMaxStringLength(15);
		
		this.firstEndColorTextField = new AdvancedTextField(Minecraft.getInstance().fontRenderer, 0, 0, 100, 20, true, null);
		this.firstEndColorTextField.setCanLoseFocus(true);
		this.firstEndColorTextField.setFocused2(false);
		this.firstEndColorTextField.setMaxStringLength(15);
		
		this.secondEndColorTextField = new AdvancedTextField(Minecraft.getInstance().fontRenderer, 0, 0, 100, 20, true, null);
		this.secondEndColorTextField.setCanLoseFocus(true);
		this.secondEndColorTextField.setFocused2(false);
		this.secondEndColorTextField.setMaxStringLength(15);
		
		this.doneButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("popup.done"), true, (press) -> {
			this.onFinish();
		});
		this.addButton(this.doneButton);
		
		this.cancelButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("popup.yesno.cancel"), true, (press) -> {
			this.onCancel();
		});
		this.addButton(this.cancelButton);
		
		KeyboardHandler.addKeyPressedListener(this::onEnterPressed);
		KeyboardHandler.addKeyPressedListener(this::onEscapePressed);
		
	}

	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, Screen renderIn) {
		super.render(matrix, mouseX, mouseY, renderIn);
		
		FontRenderer font = Minecraft.getInstance().fontRenderer;
		
		if (this.isDisplayed()) {
			
			RenderSystem.enableBlend();
			
			drawCenteredString(matrix, font, new StringTextComponent(title), renderIn.width / 2, (renderIn.height / 2) - 90, Color.WHITE.getRGB());
			
			/** FIRST ROW **/
			this.firstStartColorTextField.setX((renderIn.width / 2) - this.firstStartColorTextField.getWidth() - 10);
			this.firstStartColorTextField.setY((renderIn.height / 2) - 50);
			this.firstStartColorTextField.renderWidget(matrix, mouseX, mouseY, Minecraft.getInstance().getRenderPartialTicks());
			String firstStartString = Locals.localize("drippyloadingscreen.helper.editor.elements.string.rainbow.colors.popup.firststartcolor");
			font.drawStringWithShadow(matrix, firstStartString, this.firstStartColorTextField.getX(), this.firstStartColorTextField.getY() - 10, -1);
			
			this.firstEndColorTextField.setX((renderIn.width / 2) + 10);
			this.firstEndColorTextField.setY((renderIn.height / 2) - 50);
			this.firstEndColorTextField.renderWidget(matrix, mouseX, mouseY, Minecraft.getInstance().getRenderPartialTicks());
			String firstEndString = Locals.localize("drippyloadingscreen.helper.editor.elements.string.rainbow.colors.popup.firstendcolor");
			font.drawStringWithShadow(matrix, firstEndString, this.firstEndColorTextField.getX(), this.firstEndColorTextField.getY() - 10, -1);
			
			/** DESCRIPTION **/
			String line1 = TextFormatting.GRAY + Locals.localize("drippyloadingscreen.helper.editor.elements.string.rainbow.colors.popup.colordesc.line1");
			int line1Width = font.getStringWidth(line1);
			font.drawStringWithShadow(matrix, line1, (renderIn.width / 2) - (line1Width / 2), (renderIn.height / 2) - 17, -1);
			String line2 = TextFormatting.GRAY + Locals.localize("drippyloadingscreen.helper.editor.elements.string.rainbow.colors.popup.colordesc.line2");
			int line2Width = font.getStringWidth(line2);
			font.drawStringWithShadow(matrix, line2, (renderIn.width / 2) - (line2Width / 2), (renderIn.height / 2) - 6, -1);
			String line3 = TextFormatting.GRAY + Locals.localize("drippyloadingscreen.helper.editor.elements.string.rainbow.colors.popup.colordesc.line3");
			int line3Width = font.getStringWidth(line3);
			font.drawStringWithShadow(matrix, line3, (renderIn.width / 2) - (line3Width / 2), (renderIn.height / 2) + 5, -1);
			
			/** SECOND ROW **/
			this.secondStartColorTextField.setX((renderIn.width / 2) - this.secondStartColorTextField.getWidth() - 10);
			this.secondStartColorTextField.setY((renderIn.height / 2) + 36);
			this.secondStartColorTextField.renderWidget(matrix, mouseX, mouseY, Minecraft.getInstance().getRenderPartialTicks());
			String secondStartString = Locals.localize("drippyloadingscreen.helper.editor.elements.string.rainbow.colors.popup.secondstartcolor");
			font.drawStringWithShadow(matrix, secondStartString, this.secondStartColorTextField.getX(), this.secondStartColorTextField.getY() - 10, -1);
			
			this.secondEndColorTextField.setX((renderIn.width / 2) + 10);
			this.secondEndColorTextField.setY((renderIn.height / 2) + 36);
			this.secondEndColorTextField.renderWidget(matrix, mouseX, mouseY, Minecraft.getInstance().getRenderPartialTicks());
			String secondEndString = Locals.localize("drippyloadingscreen.helper.editor.elements.string.rainbow.colors.popup.secondendcolor");
			font.drawStringWithShadow(matrix, secondEndString, this.secondEndColorTextField.getX(), this.secondEndColorTextField.getY() - 10, -1);
			
			/** THIRD ROW **/
			this.doneButton.setX((renderIn.width / 2) - this.doneButton.getWidth() - 10);
			this.doneButton.setY((renderIn.height / 2) + 80);
			
			this.cancelButton.setX((renderIn.width / 2) + 10);
			this.cancelButton.setY((renderIn.height / 2) + 80);
			
			this.renderButtons(matrix, mouseX, mouseY);
			
		}
	}
	
	public void setText(String text) {
		this.firstStartColorTextField.setText("");
		this.firstStartColorTextField.writeText(text);
	}
	
	protected void onFinish() {
		
		String firstStart = this.firstStartColorTextField.getText();
		String firstEnd = this.firstEndColorTextField.getText();
		String secondStart = this.secondStartColorTextField.getText();
		String secondEnd = this.secondEndColorTextField.getText();
		
		if (!firstStart.replace(" ", "").equals("") && !firstEnd.replace(" ", "").equals("") && !secondStart.replace(" ", "").equals("") && !secondEnd.replace(" ", "").equals("")) {
			Color fS = RenderUtils.getColorFromHexString(firstStart);
			Color fE = RenderUtils.getColorFromHexString(firstEnd);
			Color sS = RenderUtils.getColorFromHexString(secondStart);
			Color sE = RenderUtils.getColorFromHexString(secondEnd);
			if ((fS != null) && (fE != null) && (sS != null) && (sE != null)) {
				
				this.element.handler.history.saveSnapshot(this.element.handler.history.createSnapshot());
				
				StringCustomizationItem i = this.element.getObject();
				
				i.rainbowStartColorHex1 = firstStart;
				i.rainbowEndColorHex1 = firstEnd;
				i.rainbowStartColorHex2 = secondStart;
				i.rainbowEndColorHex2 = secondEnd;
				
				i.updateRainbowColors();
				
			} else {
				//TODO error popup
			}
		} else {
			//TODO error popup
		}
		
		this.setDisplayed(false);
	}
	
	protected void onCancel() {
		this.setDisplayed(false);
	}
	
	public void onEnterPressed(KeyboardData d) {
		if ((d.keycode == 257) && this.isDisplayed()) {
			this.onFinish();
		}
	}
	
	public void onEscapePressed(KeyboardData d) {
		if ((d.keycode == 256) && this.isDisplayed()) {
			this.onCancel();
		}
	}
	
	protected void setInputs() {
		String fS = this.element.getObject().rainbowStartColorHex1;
		String fE = this.element.getObject().rainbowEndColorHex1;
		String sS = this.element.getObject().rainbowStartColorHex2;
		String sE = this.element.getObject().rainbowEndColorHex2;
		if ((fS != null) && (fE != null) && (sS != null) && (sE != null)) {
			this.firstStartColorTextField.setText(fS);
			this.firstEndColorTextField.setText(fE);
			this.secondStartColorTextField.setText(sS);
			this.secondEndColorTextField.setText(sE);
		} else {
			RainbowText rt = this.element.getObject().rainbowText;
			this.firstStartColorTextField.setText(TextComponentUtils.toHexString(rt.startColor1.getRed(), rt.startColor1.getGreen(), rt.startColor1.getBlue()));
			this.firstEndColorTextField.setText(TextComponentUtils.toHexString(rt.endColor1.getRed(), rt.endColor1.getGreen(), rt.endColor1.getBlue()));
			this.secondStartColorTextField.setText(TextComponentUtils.toHexString(rt.startColor2.getRed(), rt.startColor2.getGreen(), rt.startColor2.getBlue()));
			this.secondEndColorTextField.setText(TextComponentUtils.toHexString(rt.endColor2.getRed(), rt.endColor2.getGreen(), rt.endColor2.getBlue()));
		}
	}
	
	@Override
	protected void colorizePopupButton(AdvancedButton b) {
		UIBase.colorizeButton(b);
	}

}
