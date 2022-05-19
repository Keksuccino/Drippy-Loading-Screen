package de.keksuccino.drippyloadingscreen.customization.rendering.splash;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.api.item.CustomizationItem;
import de.keksuccino.drippyloadingscreen.api.item.CustomizationItemContainer;
import de.keksuccino.drippyloadingscreen.api.item.CustomizationItemRegistry;
import de.keksuccino.drippyloadingscreen.customization.CustomizationHandler;
import de.keksuccino.drippyloadingscreen.customization.CustomizationPropertiesHandler;
import de.keksuccino.drippyloadingscreen.customization.helper.CustomizationHelperScreen;
import de.keksuccino.drippyloadingscreen.customization.items.*;
import de.keksuccino.drippyloadingscreen.customization.items.custombars.CustomProgressBarCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.items.vanilla.LogoSplashCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.items.vanilla.ProgressBarSplashCustomizationItem;
import de.keksuccino.drippyloadingscreen.customization.placeholdervalues.PlaceholderTextValueHelper;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements.LogoSplashElement;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.elements.ProgressBarSplashElement;
import de.keksuccino.drippyloadingscreen.events.CustomizationSystemReloadedEvent;
import de.keksuccino.drippyloadingscreen.events.OverlayOpenEvent;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.properties.PropertiesSet;
import de.keksuccino.konkrete.rendering.CurrentScreenHandler;
import de.keksuccino.konkrete.rendering.RenderUtils;
import de.keksuccino.konkrete.resources.ExternalTextureResourceLocation;
import de.keksuccino.konkrete.resources.TextureHandler;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.awt.Color;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class SplashCustomizationLayer extends GuiComponent {

    protected static SplashCustomizationLayer instance;

    private static int backgroundColor = FastColor.ARGB32.color(255, 239, 50, 61);
    private static int backgroundColor2 = backgroundColor & 16777215;

    public final LogoSplashElement logoSplashElement = new LogoSplashElement(this);
    public final ProgressBarSplashElement progressBarSplashElement = new ProgressBarSplashElement(this);

    public String customBackgroundHex = null;
    protected String lastCustomBackgroundHex = null;
    public Color customBackgroundColor;

    public ExternalTextureResourceLocation backgroundImageSource = null;
    public ResourceLocation backgroundImage = null;
    public String backgroundImagePath = null;
    public boolean keepBackgroundAspectRatio = false;

    public boolean scaled = false;
    public boolean fadeOut = true;

    public final boolean isEditor;
    public boolean isNewLoadingScreen = true;

    /** GETTER ONLY **/
    public ReloadInstance reload;
    /** GETTER ONLY **/
    public Consumer<Optional<Throwable>> onFinish;
    /** GETTER ONLY **/
    public boolean fadeIn;
    /** GETTER ONLY **/
    public long fadeOutStart;
    /** GETTER ONLY **/
    public long fadeInStart;
    /** GETTER ONLY **/
    public float currentProgress;

    public List<CustomizationItemBase> backgroundElements = new ArrayList<CustomizationItemBase>();
    public List<CustomizationItemBase> foregroundElements = new ArrayList<CustomizationItemBase>();

    protected Map<String, RandomLayoutContainer> randomLayoutGroups = new HashMap<String, RandomLayoutContainer>();

    protected Minecraft mc = Minecraft.getInstance();

    public SplashCustomizationLayer(boolean isEditor) {
        this.isEditor = isEditor;
        this.updateCustomizations();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onCustomizationSystemReloaded(CustomizationSystemReloadedEvent e) {
        for (RandomLayoutContainer c : this.randomLayoutGroups.values()) {
            c.lastLayoutPath = null;
        }
        PlaceholderTextValueHelper.randomTextIntervals.clear();
        this.updateCustomizations();
    }

    @SubscribeEvent
    public void onOverlayOpenEvent(OverlayOpenEvent e) {
        this.isNewLoadingScreen = true;
    }

    public void renderLayer() {

        List<Runnable> runs = new ArrayList<>();
        runs.addAll(CustomizationHandler.mainThreadTasks);
        for (Runnable r : runs) {
            try {
                r.run();
                CustomizationHandler.mainThreadTasks.remove(r);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if ((this.customBackgroundHex != null) && !this.customBackgroundHex.equals(this.lastCustomBackgroundHex)) {
            this.customBackgroundColor = RenderUtils.getColorFromHexString(this.customBackgroundHex);
        }
        this.lastCustomBackgroundHex = this.customBackgroundHex;

        if ((Minecraft.getInstance() == null) || (Minecraft.getInstance().getWindow() == null)) {
            return;
        }

        PoseStack matrix = new PoseStack();
        float partial = Minecraft.getInstance().getFrameTime();
        int screenWidth = this.mc.getWindow().getGuiScaledWidth();
        int screenHeight = this.mc.getWindow().getGuiScaledHeight();

        float elementOpacity = 1.0F;

        //Render background
        if (!this.isEditor) {
            long time = Util.getMillis();
            float f = this.fadeOutStart > -1L ? (float)(time - this.fadeOutStart) / 1000.0F : -1.0F;
            float f1 = this.fadeInStart > -1L ? (float)(time - this.fadeInStart) / 500.0F : -1.0F;
            if (isCustomizationHelperScreen() || DrippyLoadingScreen.isFancyMenuLoaded() || !this.fadeOut) {
                f = 1.0F;
            }
            if (f >= 1.0F) {
                int l = Mth.ceil((1.0F - Mth.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
                if (this.customBackgroundColor != null) {
                    fill(matrix, 0, 0, screenWidth, screenHeight, withAlpha(this.customBackgroundColor.getRGB(), l));
                } else {
                    fill(matrix, 0, 0, screenWidth, screenHeight, withAlpha(backgroundColor2, l));
                }
                elementOpacity = (1.0F - Mth.clamp(f - 1.0F, 0.0F, 1.0F));
            } else if (this.fadeIn) {
                int i2 = Mth.ceil(Mth.clamp(f1, 0.15D, 1.0D) * 255.0D);
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
            if (elementOpacity > 1.0F) {
                elementOpacity = 1.0F;
            }
            if (elementOpacity < 0.0F) {
                elementOpacity = 0.0F;
            }
            if (this.backgroundImage != null) {
                RenderUtils.bindTexture(this.backgroundImage);
                if (!SplashCustomizationLayer.isCustomizationHelperScreen()) {
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, elementOpacity);
                } else {
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                }
                if (!this.keepBackgroundAspectRatio) {
                    blit(matrix, 0, 0, 0.0F, 0.0F, screenWidth, screenHeight, screenWidth, screenHeight);
                } else {
                    int w = this.backgroundImageSource.getWidth();
                    int h = this.backgroundImageSource.getHeight();
                    double ratio = (double) w / (double) h;
                    int wfinal = (int)(screenHeight * ratio);
                    int screenCenterX = screenWidth / 2;
                    if (wfinal < screenWidth) {
                        GuiComponent.blit(CurrentScreenHandler.getPoseStack(), 0, 0, 1.0F, 1.0F, screenWidth + 1, screenHeight + 1, screenWidth + 1, screenHeight + 1);
                    } else {
                        GuiComponent.blit(CurrentScreenHandler.getPoseStack(), screenCenterX - (wfinal / 2), 0, 1.0F, 1.0F, wfinal + 1, screenHeight + 1, wfinal + 1, screenHeight + 1);
                    }
                }
                RenderSystem.disableBlend();
            }
        }

        if (this.isEditor || SplashCustomizationLayer.isCustomizationHelperScreen() || DrippyLoadingScreen.isFancyMenuLoaded() || !this.fadeOut) {
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
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        this.logoSplashElement.render(matrix, screenWidth, screenHeight, partial);

        this.progressBarSplashElement.render(matrix, screenWidth, screenHeight, partial);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

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
            this.progressBarSplashElement.onReloadCustomizations();

            this.customBackgroundHex = null;
            this.lastCustomBackgroundHex = null;
            this.customBackgroundColor = null;

            this.backgroundImageSource = null;
            this.backgroundImage = null;
            this.backgroundImagePath = null;
            this.keepBackgroundAspectRatio = false;

            this.foregroundElements.clear();
            this.backgroundElements.clear();

            this.scaled = false;
            this.fadeOut = true;

            List<PropertiesSet> propsRaw = CustomizationPropertiesHandler.getProperties();
            List<PropertiesSet> normalLayouts = new ArrayList<PropertiesSet>();
            List<PropertiesSet> layouts = new ArrayList<PropertiesSet>();

            String randomDefaultGroup = "-100397";

            for (RandomLayoutContainer c : this.randomLayoutGroups.values()) {
                c.onlyFirstTime = false;
                c.clearLayouts();
            }

            boolean logoSet = false;
            boolean forgeTextSet = false;
            boolean forgeMemoryInfoSet = false;
            boolean progressBarSet = false;

            for (PropertiesSet s : propsRaw) {

                List<PropertiesSection> metas = s.getPropertiesOfType("customization-meta");

                if (metas.isEmpty()) {
                    continue;
                }

                String randomMode = metas.get(0).getEntryValue("randommode");
                if ((randomMode != null) && randomMode.equalsIgnoreCase("true")) {

                    String group = metas.get(0).getEntryValue("randomgroup");
                    if (group == null) {
                        group = randomDefaultGroup;
                    }
                    if (!this.randomLayoutGroups.containsKey(group)) {
                        this.randomLayoutGroups.put(group, new RandomLayoutContainer(group, this));
                    }
                    RandomLayoutContainer c = this.randomLayoutGroups.get(group);
                    if (c != null) {
                        String randomOnlyFirstTime = metas.get(0).getEntryValue("randomonlyfirsttime");
                        if ((randomOnlyFirstTime != null) && randomOnlyFirstTime.equalsIgnoreCase("true")) {
                            c.setOnlyFirstTime(true);
                        }
                        c.addLayout(s);
                    }

                } else {

                    normalLayouts.add(s);

                }

            }

            List<String> trashLayoutGroups = new ArrayList<String>();
            for (Map.Entry<String, RandomLayoutContainer> m : this.randomLayoutGroups.entrySet()) {
                if (m.getValue().getLayouts().isEmpty()) {
                    trashLayoutGroups.add(m.getKey());
                }
            }
            for (String s : trashLayoutGroups) {
                this.randomLayoutGroups.remove(s);
            }

            for (RandomLayoutContainer c : this.randomLayoutGroups.values()) {
                PropertiesSet s = c.getRandomLayout();
                if (s != null) {
                    layouts.add(s);
                }
            }
            layouts.addAll(normalLayouts);

            for (PropertiesSet s : layouts) {

                boolean renderInBackground = false;

                List<PropertiesSection> metas = s.getPropertiesOfType("customization-meta");

                if (metas.isEmpty()) {
                    continue;
                }

                String roString = metas.get(0).getEntryValue("renderorder");
                if ((roString != null) && roString.equalsIgnoreCase("background")) {
                    renderInBackground = true;
                }

                Window w = Minecraft.getInstance().getWindow();
                String scaleString = metas.get(0).getEntryValue("scale");
                if ((scaleString != null) && (MathUtils.isInteger(scaleString.replace(" ", "")) || MathUtils.isDouble(scaleString.replace(" ", "")))) {
                    int newscale = (int) Double.parseDouble(scaleString.replace(" ", ""));
                    if (newscale <= 0) {
                        newscale = 1;
                    }
                    w.setGuiScale((double)newscale);
                    if (mc.screen != null) {
                        mc.screen.width = w.getGuiScaledWidth();
                        mc.screen.height = w.getGuiScaledHeight();
                    }
                    this.scaled = true;
                }

                //Handle auto-scaling
                int autoScaleBaseWidth = 0;
                int autoScaleBaseHeight = 0;
                String baseWidth = metas.get(0).getEntryValue("autoscale_basewidth");
                String baseHeight = metas.get(0).getEntryValue("autoscale_baseheight");
                if ((baseWidth != null) && (baseHeight != null) && MathUtils.isInteger(baseWidth) && MathUtils.isInteger(baseHeight)) {
                    autoScaleBaseWidth = Integer.parseInt(baseWidth);
                    autoScaleBaseHeight = Integer.parseInt(baseHeight);
                }
                if ((autoScaleBaseWidth != 0) && (autoScaleBaseHeight != 0)) {
                    double guiWidth = w.getScreenWidth();
                    double guiHeight = w.getScreenHeight();
                    double percentX = (guiWidth / (double)autoScaleBaseWidth) * 100.0D;
                    double percentY = (guiHeight / (double)autoScaleBaseHeight) * 100.0D;
                    double newScaleX = (percentX / 100.0D) * w.getGuiScale();
                    double newScaleY = (percentY / 100.0D) * w.getGuiScale();
                    double newScale = Math.min(newScaleX, newScaleY);

                    w.setGuiScale(newScale);
                    if (mc.screen != null) {
                        mc.screen.width = w.getGuiScaledWidth();
                        mc.screen.height = w.getGuiScaledHeight();
                    }
                    this.scaled = true;
                }

                String fadeOutString = metas.get(0).getEntryValue("fadeout");
                if ((fadeOutString != null) && fadeOutString.equalsIgnoreCase("false")) {
                    this.fadeOut = false;
                }

                String cusBackColorString = metas.get(0).getEntryValue("backgroundcolor");
                if (cusBackColorString != null) {
                    this.customBackgroundHex = cusBackColorString;
                }

                String keepAspect = metas.get(0).getEntryValue("keepaspectratio");
                if ((keepAspect != null) && keepAspect.equalsIgnoreCase("true")) {
                    this.keepBackgroundAspectRatio = true;
                }

                String backgroundImageString = metas.get(0).getEntryValue("backgroundimage");
                if (backgroundImageString != null) {
                    File f = new File(backgroundImageString);
                    if (f.isFile() && (f.getPath().toLowerCase().endsWith(".jpg") || f.getPath().toLowerCase().endsWith(".jpeg") || f.getPath().toLowerCase().endsWith(".png"))) {
                        this.backgroundImagePath = backgroundImageString;
                        ExternalTextureResourceLocation tex = TextureHandler.getResource(backgroundImageString);
                        tex.loadTexture();
                        this.backgroundImageSource = tex;
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

//                            /** FORGE STATUS TEXT **/
//                            if (action.equalsIgnoreCase("editforgestatustext")) {
//                                this.backgroundElements.add(new ForgeTextSplashCustomizationItem(this.forgeTextSplashElement, sec, forgeTextSet));
//                                forgeTextSet = true;
//                            }
//
//                            /** FORGE MEMORY INFO **/
//                            if (action.equalsIgnoreCase("editforgememoryinfo")) {
//                                this.backgroundElements.add(new ForgeMemoryInfoSplashCustomizationItem(this.forgeMemoryInfoSplashElement, sec, forgeMemoryInfoSet));
//                                forgeMemoryInfoSet = true;
//                            }

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

                        if (action.startsWith("add_")) { //DEPRECATED OLD API
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

                        /** CUSTOM ITEMS (API) **/
                        if (action.startsWith("custom_layout_element:")) {
                            String cusId = action.split("[:]", 2)[1];
                            de.keksuccino.drippyloadingscreen.api.item.v2.CustomizationItemContainer cusItem = de.keksuccino.drippyloadingscreen.api.item.v2.CustomizationItemRegistry.getItem(cusId);
                            if (cusItem != null) {
                                de.keksuccino.drippyloadingscreen.api.item.v2.CustomizationItem cusItemInstance = cusItem.constructCustomizedItemInstance(sec);
                                if (renderInBackground) {
                                    backgroundElements.add(cusItemInstance);
                                } else {
                                    foregroundElements.add(cusItemInstance);
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
//            if (!forgeTextSet) {
//                this.backgroundElements.add(new ForgeTextSplashCustomizationItem(this.forgeTextSplashElement, dummySec, false));
//            }
//            if (!forgeMemoryInfoSet) {
//                this.backgroundElements.add(new ForgeMemoryInfoSplashCustomizationItem(this.forgeMemoryInfoSplashElement, dummySec, false));
//            }
            if (!progressBarSet) {
                this.backgroundElements.add(new ProgressBarSplashCustomizationItem(this.progressBarSplashElement, dummySec, false));
            }

            for (CustomizationItemBase i : this.foregroundElements) {
                if (i.orientation.equals("loading-progress") && (i.orientationElementIdentifier != null)) {
                    i.orientationElement = this.getItemByActionId(i.orientationElementIdentifier);
                }
            }
            for (CustomizationItemBase i : this.backgroundElements) {
                if (i.orientation.equals("loading-progress") && (i.orientationElementIdentifier != null)) {
                    i.orientationElement = this.getItemByActionId(i.orientationElementIdentifier);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.isNewLoadingScreen = false;

    }

    protected CustomizationItemBase getItemByActionId(String actionId) {
        for (CustomizationItemBase c : this.backgroundElements) {
            if (c.getActionId().equals(actionId)) {
                return c;
            }
        }
        for (CustomizationItemBase c : this.foregroundElements) {
            if (c.getActionId().equals(actionId)) {
                return c;
            }
        }
        return null;
    }

    private static int withAlpha(int color, int alpha) {
        return color & 16777215 | alpha << 24;
    }

    public static boolean isCustomizationHelperScreen() {
        return (Minecraft.getInstance().screen instanceof CustomizationHelperScreen);
    }

    public static SplashCustomizationLayer getInstance() {
        if (instance == null) {
            instance = new SplashCustomizationLayer(false);
        }
        return instance;
    }

    public static class RandomLayoutContainer {

        public final String id;
        protected List<PropertiesSet> layouts = new ArrayList<PropertiesSet>();
        protected boolean onlyFirstTime = false;
        protected String lastLayoutPath = null;

        public SplashCustomizationLayer parent;

        public RandomLayoutContainer(String id, SplashCustomizationLayer parent) {
            this.id = id;
            this.parent = parent;
        }

        public List<PropertiesSet> getLayouts() {
            return this.layouts;
        }

        public void addLayout(PropertiesSet layout) {
            this.layouts.add(layout);
        }

        public void addLayouts(List<PropertiesSet> layouts) {
            this.layouts.addAll(layouts);
        }

        public void clearLayouts() {
            this.layouts.clear();
        }

        public void setOnlyFirstTime(boolean b) {
            this.onlyFirstTime = b;
        }

        public boolean isOnlyFirstTime() {
            return this.onlyFirstTime;
        }

        public void resetLastLayout() {
            this.lastLayoutPath = null;
        }

        @Nullable
        public PropertiesSet getRandomLayout() {
            if (!this.layouts.isEmpty()) {
                if ((this.onlyFirstTime || !this.parent.isNewLoadingScreen) && (this.lastLayoutPath != null)) {
                    File f = new File(this.lastLayoutPath);
                    if (f.exists()) {
                        for (PropertiesSet s : this.layouts) {
                            List<PropertiesSection> metas = s.getPropertiesOfType("customization-meta");
                            if (metas.isEmpty()) {
                                metas = s.getPropertiesOfType("type-meta");
                            }
                            if (metas.isEmpty()) {
                                continue;
                            }
                            String path = metas.get(0).getEntryValue("path");
                            if ((path != null) && path.equals(this.lastLayoutPath)) {
                                return s;
                            }
                        }
                    }
                }
                int i = MathUtils.getRandomNumberInRange(0, this.layouts.size()-1);
                PropertiesSet s = this.layouts.get(i);
                List<PropertiesSection> metas = s.getPropertiesOfType("customization-meta");
                if (metas.isEmpty()) {
                    metas = s.getPropertiesOfType("type-meta");
                }
                if (!metas.isEmpty()) {
                    String path = metas.get(0).getEntryValue("path");
                    if ((path != null)) {
                        this.lastLayoutPath = path;
                        return s;
                    }
                }
            }
            return null;
        }

    }

}
