package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import de.keksuccino.drippyloadingscreen.customization.backgrounds.Backgrounds;
import de.keksuccino.drippyloadingscreen.customization.backgrounds.color.ColorMenuBackground;
import de.keksuccino.drippyloadingscreen.customization.placeholders.Placeholders;
import de.keksuccino.fancymenu.customization.background.MenuBackground;
import de.keksuccino.fancymenu.customization.element.SerializedElement;
import de.keksuccino.fancymenu.customization.element.anchor.ElementAnchorPoints;
import de.keksuccino.fancymenu.customization.element.elements.Elements;
import de.keksuccino.fancymenu.customization.element.elements.button.vanillawidget.VanillaWidgetElement;
import de.keksuccino.fancymenu.customization.element.elements.button.vanillawidget.VanillaWidgetElementBuilder;
import de.keksuccino.fancymenu.customization.element.elements.progressbar.ProgressBarElement;
import de.keksuccino.fancymenu.customization.layout.Layout;
import de.keksuccino.fancymenu.util.Legacy;
import de.keksuccino.fancymenu.util.MathUtils;
import de.keksuccino.fancymenu.util.SerializationUtils;
import de.keksuccino.fancymenu.util.properties.PropertyContainer;
import de.keksuccino.fancymenu.util.properties.PropertyContainerSet;
import de.keksuccino.fancymenu.util.rendering.DrawableColor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.*;

@Mixin(Layout.class)
public class MixinLayout {

    @Legacy("This converts old v2 elements to its replacements. Remove this in the future.")
    @SuppressWarnings("all")
    @Inject(method = "convertLegacyElements", at = @At("RETURN"), remap = false)
    private static void atReturnConvertLegacyElementsDrippy(PropertyContainerSet layout, CallbackInfoReturnable<@NotNull List<List<?>>> info) {

        List<List<?>> returnValue = info.getReturnValue();
        List<SerializedElement> elements = (List<SerializedElement>) returnValue.get(0);
        List<String> elementOrder = (List<String>) returnValue.get(1);

        for (PropertyContainer sec : layout.getContainersOfType("customization")) {

            String action = sec.getValue("action");
            if (action != null) {

                if (action.startsWith("custom_layout_element:")) {

                    //GENERIC PROGRESS BAR ELEMENT
                    if (action.equals("custom_layout_element:drippy_generic_progress_bar")) {

                        ProgressBarElement element = Elements.PROGRESS_BAR.deserializeElementInternal(convertContainerToSerializedElementDrippy(sec));

                        if (element != null) {

                            element.useProgressForElementAnchor = SerializationUtils.deserializeBoolean(false, sec.getValue("progress_for_element_orientation"));
                            element.progressValueMode = ProgressBarElement.ProgressValueMode.FLOATING_POINT;

                            elements.add(Elements.PROGRESS_BAR.serializeElementInternal(element));
                            elementOrder.add(element.getInstanceIdentifier());

                        }

                    }

                    //LOADING BAR ELEMENT
                    if (action.equals("custom_layout_element:drippy_custom_loading_bar")) {

                        ProgressBarElement element = Elements.PROGRESS_BAR.deserializeElementInternal(convertContainerToSerializedElementDrippy(sec));

                        if (element != null) {

                            element.useProgressForElementAnchor = SerializationUtils.deserializeBoolean(false, sec.getValue("progress_for_element_orientation"));
                            element.progressValueMode = ProgressBarElement.ProgressValueMode.PERCENTAGE;
                            element.progressSource = Placeholders.GAME_LOADING_PROGRESS_PERCENT.getDefaultPlaceholderString().toString();

                            elements.add(Elements.PROGRESS_BAR.serializeElementInternal(element));
                            elementOrder.add(element.getInstanceIdentifier());

                        }

                    }

                }

            }

        }

    }

    @Unique
    private static SerializedElement convertContainerToSerializedElementDrippy(PropertyContainer sec) {
        SerializedElement e = new SerializedElement();
        for (Map.Entry<String, String> m : sec.getProperties().entrySet()) {
            e.putProperty(m.getKey(), m.getValue());
        }
        return e;
    }

