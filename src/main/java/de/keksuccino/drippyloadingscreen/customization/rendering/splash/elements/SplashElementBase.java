package de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

public abstract class SplashElementBase extends AbstractGui {

    protected SplashCustomizationLayer handler;

    public boolean fireEvents = true;

    public boolean visible = true;
    public int width = 20;
    public int height = 20;
    public int x = 10;
    public int y = 10;
    public float scale = 1.0F;
    public float rotation = 0.0F;
    public float opacity = 1.0F;

    protected Minecraft mc = Minecraft.getInstance();

    public SplashElementBase(SplashCustomizationLayer handler) {
        this.handler = handler;
    }

    public abstract void render(MatrixStack matrix, int scaledWidth, int scaledHeight, float partialTicks);

    public SplashCustomizationLayer getHandler() {
        return this.handler;
    }

    /**
     * Only for internal use. It's not recommended to manually set the handler at any point!
     */
    public void setHandler(SplashCustomizationLayer handler) {
        this.handler = handler;
    }

    public void onReloadCustomizations() {
    }

}
