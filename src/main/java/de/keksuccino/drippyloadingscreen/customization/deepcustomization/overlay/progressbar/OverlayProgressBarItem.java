package de.keksuccino.drippyloadingscreen.customization.deepcustomization.overlay.progressbar;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationElement;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationItem;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class OverlayProgressBarItem extends DeepCustomizationItem {

    public String hexColorString = "#RRGGBB";
    public Color hexColor = null;

    public OverlayProgressBarItem(DeepCustomizationElement parentElement, PropertiesSection item) {

        super(parentElement, item);

        String hex = item.getEntryValue("custom_color_hex");
        if ((hex != null) && !hex.toUpperCase().replace(" ", "").equals("#RRGGBB") && !hex.replace(" ", "").equals("")) {
            Color c = RenderUtils.getColorFromHexString(hex);
            if (c != null) {
                this.hexColorString = hex;
                this.hexColor = c;
            }
        }

    }

    @Override
    public void render(MatrixStack matrix, Screen menu) {

        Minecraft mc = Minecraft.getInstance();
        int i = mc.getWindow().getGuiScaledWidth();
        double d1 = Math.min((double)mc.getWindow().getGuiScaledWidth() * 0.75D, mc.getWindow().getGuiScaledHeight()) * 0.25D;
        double d0 = d1 * 4.0D;
        int j1 = (int)(d0 * 0.5D);
        int k1 = (int)((double)mc.getWindow().getGuiScaledHeight() * 0.8325D);
        float currentProgress = 0.5F;

        this.posX = i / 2 - j1;
        this.posY = k1 - 5;
        this.width = j1 * 2;
        this.height = 10;

        this.drawProgressBar(matrix, i / 2 - j1, k1 - 5, i / 2 + j1, k1 + 5, this.opacity, currentProgress);

    }

    private void drawProgressBar(MatrixStack matrix, int i1, int i2, int i3, int i4, float opacity, float currentProgress) {
        int i = MathHelper.ceil((float)(i3 - i1 - 2) * currentProgress);
        int j = Math.round(opacity * 255.0F);
        int k = ColorHelper.PackedColor.color(j, 255, 255, 255);
        if (this.hexColor != null) {
            k = ColorHelper.PackedColor.color(j, this.hexColor.getRed(), this.hexColor.getGreen(), this.hexColor.getBlue());
        }
        fill(matrix, i1 + 2, i2 + 2, i1 + i, i4 - 2, k);
        fill(matrix, i1 + 1, i2, i3 - 1, i2 + 1, k);
        fill(matrix, i1 + 1, i4, i3 - 1, i4 - 1, k);
        fill(matrix, i1, i2, i1 + 1, i4, k);
        fill(matrix, i3, i2, i3 - 1, i4, k);
    }

}