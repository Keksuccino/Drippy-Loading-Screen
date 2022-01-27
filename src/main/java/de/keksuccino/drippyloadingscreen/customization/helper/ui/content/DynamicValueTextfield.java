package de.keksuccino.drippyloadingscreen.customization.helper.ui.content;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.api.PlaceholderTextValueRegistry;
import de.keksuccino.drippyloadingscreen.api.PlaceholderTextValueRegistry.PlaceholderValue;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.UIBase;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.content.AdvancedImageButton;
import de.keksuccino.konkrete.gui.content.AdvancedTextField;
import de.keksuccino.konkrete.input.CharacterFilter;
import de.keksuccino.konkrete.input.MouseInput;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;

public class DynamicValueTextfield extends AdvancedTextField {

	private AdvancedImageButton variableButton;
	private FHContextMenu variableMenu;
	
	private static final ResourceLocation VARIABLES_BUTTON_RESOURCE = new ResourceLocation("drippyloadingscreen", "add_btn.png");
	
	public DynamicValueTextfield(Font fontrenderer, int x, int y, int width, int height, boolean handleTextField, CharacterFilter filter) {
		super(fontrenderer, x, y, width, height, handleTextField, filter);
		
		variableMenu = new FHContextMenu();
		variableMenu.setAutoclose(true);

		/** PLAYER CATEGORY **/
		FHContextMenu playerMenu = new FHContextMenu();
		playerMenu.setAutoclose(true);
		variableMenu.addChild(playerMenu);

		AdvancedButton playerName = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.playername"), true, (press) -> {
			this.insertText("%playername%");
		});
		playerName.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.playername.desc"), "%n%"));
		UIBase.colorizeButton(playerName);
		playerMenu.addContent(playerName);

		AdvancedButton playerUUID = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.playeruuid"), true, (press) -> {
			this.insertText("%playeruuid%");
		});
		playerUUID.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.playeruuid.desc"), "%n%"));
		UIBase.colorizeButton(playerUUID);
		playerMenu.addContent(playerUUID);

		AdvancedButton playerCategoryButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.categories.player"), true, (press) -> {
			playerMenu.setParentButton((AdvancedButton) press);
			playerMenu.openMenuAt(0, press.y);
		});
		UIBase.colorizeButton(playerCategoryButton);
		variableMenu.addContent(playerCategoryButton);
		
		/** REAL TIME CATEGORY **/
		FHContextMenu realtimeMenu = new FHContextMenu();
		realtimeMenu.setAutoclose(true);
		variableMenu.addChild(realtimeMenu);
		
		AdvancedButton realtimeYear = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.realtimeyear"), true, (press) -> {
			this.insertText("%realtimeyear%");
		});
		UIBase.colorizeButton(realtimeYear);
		realtimeMenu.addContent(realtimeYear);
		
		AdvancedButton realtimeMonth = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.realtimemonth"), true, (press) -> {
			this.insertText("%realtimemonth%");
		});
		UIBase.colorizeButton(realtimeMonth);
		realtimeMenu.addContent(realtimeMonth);
		
		AdvancedButton realtimeDay = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.realtimeday"), true, (press) -> {
			this.insertText("%realtimeday%");
		});
		UIBase.colorizeButton(realtimeDay);
		realtimeMenu.addContent(realtimeDay);
		
		AdvancedButton realtimeHour = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.realtimehour"), true, (press) -> {
			this.insertText("%realtimehour%");
		});
		UIBase.colorizeButton(realtimeHour);
		realtimeMenu.addContent(realtimeHour);
		
		AdvancedButton realtimeMinute = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.realtimeminute"), true, (press) -> {
			this.insertText("%realtimeminute%");
		});
		UIBase.colorizeButton(realtimeMinute);
		realtimeMenu.addContent(realtimeMinute);
		
		AdvancedButton realtimeSecond = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.realtimesecond"), true, (press) -> {
			this.insertText("%realtimesecond%");
		});
		UIBase.colorizeButton(realtimeSecond);
		realtimeMenu.addContent(realtimeSecond);
		
		AdvancedButton realtimeCategoryButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.categories.realtime"), true, (press) -> {
			realtimeMenu.setParentButton((AdvancedButton) press);
			realtimeMenu.openMenuAt(0, press.y);
		});
		UIBase.colorizeButton(realtimeCategoryButton);
		variableMenu.addContent(realtimeCategoryButton);
		
		/** OTHER CATEGORY **/
		FHContextMenu otherMenu = new FHContextMenu();
		otherMenu.setAutoclose(true);
		variableMenu.addChild(otherMenu);
		
		AdvancedButton mcVersion = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.mcversion"), true, (press) -> {
			this.insertText("%mcversion%");
		});
		mcVersion.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.mcversion.desc"), "%n%"));
		UIBase.colorizeButton(mcVersion);
		otherMenu.addContent(mcVersion);
		
		AdvancedButton forgeVersion = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.forgeversion"), true, (press) -> {
			this.insertText("%version:" + DrippyLoadingScreen.MOD_LOADER + "%");
		});
		forgeVersion.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.forgeversion.desc"), "%n%"));
		UIBase.colorizeButton(forgeVersion);
		otherMenu.addContent(forgeVersion);
		
		AdvancedButton modVersion = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.modversion"), true, (press) -> {
			this.insertText("%version:<modid>%");
		});
		modVersion.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.modversion.desc"), "%n%"));
		UIBase.colorizeButton(modVersion);
		otherMenu.addContent(modVersion);
		
		AdvancedButton totalMods = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.totalmods"), true, (press) -> {
			this.insertText("%totalmods%");
		});
		totalMods.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.totalmods.desc"), "%n%"));
		UIBase.colorizeButton(totalMods);
		otherMenu.addContent(totalMods);
		
		AdvancedButton loadedMods = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.loadedmods"), true, (press) -> {
			this.insertText("%loadedmods%");
		});
		loadedMods.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.loadedmods.desc"), "%n%"));
		UIBase.colorizeButton(loadedMods);
		otherMenu.addContent(loadedMods);
		
		AdvancedButton fps = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.fps"), true, (press) -> {
			this.insertText("%fps%");
		});
		UIBase.colorizeButton(fps);
		otherMenu.addContent(fps);
		
		otherMenu.addSeparator();
		
		AdvancedButton percentRam = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.percentram"), true, (press) -> {
			this.insertText("%percentram%");
		});
		UIBase.colorizeButton(percentRam);
		otherMenu.addContent(percentRam);
		
		AdvancedButton usedRam = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.usedram"), true, (press) -> {
			this.insertText("%usedram%");
		});
		UIBase.colorizeButton(usedRam);
		otherMenu.addContent(usedRam);
		
		AdvancedButton maxRam = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.maxram"), true, (press) -> {
			this.insertText("%maxram%");
		});
		UIBase.colorizeButton(maxRam);
		otherMenu.addContent(maxRam);

		otherMenu.addSeparator();

		AdvancedButton loadingProgress = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.loadingprogress"), true, (press) -> {
			this.insertText("%loadingprogress%");
		});
		UIBase.colorizeButton(loadingProgress);
		otherMenu.addContent(loadingProgress);

		//TODO übernehmen
		AdvancedButton cpuInfo = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.cpuinfo"), true, (press) -> {
			this.insertText("%cpuinfo%");
		});
		UIBase.colorizeButton(cpuInfo);
		otherMenu.addContent(cpuInfo);

		//TODO übernehmen
		AdvancedButton gpuInfo = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.gpuinfo"), true, (press) -> {
			this.insertText("%gpuinfo%");
		});
		UIBase.colorizeButton(gpuInfo);
		otherMenu.addContent(gpuInfo);

		//TODO übernehmen
		AdvancedButton osName = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.osname"), true, (press) -> {
			this.insertText("%osname%");
		});
		UIBase.colorizeButton(osName);
		otherMenu.addContent(osName);

		//TODO übernehmen
		AdvancedButton openGlVersion = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.openglversion"), true, (press) -> {
			this.insertText("%openglversion%");
		});
		UIBase.colorizeButton(openGlVersion);
		otherMenu.addContent(openGlVersion);

		//TODO übernehmen
		AdvancedButton javaVersion = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.javaversion"), true, (press) -> {
			this.insertText("%javaversion%");
		});
		UIBase.colorizeButton(javaVersion);
		otherMenu.addContent(javaVersion);

		//TODO übernehmen
		AdvancedButton randomText = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.randomtext"), true, (press) -> {
			this.insertText("%randomtext:<filepath>:<interval_sec>%");
		});
		randomText.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.randomtext.desc"), "%n%"));
		UIBase.colorizeButton(randomText);
		otherMenu.addContent(randomText);
		
		otherMenu.addSeparator();
		
		//Custom values without category
		for (PlaceholderValue v : PlaceholderTextValueRegistry.getInstance().getValuesAsList()) {
			if (v.valueCategory == null) {
				AdvancedButton customValue = new AdvancedButton(0, 0, 0, 0, v.valueDisplayName, true, (press) -> {
					this.insertText(v.getPlaceholder());
				});
				UIBase.colorizeButton(customValue);
				otherMenu.addContent(customValue);
			}
		}
		
		AdvancedButton otherCategoryButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.categories.other"), true, (press) -> {
			otherMenu.setParentButton((AdvancedButton) press);
			otherMenu.openMenuAt(0, press.y);
		});
		UIBase.colorizeButton(otherCategoryButton);
		variableMenu.addContent(otherCategoryButton);
		
		variableMenu.addSeparator();
		
		/** CUSTOM CATEGORIES **/
		for (String c : PlaceholderTextValueRegistry.getInstance().getCategories()) {
			
			List<PlaceholderValue> values = PlaceholderTextValueRegistry.getInstance().getValuesForCategory(c);
			
			if (!values.isEmpty()) {
				
				FHContextMenu customCategoryMenu = new FHContextMenu();
				customCategoryMenu.setAutoclose(true);
				variableMenu.addChild(customCategoryMenu);
				
				for (PlaceholderValue v : values) {
					AdvancedButton customValue = new AdvancedButton(0, 0, 0, 0, v.valueDisplayName, true, (press) -> {
						this.insertText(v.getPlaceholder());
					});
					UIBase.colorizeButton(customValue);
					customCategoryMenu.addContent(customValue);
				}
				
				AdvancedButton customCategoryButton = new AdvancedButton(0, 0, 0, 0, c, true, (press) -> {
					customCategoryMenu.setParentButton((AdvancedButton) press);
					customCategoryMenu.openMenuAt(0, press.y);
				});
				UIBase.colorizeButton(customCategoryButton);
				variableMenu.addContent(customCategoryButton);
				
			}
			
		}

		/** VARIABLE BUTTON **/
		variableButton = new AdvancedImageButton(0, 0, height, height, VARIABLES_BUTTON_RESOURCE, true, (press) -> {
			UIBase.openScaledContextMenuAtMouse(variableMenu);
		});
		variableButton.ignoreBlockedInput = true;
		variableButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.ui.dynamicvariabletextfield.variables.desc"), "%n%"));
		UIBase.colorizeButton(variableButton);
		
		variableMenu.setParentButton(variableButton);
		
	}
	
	@Override
	public void renderButton(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		if (this.variableButton != null) {
			
			this.variableButton.setWidth(this.height);
			this.variableButton.setHeight(this.height);
			
			super.renderButton(matrix, mouseX, mouseY, partialTicks);
			
			this.variableButton.setX(this.x + this.width + 5);
			this.variableButton.setY(this.y);
			this.variableButton.render(matrix, mouseX, mouseY, partialTicks);
			
			float scale = UIBase.getUIScale();
			
			MouseInput.setRenderScale(scale);
			
			matrix.pushPose();
			
			matrix.scale(scale, scale, scale);
			
			if (this.variableMenu != null) {
				this.variableMenu.render(matrix, MouseInput.getMouseX(), MouseInput.getMouseY());
			}
			
			matrix.popPose();
			
			MouseInput.resetRenderScale();
			
		}
	}

}
