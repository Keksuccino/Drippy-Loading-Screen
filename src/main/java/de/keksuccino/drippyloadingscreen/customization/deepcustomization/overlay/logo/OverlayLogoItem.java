package de.keksuccino.drippyloadingscreen.customization.deepcustomization.overlay.logo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationElement;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationItem;
import de.keksuccino.konkrete.properties.PropertiesSection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class OverlayLogoItem extends DeepCustomizationItem {

    private static final ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/mojangstudios.png");

    public OverlayLogoItem(DeepCustomizationElement parentElement, PropertiesSection item) {
        super(parentElement, item);
    }

    @Override
    public void render(PoseStack matrix, Screen menu) {

        Minecraft mc = Minecraft.getInstance();
        int centerX = (int)((double)mc.getWindow().getGuiScaledWidth() * 0.5D);
        int centerY = (int)((double)mc.getWindow().getGuiScaledHeight() * 0.5D);
        double logoHeight = Math.min((double)mc.getWindow().getGuiScaledWidth() * 0.75D, mc.getWindow().getGuiScaledHeight()) * 0.25D;
        int logoHeightHalf = (int)(logoHeight * 0.5D);
        double logoWidth = logoHeight * 4.0D;
        int logoWidthHalf = (int)(logoWidth * 0.5D);

        this.posX = centerX - logoWidthHalf;
        this.posY = centerY - logoHeightHalf;
        this.width = logoWidthHalf * 2;
        this.height = logoHeightHalf * 2;

        RenderSystem.setShaderTexture(0, MOJANG_STUDIOS_LOGO_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.blendEquation(32774);
        RenderSystem.blendFunc(770, 1);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        blit(matrix, centerX - logoWidthHalf, centerY - logoHeightHalf, logoWidthHalf, (int)logoHeight, -0.0625F, 0.0F, 120, 60, 120, 120);
        blit(matrix, centerX, centerY - logoHeightHalf, logoWidthHalf, (int)logoHeight, 0.0625F, 60.0F, 120, 60, 120, 120);

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

    }

}