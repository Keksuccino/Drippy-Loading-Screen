package de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.vanilla;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.LayoutEditorScreen;
import de.keksuccino.drippyloadingscreen.customization.helper.editor.elements.LayoutElement;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.FHTextInputPopup;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.FHYesNoPopup;
import de.keksuccino.drippyloadingscreen.customization.items.vanilla.VanillaSplashCustomizationItemBase;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.CharacterFilter;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

public abstract class VanillaLayoutSplashElement extends LayoutElement {
	
	protected boolean scaleable = false;
	
	public VanillaLayoutSplashElement(VanillaSplashCustomizationItemBase object, LayoutEditorScreen handler) {
		super(object, true, handler);
	}
	
	@Override
	public void init() {
		this.enableVisibilityRequirements = false;
		this.orderable = false;
		super.init();
		
		/** RESET ELEMENT **/
		AdvancedButton resetElementButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.editor.elements.vanilla.resetelement"), (press) -> {
			this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
			this.resetElement();
			this.handler.rebuildEditor();
		});
		resetElementButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.editor.elements.vanilla.resetelement.btn.desc"), "%n%"));
		this.rightclickMenu.addContent(resetElementButton);
		
		this.rightclickMenu.addSeparator();
		
		/** SCALE **/
		if (this.scaleable) {
			AdvancedButton scaleButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.editor.elements.scale"), (press) -> {
				FHTextInputPopup pop = new FHTextInputPopup(new Color(0, 0, 0, 0), Locals.localize("drippyloadingscreen.helper.editor.elements.scale"), CharacterFilter.getDoubleCharacterFiler(), 240, (call) -> {
					if (call != null) {
						float f = 1.0F;
						if (MathUtils.isFloat(call)) {
							f = Float.parseFloat(call);
						}
						if (f != this.getVanillaObject().scale) {
							this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
						}
						this.getVanillaObject().scale = f;
					}
				});
				pop.setText("" + this.getVanillaObject().scale);
				PopupHandler.displayPopup(pop);
			});
			scaleButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.editor.elements.scale.btn.desc"), "%n%"));
			this.rightclickMenu.addContent(scaleButton);
		}
		
		this.rightclickMenu.addSeparator();
		
	}
	
	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY) {
		
		if (!this.getVanillaObject().vanillaVisible) {
			this.object.render(matrix);
		} else {
			super.render(matrix, mouseX, mouseY);
		}
		
	}
	
	@Override
	protected void renderBorder(MatrixStack matrix, int mouseX, int mouseY) {
		//horizontal line top
		AbstractGui.fill(matrix, this.object.getPosX(), this.object.getPosY(), this.object.getPosX() + this.object.width, this.object.getPosY() + 1, Color.BLUE.getRGB());
		//horizontal line bottom
		AbstractGui.fill(matrix, this.object.getPosX(), this.object.getPosY() + this.object.height - 1, this.object.getPosX() + this.object.width, this.object.getPosY() + this.object.height, Color.BLUE.getRGB());
		//vertical line left
		AbstractGui.fill(matrix, this.object.getPosX(), this.object.getPosY(), this.object.getPosX() + 1, this.object.getPosY() + this.object.height, Color.BLUE.getRGB());
		//vertical line right
		AbstractGui.fill(matrix, this.object.getPosX() + this.object.width - 1, this.object.getPosY(), this.object.getPosX() + this.object.width, this.object.getPosY() + this.object.height, Color.BLUE.getRGB());
		
		//Render pos and size values
		RenderUtils.setScale(matrix, 0.5F);
		AbstractGui.drawString(matrix, Minecraft.getInstance().font, Locals.localize("drippyloadingscreen.helper.creator.items.border.orientation") + ": " + this.object.orientation, this.object.getPosX()*2, (this.object.getPosY()*2) - 26, Color.WHITE.getRGB());
		AbstractGui.drawString(matrix, Minecraft.getInstance().font, Locals.localize("drippyloadingscreen.helper.creator.items.border.posx") + ": " + this.object.getPosX(), this.object.getPosX()*2, (this.object.getPosY()*2) - 17, Color.WHITE.getRGB());
		AbstractGui.drawString(matrix, Minecraft.getInstance().font, Locals.localize("drippyloadingscreen.helper.creator.items.border.width") + ": " + this.object.width, this.object.getPosX()*2, (this.object.getPosY()*2) - 8, Color.WHITE.getRGB());
		
		AbstractGui.drawString(matrix, Minecraft.getInstance().font, Locals.localize("drippyloadingscreen.helper.creator.items.border.posy") + ": " + this.object.getPosY(), ((this.object.getPosX() + this.object.width)*2)+3, ((this.object.getPosY() + this.object.height)*2) - 14, Color.WHITE.getRGB());
		AbstractGui.drawString(matrix, Minecraft.getInstance().font, Locals.localize("drippyloadingscreen.helper.creator.items.border.height") + ": " + this.object.height, ((this.object.getPosX() + this.object.width)*2)+3, ((this.object.getPosY() + this.object.height)*2) - 5, Color.WHITE.getRGB());
		RenderUtils.postScale(matrix);
	}
	
	@Override
	public int getActiveResizeGrabber() {
		return -1;
	}
	
	@Override
	public boolean isGrabberPressed() {
		return false;
	}

	@Override
	public void destroyObject() {
		if (DrippyLoadingScreen.config.getOrDefault("editordeleteconfirmation", true)) {
			PopupHandler.displayPopup(new FHYesNoPopup(300, new Color(0, 0, 0, 0), 240, (call) -> {
				if (call) {
					this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
					this.getVanillaObject().vanillaVisible = false;
					this.getVanillaObject().element.visible = false;
					this.handler.rebuildEditor();
				}
			}, "§c§l" + Locals.localize("drippyloadingscreen.helper.creator.messages.sure"), "", Locals.localize("drippyloadingscreen.helper.creator.deleteobject"), "", "", "", "", ""));
		} else {
			this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
			this.getVanillaObject().vanillaVisible = false;
			this.getVanillaObject().element.visible = false;
			this.handler.rebuildEditor();
		}
	}
	
	public VanillaSplashCustomizationItemBase getVanillaObject() {
		return (VanillaSplashCustomizationItemBase) this.object;
	}
	
	public abstract void resetElement();

}