    @Legacy("Converts old v2 logo and progress bar deep elements to Vanilla widget elements. Remove this in the future.")
    @Inject(method = "convertLegacyVanillaButtonCustomizations", at = @At("RETURN"), remap = false)
    private static void atReturnConvertLegacyVanillaButtonCustomizationsDrippy(PropertyContainerSet layout, CallbackInfoReturnable<@NotNull List<SerializedElement>> info) {

        List<SerializedElement> returnValue = info.getReturnValue();
        Map<String, VanillaWidgetElement> elements = new HashMap<>();

        for (PropertyContainer sec : layout.getContainersOfType("customization")) {

            String action = sec.getValue("action");
            if (action != null) {

                //MOJANG LOGO
                if (action.equals("deep_customization_element:drippy_overlay_logo")) {

                    VanillaWidgetElement element = elements.get("vanillabtn:mojang_logo");
                    if (element == null) {
                        element = VanillaWidgetElementBuilder.INSTANCE.buildDefaultInstance();
                        element.setInstanceIdentifier("vanillabtn:mojang_logo");
                        elements.put("vanillabtn:mojang_logo", element);
                    }

                    if ("true".equals(sec.getValue("hidden"))) {
                        element.setHidden(true);
                    }

                    if ("false".equals(sec.getValue("original_pos_size_calculation"))) {

                        String x = sec.getValue("x");
                        String y = sec.getValue("y");
                        String w = sec.getValue("width");
                        String h = sec.getValue("height");

                        if ((x != null) && (y != null) && (w != null) && (h != null)) {
                            if (MathUtils.isInteger(x) && MathUtils.isInteger(y) && MathUtils.isInteger(w) && MathUtils.isInteger(h)) {

                                element.posOffsetX = Integer.parseInt(x);
                                element.posOffsetY = Integer.parseInt(y);
                                element.baseWidth = Integer.parseInt(w);
                                element.baseHeight = Integer.parseInt(h);

                                String anchor = sec.getValue("orientation");
                                if (anchor == null) {
                                    element.anchorPoint = ElementAnchorPoints.TOP_LEFT;
                                } else {
                                    element.anchorPoint = Objects.requireNonNullElse(ElementAnchorPoints.getAnchorPointByName(anchor), ElementAnchorPoints.TOP_LEFT);
                                }

                            }
                        }

                    }

                }

                //VANILLA PROGRESS BAR
                if (action.equals("deep_customization_element:drippy_overlay_progress_bar")) {

                    VanillaWidgetElement element = elements.get("vanillabtn:progress_bar");
                    if (element == null) {
                        element = VanillaWidgetElementBuilder.INSTANCE.buildDefaultInstance();
                        element.setInstanceIdentifier("vanillabtn:progress_bar");
                        elements.put("vanillabtn:progress_bar", element);
                    }

                    if ("true".equals(sec.getValue("hidden"))) {
                        element.setHidden(true);
                    }

                    if ("false".equals(sec.getValue("original_pos_size_calculation"))) {

                        String x = sec.getValue("x");
                        String y = sec.getValue("y");
                        String w = sec.getValue("width");
                        String h = sec.getValue("height");

                        if ((x != null) && (y != null) && (w != null) && (h != null)) {
                            if (MathUtils.isInteger(x) && MathUtils.isInteger(y) && MathUtils.isInteger(w) && MathUtils.isInteger(h)) {

                                element.posOffsetX = Integer.parseInt(x);
                                element.posOffsetY = Integer.parseInt(y);
                                element.baseWidth = Integer.parseInt(w);
                                element.baseHeight = Integer.parseInt(h);

                                String anchor = sec.getValue("orientation");
                                if (anchor == null) {
                                    element.anchorPoint = ElementAnchorPoints.TOP_LEFT;
                                } else {
                                    element.anchorPoint = Objects.requireNonNullElse(ElementAnchorPoints.getAnchorPointByName(anchor), ElementAnchorPoints.TOP_LEFT);
                                }

                            }
                        }

                    }

                }

            }

        }

        elements.forEach((s, vanillaWidgetElement) -> {
            returnValue.add(VanillaWidgetElementBuilder.INSTANCE.serializeElementInternal(vanillaWidgetElement));
        });

    }

    @Legacy("Convert old v2 background color deep element to new ColorMenuBackground instance. Remove this in the future.")
    @Inject(method = "convertLegacyMenuBackground", at = @At("RETURN"), cancellable = true, remap = false)
    private static void atReturnConvertLegacyMenuBackgroundDrippy(PropertyContainerSet layout, CallbackInfoReturnable<MenuBackground> info) {

        if (info.getReturnValue() != null) return;

        for (PropertyContainer sec : layout.getContainersOfType("customization")) {

            String action = sec.getValue("action");
            if (action != null) {

                //BACKGROUND COLOR
                if (action.equals("deep_customization_element:drippy_overlay_background")) {

                    ColorMenuBackground background = new ColorMenuBackground(Backgrounds.COLOR_MENU_BACKGROUND);

                    String color = sec.getValue("custom_color_hex");
                    if (color != null) {
                        background.color = DrawableColor.of(color);
                    }

                    info.setReturnValue(background);
                    break;

                }

            }

        }

    }

}
