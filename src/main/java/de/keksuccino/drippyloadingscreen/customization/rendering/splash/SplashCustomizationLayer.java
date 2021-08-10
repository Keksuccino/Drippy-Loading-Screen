package de.keksuccino.drippyloadingscreen.customization.rendering.splash;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.api.item.CustomizationItem;
import de.keksuccino.drippyloadingscreen.api.item.CustomizationItemContainer;
import de.keksuccino.drippyloadingscreen.api.item.CustomizationItemRegistry;
import de.keksuccino.drippyloadingscreen.customization.CustomizationHandler;
import de.keksuccino.drippyloadingscreen.customization.CustomizationPropertiesHandler;
import de.keksuccino.drippyloadingscreen.customization.helper.CustomizationHelperScreen;
import de.keksuccino.drippyloadingscreen.customization.items.*;
import de.keksuccino.drippyloadingscreen.customization.items.custombars.CustomProgressBarCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.items.vanilla.ForgeMemoryInfoSplashCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.items.vanilla.ForgeTextSplashCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.items.vanilla.LogoSplashCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.items.vanilla.ProgressBarSplashCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements.ForgeMemoryInfoSplashElement;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements.ForgeTextSplashElement;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements.LogoSplashElement;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements.ProgressBarSplashElement;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.properties.PropertiesSet;
import de.keksuccino.konkrete.rendering.RenderUtils;
import de.keksuccino.konkrete.resources.ExternalTextureResourceLocation;
import de.keksuccino.konkrete.resources.TextureHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.resources.IAsyncReloader;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class SplashCustomizationLayer extends AbstractGui {

    protected static SplashCustomizationLayer instance;

    private static int backgroundColor = ColorHelper.PackedColor.packColor(255, 239, 50, 61);
    private static int backgroundColor2 = backgroundColor & 16777215;

    public final LogoSplashElement logoSplashElement = new LogoSplashElement(this);
    public final ForgeTextSplashElement forgeTextSplashElement = new ForgeTextSplashElement(this);
    public final ForgeMemoryInfoSplashElement forgeMemoryInfoSplashElement = new ForgeMemoryInfoSplashElement(this);
    public final ProgressBarSplashElement progressBarSplashElement = new ProgressBarSplashElement(this);

    public String customBackgroundHex = null;
    protected String lastCustomBackgroundHex = null;
    public Color customBackgroundColor;

    public ResourceLocation backgroundImage = null;
    public String backgroundImagePath = null;

    public final boolean isEditor;

    /** GETTER ONLY **/
    public IAsyncReloader asyncReloader;
    /** GETTER ONLY **/
    public Consumer<Optional<Throwable>> completedCallback;
    /** GETTER ONLY **/
    public boolean reloading;
    /** GETTER ONLY **/
    public long fadeOutStart;
    /** GETTER ONLY **/
    public long fadeInStart;
    /** GETTER ONLY **/
    public float progress;

    protected List<CustomizationItemBase> backgroundElements = new ArrayList<CustomizationItemBase>();
    protected List<CustomizationItemBase> foregroundElements = new ArrayList<CustomizationItemBase>();

    protected Minecraft mc = Minecraft.getInstance();

    public SplashCustomizationLayer(boolean isEditor) {
        this.isEditor = isEditor;
        this.updateCustomizations();
    }

    public void renderLayer() {

        if ((this.customBackgroundHex != null) && !this.customBackgroundHex.equals(this.lastCustomBackgroundHex)) {
            this.customBackgroundColor = RenderUtils.getColorFromHexString(this.customBackgroundHex);
        }
        this.lastCustomBackgroundHex = this.customBackgroundHex;

        MatrixStack matrix = new MatrixStack();
        float partial = Minecraft.getInstance().getRenderPartialTicks();
        int screenWidth = this.mc.getMainWindow().getScaledWidth();
        int screenHeight = this.mc.getMainWindow().getScaledHeight();

        float elementOpacity = 1.0F;

        //Render background
        if (!this.isEditor) {
            long time = Util.milliTime();
            float f = this.fadeOutStart > -1L ? (float)(time - this.fadeOutStart) / 1000.0F : -1.0F;
            float f1 = this.fadeInStart > -1L ? (float)(time - this.fadeInStart) / 500.0F : -1.0F;
            if (isCustomizationHelperScreen() || DrippyLoadingScreen.isFancyMenuLoaded()) {
                f = 1.0F;
            }
            if (f >= 1.0F) {
                int l = MathHelper.ceil((1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
                if (this.customBackgroundColor != null) {
                    fill(matrix, 0, 0, screenWidth, screenHeight, withAlpha(this.customBackgroundColor.getRGB(), l));
                } else {
                    fill(matrix, 0, 0, screenWidth, screenHeight, withAlpha(backgroundColor2, l));
                }
                elementOpacity = (1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F));
            } else if (this.reloading) {
                int i2 = MathHelper.ceil(MathHelper.clamp(f1, 0.15D, 1.0D) * 255.0D);
                if (this.customBackgroundColor != null) {
                    fill(matrix, 0, 0, screenWidth, screenHeight, withAlpha(this.customBackgroundColor.getRGB(), i2));
                } else {
                    fill(matrix, 0, 0, screenWidth, screenHeight, withAlpha(backgroundColor2, i2));
                }
                elementOpacity = f1;
            } else {
                if (this.customBackgroundColor != null) {
                    fill(matrix, 0, 0, screenWidth, screenHeight, this.customBackgroundColor.getRGB());
                } else {
                    fill(matrix, 0, 0, screenWidth, screenHeight, backgroundColor);
                }
                elementOpacity = 1.0F;
            }
            if (this.backgroundImage != null) {
                Minecraft.getInstance().getTextureManager().bindTexture(this.backgroundImage);
                RenderSystem.enableBlend();
                if (!SplashCustomizationLayer.isCustomizationHelperScreen()) {
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, elementOpacity);
                } else {
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                }
                blit(matrix, 0, 0, 0.0F, 0.0F, screenWidth, screenHeight, screenWidth, screenHeight);
                RenderSystem.disableBlend();
            }
        }

        if (this.isEditor || SplashCustomizationLayer.isCustomizationHelperScreen() || DrippyLoadingScreen.isFancyMenuLoaded()) {
            elementOpacity = 1.0F;
        }

        for (CustomizationItemBase i : this.backgroundElements) {
            i.opacity = elementOpacity;
        }
        for (CustomizationItemBase i : this.foregroundElements) {
            i.opacity = elementOpacity;
        }

        //Render background customization items (including customization handlers for vanilla elements)
        if (!this.isEditor) {
            for (CustomizationItemBase i : this.backgroundElements) {
                i.render(matrix);
            }
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.logoSplashElement.render(matrix, screenWidth, screenHeight, partial);

        this.forgeTextSplashElement.render(matrix, screenWidth, screenHeight, partial);

        this.forgeMemoryInfoSplashElement.render(matrix, screenWidth, screenHeight, partial);

        this.progressBarSplashElement.render(matrix, screenWidth, screenHeight, partial);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        //Render foreground customization items
        if (!this.isEditor) {
            for (CustomizationItemBase i : this.foregroundElements) {
                i.render(matrix);
            }
        }

    }

    public void updateCustomizations() {

        if (this.isEditor) {
            return;
        }

        try {

            this.logoSplashElement.onReloadCustomizations();
            this.forgeTextSplashElement.onReloadCustomizations();
            this.forgeMemoryInfoSplashElement.onReloadCustomizations();
            this.progressBarSplashElement.onReloadCustomizations();

            this.customBackgroundHex = null;
            this.lastCustomBackgroundHex = null;
            this.customBackgroundColor = null;

            this.backgroundImage = null;
            this.backgroundImagePath = null;

            this.foregroundElements.clear();
            this.backgroundElements.clear();

            List<PropertiesSet> props = CustomizationPropertiesHandler.getProperties();

            boolean logoSet = false;
            boolean forgeTextSet = false;
            boolean forgeMemoryInfoSet = false;
            boolean progressBarSet = false;

            for (PropertiesSet s : props) {

                boolean renderInBackground = false;

                List<PropertiesSection> metas = s.getPropertiesOfType("customization-meta");

                if (metas.isEmpty()) {
                    continue;
                }

                String roString = metas.get(0).getEntryValue("renderorder");
                if ((roString != null) && roString.equalsIgnoreCase("background")) {
                    renderInBackground = true;
                }

                String cusBackColorString = metas.get(0).getEntryValue("backgroundcolor");
                if (cusBackColorString != null) {
                    this.customBackgroundHex = cusBackColorString;
                }

                String backgroundImageString = metas.get(0).getEntryValue("backgroundimage");
                if (backgroundImageString != null) {
                    File f = new File(backgroundImageString);
                    if (f.isFile() && (f.getPath().toLowerCase().endsWith(".jpg") || f.getPath().toLowerCase().endsWith(".jpeg") || f.getPath().toLowerCase().endsWith(".png"))) {
                        this.backgroundImagePath = backgroundImageString;
                        ExternalTextureResourceLocation tex = TextureHandler.getResource(backgroundImageString);
                        tex.loadTexture();
                        this.backgroundImage = tex.getResourceLocation();
                    }
                }

                for (PropertiesSection sec : s.getPropertiesOfType("customization")) {
                    String action = sec.getEntryValue("action");

                    if (action != null) {

                        if (!CustomizationHandler.isLightModeEnabled()) {

                            /** ################## VANILLA ELEMENTS / CUSTOMIZATIONS ################## **/

                            /** LOGO **/
                            if (action.equalsIgnoreCase("editlogo")) {
                                this.backgroundElements.add(new LogoSplashCustomizationItem(this.logoSplashElement, sec, logoSet));
                                logoSet = true;
                            }

                            /** FORGE STATUS TEXT **/
                            if (action.equalsIgnoreCase("editforgestatustext")) {
                                this.backgroundElements.add(new ForgeTextSplashCustomizationItem(this.forgeTextSplashElement, sec, forgeTextSet));
                                forgeTextSet = true;
                            }

                            /** FORGE MEMORY INFO **/
                            if (action.equalsIgnoreCase("editforgememoryinfo")) {
                                this.backgroundElements.add(new ForgeMemoryInfoSplashCustomizationItem(this.forgeMemoryInfoSplashElement, sec, forgeMemoryInfoSet));
                                forgeMemoryInfoSet = true;
                            }

                            /** PROGRESS BAR **/
                            if (action.equalsIgnoreCase("editprogressbar")) {
                                this.backgroundElements.add(new ProgressBarSplashCustomizationItem(this.progressBarSplashElement, sec, progressBarSet));
                                progressBarSet = true;
                            }

                        }

                        /** ################## ITEMS ################## **/

                        /** TEXT ELEMENT **/
                        if (action.equalsIgnoreCase("addtext")) {
                            if (renderInBackground) {
                                backgroundElements.add(new StringCustomizationItem(sec));
                            } else {
                                foregroundElements.add(new StringCustomizationItem(sec));
                            }
                        }

                        /** WEB TEXT ELEMENT **/
                        if (action.equalsIgnoreCase("addwebtext")) {
                            if (renderInBackground) {
                                backgroundElements.add(new WebStringCustomizationItem(sec));
                            } else {
                                foregroundElements.add(new WebStringCustomizationItem(sec));
                            }
                        }

                        /** TEXTURE ELEMENT **/
                        if (action.equalsIgnoreCase("addtexture")) {
                            if (renderInBackground) {
                                backgroundElements.add(new TextureCustomizationItem(sec));
                            } else {
                                foregroundElements.add(new TextureCustomizationItem(sec));
                            }
                        }

                        /** WEB TEXTURE ELEMENT **/
                        if (action.equalsIgnoreCase("addwebtexture")) {
                            if (renderInBackground) {
                                backgroundElements.add(new WebTextureCustomizationItem(sec));
                            } else {
                                foregroundElements.add(new WebTextureCustomizationItem(sec));
                            }
                        }

                        /** SHAPE ELEMENT **/
                        if (action.equalsIgnoreCase("addshape")) {
                            if (renderInBackground) {
                                backgroundElements.add(new ShapeCustomizationItem(sec));
                            } else {
                                foregroundElements.add(new ShapeCustomizationItem(sec));
                            }
                        }

                        /** SLIDESHOW ELEMENT **/
                        if (action.equalsIgnoreCase("addslideshow")) {
                            if (renderInBackground) {
                                backgroundElements.add(new SlideshowCustomizationItem(sec));
                            } else {
                                foregroundElements.add(new SlideshowCustomizationItem(sec));
                            }
                        }

                        /** SPLASH TEXT ELEMENT **/
                        if (action.equalsIgnoreCase("addsplash")) {
                            String file = sec.getEntryValue("splashfilepath");
                            String text = sec.getEntryValue("text");
                            if ((file != null) || (text != null)) {

                                SplashTextCustomizationItem i = new SplashTextCustomizationItem(sec);

                                if (renderInBackground) {
                                    backgroundElements.add(i);
                                } else {
                                    foregroundElements.add(i);
                                }

                            }
                        }

                        /** CUSTOM PROGRESS BAR **/
                        if (action.equalsIgnoreCase("addcustomprogressbar")) {
                            if (renderInBackground) {
                                backgroundElements.add(new CustomProgressBarCustomizationItem(sec));
                            } else {
                                foregroundElements.add(new CustomProgressBarCustomizationItem(sec));
                            }
                        }

                        /** ################## CUSTOM ITEMS ################## **/

                        if (action.startsWith("add_")) {
                            String id = action.split("[_]", 2)[1];
                            CustomizationItemContainer c = CustomizationItemRegistry.getInstance().getElement(id);
                            if (c != null) {

                                CustomizationItem i = c.constructWithProperties(sec);
                                if (renderInBackground) {
                                    backgroundElements.add(i);
                                } else {
                                    foregroundElements.add(i);
                                }

                            }
                        }

                    }

                }

            }

            //Add dummy customization items to handle positions of all non-customized elements
            PropertiesSection dummySec = new PropertiesSection("customization");
            if (!logoSet) {
                this.backgroundElements.add(new LogoSplashCustomizationItem(this.logoSplashElement, dummySec, false));
            }
            if (!forgeTextSet) {
                this.backgroundElements.add(new ForgeTextSplashCustomizationItem(this.forgeTextSplashElement, dummySec, false));
            }
            if (!forgeMemoryInfoSet) {
                this.backgroundElements.add(new ForgeMemoryInfoSplashCustomizationItem(this.forgeMemoryInfoSplashElement, dummySec, false));
            }
            if (!progressBarSet) {
                this.backgroundElements.add(new ProgressBarSplashCustomizationItem(this.progressBarSplashElement, dummySec, false));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static int withAlpha(int color, int alpha) {
        return color & 16777215 | alpha << 24;
    }

    public static boolean isCustomizationHelperScreen() {
        return (Minecraft.getInstance().currentScreen instanceof CustomizationHelperScreen);
    }

    public static SplashCustomizationLayer getInstance() {
        if (instance == null) {
            instance = new SplashCustomizationLayer(false);
        }
        return instance;
    }

}
