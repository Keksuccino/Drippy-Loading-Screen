package de.keksuccino.drippyloadingscreen.customization.helper.editor.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.keksuccino.drippyloadingscreen.customization.helper.ui.popup.FHPopup;
import de.keksuccino.drippyloadingscreen.customization.items.CustomizationItemBase;
import de.keksuccino.drippyloadingscreen.customization.items.visibilityrequirements.VisibilityRequirementContainer;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.content.AdvancedImageButton;
import de.keksuccino.konkrete.gui.content.AdvancedTextField;
import de.keksuccino.konkrete.input.CharacterFilter;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class VisibilityRequirementsPopup extends FHPopup {

    protected CustomizationItemBase parent;
    protected List<Requirement> requirements = new ArrayList<Requirement>();
    protected int currentRequirement = 0;

    protected AdvancedButton doneButton;
    protected AdvancedButton leftButton;
    protected AdvancedButton rightButton;

    public VisibilityRequirementsPopup(CustomizationItemBase parent) {
        super(240);
        this.parent = parent;

        this.doneButton = new AdvancedButton(0, 0, 100, 20, Locals.localize("popup.done"), true, (press) -> {
            this.setDisplayed(false);
        });
        this.addButton(this.doneButton);

        this.leftButton = new AdvancedImageButton(0, 0, 20, 20, new ResourceLocation("keksuccino", "arrow_left.png"), true, (press) -> {
            int i = this.currentRequirement - 1;
            if (i >= 0) {
                this.currentRequirement = i;
            }
        });
        this.addButton(this.leftButton);

        this.rightButton = new AdvancedImageButton(0, 0, 20, 20, new ResourceLocation("keksuccino", "arrow_right.png"), true, (press) -> {
            int i = this.currentRequirement + 1;
            if (i <= this.requirements.size() - 1) {
                this.currentRequirement = i;
            }
        });
        this.addButton(this.rightButton);

        this.initRequirements();
    }

    protected void initRequirements() {

        VisibilityRequirementContainer c = this.parent.visibilityRequirementContainer;

//        /** Active Slot **/
//        String activeSlotName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.activeslot");
//        String activeSlotDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.activeslot.desc");
//        String activeSlotValueName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.activeslot.valuename");
//        Requirement activeSlot = new Requirement(this, activeSlotName, activeSlotDesc, activeSlotValueName, c.vrCheckForActiveSlot, c.vrShowIfActiveSlot,
//                (enabledCallback) -> {
//            c.vrCheckForActiveSlot = enabledCallback;
//        }, (showIfCallback) -> {
//            c.vrShowIfActiveSlot = showIfCallback;
//        }, (valueCallback) -> {
//            if ((valueCallback != null) && MathUtils.isInteger(valueCallback)) {
//                c.vrActiveSlot = Integer.parseInt(valueCallback);
//            } else {
//                c.vrActiveSlot = 0;
//            }
//        }, CharacterFilter.getIntegerCharacterFiler(), "" + c.vrActiveSlot);
//        this.requirements.add(activeSlot);
//
//        /** Item In Main Hand **/
//        String itemInMainHandName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.iteminmainhand");
//        String itemInMainHandDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.iteminmainhand.desc");
//        Requirement itemInMainHand = new Requirement(this, itemInMainHandName, itemInMainHandDesc, null, c.vrCheckForItemInMainHand, c.vrShowIfItemInMainHand,
//                (enabledCallback) -> {
//                    c.vrCheckForItemInMainHand = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfItemInMainHand = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(itemInMainHand);
//
//        /** Item In Off Hand **/
//        String itemInOffHandName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.iteminoffhand");
//        String itemInOffHandDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.iteminoffhand.desc");
//        Requirement itemInOffHand = new Requirement(this, itemInOffHandName, itemInOffHandDesc, null, c.vrCheckForItemInOffHand, c.vrShowIfItemInOffHand,
//                (enabledCallback) -> {
//                    c.vrCheckForItemInOffHand = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfItemInOffHand = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(itemInOffHand);
//
//        /** Active Item Name **/
//        String activeItemNameName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.activeitemname");
//        String activeItemNameDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.activeitemname.desc");
//        String activeItemNameValueName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.activeitemname.valuename");
//        Requirement activeItemName = new Requirement(this, activeItemNameName, activeItemNameDesc, activeItemNameValueName, c.vrCheckForActiveItemName, c.vrShowIfActiveItemName,
//                (enabledCallback) -> {
//                    c.vrCheckForActiveItemName = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfActiveItemName = showIfCallback;
//        }, (valueCallback) -> {
//            c.vrActiveItemName = valueCallback;
//        }, null, c.vrActiveItemName);
//        this.requirements.add(activeItemName);
//
//        /** Slot Item Name **/
//        String slotItemNameName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.slotitemname");
//        String slotItemNameDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.slotitemname.desc");
//        String slotItemNameValueName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.slotitemname.valuename");
//        Requirement slotItemName = new Requirement(this, slotItemNameName, slotItemNameDesc, slotItemNameValueName, c.vrCheckForSlotItemName, c.vrShowIfSlotItemName,
//                (enabledCallback) -> {
//                    c.vrCheckForSlotItemName = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfSlotItemName = showIfCallback;
//        }, (valueCallback) -> {
//            if (valueCallback.contains(":")) {
//                String slotString = valueCallback.split("[:]", 2)[0];
//                String itemNameString = valueCallback.split("[:]", 2)[1];
//                if (MathUtils.isInteger(slotString)) {
//                    c.vrSlotItemName = itemNameString;
//                    c.vrSlotItemNameSlot = Integer.parseInt(slotString);
//                }
//            }
//        }, null, c.vrSlotItemNameSlot + ":" + c.vrSlotItemName);
//        this.requirements.add(slotItemName);
//
//        /** Singleplayer **/
//        String singleplayerName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.singleplayer");
//        String singleplayerDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.singleplayer.desc");
//        Requirement singleplayer = new Requirement(this, singleplayerName, singleplayerDesc, slotItemNameValueName, c.vrCheckForSingleplayer, c.vrShowIfSingleplayer,
//                (enabledCallback) -> {
//                    c.vrCheckForSingleplayer = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfSingleplayer = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(singleplayer);
//
//        /** Multiplayer **/
//        String multiplayerName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.multiplayer");
//        String multiplayerDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.multiplayer.desc");
//        Requirement multiplayer = new Requirement(this, multiplayerName, multiplayerDesc, slotItemNameValueName, c.vrCheckForMultiplayer, c.vrShowIfMultiplayer,
//                (enabledCallback) -> {
//                    c.vrCheckForMultiplayer = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfMultiplayer = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(multiplayer);
//
//        /** Player On Ground **/
//        String playerOnGroundName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.playeronground");
//        String playerOnGroundDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.playeronground.desc");
//        Requirement playerOnGround = new Requirement(this, playerOnGroundName, playerOnGroundDesc, slotItemNameValueName, c.vrCheckForPlayerOnGround, c.vrShowIfPlayerOnGround,
//                (enabledCallback) -> {
//                    c.vrCheckForPlayerOnGround = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfPlayerOnGround = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(playerOnGround);
//
//        /** Player Is In Water **/
//        String playerIsInWaterName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.playerisinwater");
//        String playerIsInWaterDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.playerisinwater.desc");
//        Requirement playerIsInWater = new Requirement(this, playerIsInWaterName, playerIsInWaterDesc, slotItemNameValueName, c.vrCheckForPlayerIsInWater, c.vrShowIfPlayerIsInWater,
//                (enabledCallback) -> {
//                    c.vrCheckForPlayerIsInWater = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfPlayerIsInWater = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(playerIsInWater);
//
//        /** Player Underwater **/
//        String playerUnderwaterName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.playerunderwater");
//        String playerUnderwaterDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.playerunderwater.desc");
//        Requirement playerUnderwater = new Requirement(this, playerUnderwaterName, playerUnderwaterDesc, slotItemNameValueName, c.vrCheckForPlayerUnderwater, c.vrShowIfPlayerUnderwater,
//                (enabledCallback) -> {
//                    c.vrCheckForPlayerUnderwater = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfPlayerUnderwater = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(playerUnderwater);
//
//        /** Player Is Riding Horse **/
//        String playerIsRidingHorseName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.playerisridinghorse");
//        String playerIsRidingHorseDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.playerisridinghorse.desc");
//        Requirement playerIsRidingHorse = new Requirement(this, playerIsRidingHorseName, playerIsRidingHorseDesc, slotItemNameValueName, c.vrCheckForPlayerIsRidingHorse, c.vrShowIfPlayerIsRidingHorse,
//                (enabledCallback) -> {
//                    c.vrCheckForPlayerIsRidingHorse = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfPlayerIsRidingHorse = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(playerIsRidingHorse);
//
//        /** Player Is Riding Entity **/
//        String playerIsRidingEntityName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.playerisridingentity");
//        String playerIsRidingEntityDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.playerisridingentity.desc");
//        Requirement playerIsRidingEntitiy = new Requirement(this, playerIsRidingEntityName, playerIsRidingEntityDesc, slotItemNameValueName, c.vrCheckForPlayerIsRidingEntity, c.vrShowIfPlayerIsRidingEntity,
//                (enabledCallback) -> {
//                    c.vrCheckForPlayerIsRidingEntity = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfPlayerIsRidingEntity = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(playerIsRidingEntitiy);
//
//        /** Player Is Running **/
//        String playerIsRunningName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.playerisrunning");
//        String playerIsRunningDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.playerisrunning.desc");
//        Requirement playerIsRunning = new Requirement(this, playerIsRunningName, playerIsRunningDesc, slotItemNameValueName, c.vrCheckForPlayerIsRunning, c.vrShowIfPlayerIsRunning,
//                (enabledCallback) -> {
//                    c.vrCheckForPlayerIsRunning = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfPlayerIsRunning = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(playerIsRunning);
//
//        /** Is Debug Open **/
//        String debugOpenName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.debugopen");
//        String debugOpenDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.debugopen.desc");
//        Requirement debugOpen = new Requirement(this, debugOpenName, debugOpenDesc, slotItemNameValueName, c.vrCheckForDebugOpen, c.vrShowIfDebugOpen,
//                (enabledCallback) -> {
//                    c.vrCheckForDebugOpen = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfDebugOpen = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(debugOpen);
//
//        /** Is Game Paused **/
//        String gamePausedName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.gamepaused");
//        String gamePausedDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.gamepaused.desc");
//        Requirement gamePaused = new Requirement(this, gamePausedName, gamePausedDesc, slotItemNameValueName, c.vrCheckForGamePaused, c.vrShowIfGamePaused,
//                (enabledCallback) -> {
//                    c.vrCheckForGamePaused = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfGamePaused = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(gamePaused);
//
//        /** Is Raining **/
//        String rainingName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.raining");
//        String rainingDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.raining.desc");
//        Requirement raining = new Requirement(this, rainingName, rainingDesc, slotItemNameValueName, c.vrCheckForRaining, c.vrShowIfRaining,
//                (enabledCallback) -> {
//                    c.vrCheckForRaining = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfRaining = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(raining);
//
//        /** Is Thundering **/
//        String thunderingName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.thundering");
//        String thunderingDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.thundering.desc");
//        Requirement thundering = new Requirement(this, thunderingName, thunderingDesc, slotItemNameValueName, c.vrCheckForThundering, c.vrShowIfThundering,
//                (enabledCallback) -> {
//                    c.vrCheckForThundering = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfThundering = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(thundering);
//
//        /** Is Health Lower Than **/
//        String healthLowerThanName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.healthlowerthan");
//        String healthLowerThanDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.healthlowerthan.desc");
//        String healthLowerThanValueName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.healthlowerthan.valuename");
//        Requirement healthLowerThan = new Requirement(this, healthLowerThanName, healthLowerThanDesc, healthLowerThanValueName, c.vrCheckForHealthLowerThan, c.vrShowIfHealthLowerThan,
//                (enabledCallback) -> {
//                    c.vrCheckForHealthLowerThan = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfHealthLowerThan = showIfCallback;
//        }, (valueCallback) -> {
//            if ((valueCallback != null) && MathUtils.isFloat(valueCallback)) {
//                c.vrHealthLowerThan = Float.parseFloat(valueCallback);
//            } else {
//                c.vrHealthLowerThan = 100;
//            }
//        }, CharacterFilter.getDoubleCharacterFiler(), "" + c.vrHealthLowerThan);
//        this.requirements.add(healthLowerThan);
//
//        /** Is Health Lower Than Percent **/
//        String healthLowerThanPercentName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.healthlowerthanpercent");
//        String healthLowerThanPercentDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.healthlowerthanpercent.desc");
//        String healthLowerThanPercentValueName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.healthlowerthanpercent.valuename");
//        Requirement healthLowerThanPercent = new Requirement(this, healthLowerThanPercentName, healthLowerThanPercentDesc, healthLowerThanPercentValueName, c.vrCheckForHealthLowerThanPercent, c.vrShowIfHealthLowerThanPercent,
//                (enabledCallback) -> {
//                    c.vrCheckForHealthLowerThanPercent = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfHealthLowerThanPercent = showIfCallback;
//        }, (valueCallback) -> {
//            if ((valueCallback != null) && MathUtils.isFloat(valueCallback)) {
//                c.vrHealthLowerThanPercent = Float.parseFloat(valueCallback);
//            } else {
//                c.vrHealthLowerThanPercent = 100;
//            }
//        }, CharacterFilter.getDoubleCharacterFiler(), "" + c.vrHealthLowerThanPercent);
//        this.requirements.add(healthLowerThanPercent);
//
//        /** Is Food Lower Than **/
//        String foodLowerThanName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.foodlowerthan");
//        String foodLowerThanDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.foodlowerthan.desc");
//        String foodLowerThanValueName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.foodlowerthan.valuename");
//        Requirement foodLowerThan = new Requirement(this, foodLowerThanName, foodLowerThanDesc, foodLowerThanValueName, c.vrCheckForFoodLowerThan, c.vrShowIfFoodLowerThan,
//                (enabledCallback) -> {
//                    c.vrCheckForFoodLowerThan = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfFoodLowerThan = showIfCallback;
//        }, (valueCallback) -> {
//            if ((valueCallback != null) && MathUtils.isFloat(valueCallback)) {
//                c.vrFoodLowerThan = (int)Float.parseFloat(valueCallback);
//            } else {
//                c.vrFoodLowerThan = 100;
//            }
//        }, CharacterFilter.getIntegerCharacterFiler(), "" + c.vrFoodLowerThan);
//        this.requirements.add(foodLowerThan);
//
//        /** Is Food Lower Than Percent **/
//        String foodLowerThanPercentName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.foodlowerthanpercent");
//        String foodLowerThanPercentDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.foodlowerthanpercent.desc");
//        String foodLowerThanPercentValueName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.foodlowerthanpercent.valuename");
//        Requirement foodLowerThanPercent = new Requirement(this, foodLowerThanPercentName, foodLowerThanPercentDesc, foodLowerThanPercentValueName, c.vrCheckForFoodLowerThanPercent, c.vrShowIfFoodLowerThanPercent,
//                (enabledCallback) -> {
//                    c.vrCheckForFoodLowerThanPercent = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfFoodLowerThanPercent = showIfCallback;
//        }, (valueCallback) -> {
//            if ((valueCallback != null) && MathUtils.isFloat(valueCallback)) {
//                c.vrFoodLowerThanPercent = Float.parseFloat(valueCallback);
//            } else {
//                c.vrFoodLowerThanPercent = 100;
//            }
//        }, CharacterFilter.getDoubleCharacterFiler(), "" + c.vrFoodLowerThanPercent);
//        this.requirements.add(foodLowerThanPercent);
//
//        /** Is Player Withered **/
//        String witheredName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.withered");
//        String witheredDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.withered.desc");
//        Requirement withered = new Requirement(this, witheredName, witheredDesc, slotItemNameValueName, c.vrCheckForWithered, c.vrShowIfWithered,
//                (enabledCallback) -> {
//                    c.vrCheckForWithered = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfWithered = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(withered);
//
//        /** Is Survival **/
//        String survivalName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.survival");
//        String survivalDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.survival.desc");
//        Requirement survival = new Requirement(this, survivalName, survivalDesc, slotItemNameValueName, c.vrCheckForSurvival, c.vrShowIfSurvival,
//                (enabledCallback) -> {
//                    c.vrCheckForSurvival = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfSurvival = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(survival);
//
//        /** Is Creative **/
//        String creativeName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.creative");
//        String creativeDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.creative.desc");
//        Requirement creative = new Requirement(this, creativeName, creativeDesc, slotItemNameValueName, c.vrCheckForCreative, c.vrShowIfCreative,
//                (enabledCallback) -> {
//                    c.vrCheckForCreative = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfCreative = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(creative);
//
//        /** Is Adventure **/
//        String adventureName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.adventure");
//        String adventureDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.adventure.desc");
//        Requirement adventure = new Requirement(this, adventureName, adventureDesc, slotItemNameValueName, c.vrCheckForAdventure, c.vrShowIfAdventure,
//                (enabledCallback) -> {
//                    c.vrCheckForAdventure = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfAdventure = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(adventure);
//
//        /** Is Spectator **/
//        String spectatorName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.spectator");
//        String spectatorDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.spectator.desc");
//        Requirement spectator = new Requirement(this, spectatorName, spectatorDesc, slotItemNameValueName, c.vrCheckForSpectator, c.vrShowIfSpectator,
//                (enabledCallback) -> {
//                    c.vrCheckForSpectator = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfSpectator = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(spectator);
//
//        /** Is Poisoned **/
//        String poisonedName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.poisoned");
//        String poisonedDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.poisoned.desc");
//        Requirement poisoned = new Requirement(this, poisonedName, poisonedDesc, slotItemNameValueName, c.vrCheckForPoisoned, c.vrShowIfPoisoned,
//                (enabledCallback) -> {
//                    c.vrCheckForPoisoned = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfPoisoned = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(poisoned);
//
//        /** Has Bad Stomach **/
//        String badStomachName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.badstomach");
//        String badStomachDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.badstomach.desc");
//        Requirement badStomach = new Requirement(this, badStomachName, badStomachDesc, slotItemNameValueName, c.vrCheckForBadStomach, c.vrShowIfBadStomach,
//                (enabledCallback) -> {
//                    c.vrCheckForBadStomach = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfBadStomach = showIfCallback;
//        }, null, null, null);
//        this.requirements.add(badStomach);
//
//        /** Is World Time Hour **/
//        String worldTimeHourValuePreset = "";
//        for (int i : c.vrWorldTimeHour) {
//            worldTimeHourValuePreset += i + ",";
//        }
//        if (worldTimeHourValuePreset.length() > 0) {
//            worldTimeHourValuePreset = worldTimeHourValuePreset.substring(0, worldTimeHourValuePreset.length() -1);
//        } else {
//            worldTimeHourValuePreset = "1, 4";
//        }
//        CharacterFilter worldTimeHourCharFilter = CharacterFilter.getIntegerCharacterFiler();
//        worldTimeHourCharFilter.addAllowedCharacters(",", " ");
//        String worldTimeHourName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.worldtimehour");
//        String worldTimeHourDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.worldtimehour.desc");
//        String worldTimeHourValueName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.worldtimehour.valuename");
//        Requirement worldTimeHour = new Requirement(this, worldTimeHourName, worldTimeHourDesc, worldTimeHourValueName, c.vrCheckForWorldTimeHour, c.vrShowIfWorldTimeHour,
//                (enabledCallback) -> {
//                    c.vrCheckForWorldTimeHour = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfWorldTimeHour = showIfCallback;
//        }, (valueCallback) -> {
//            if (valueCallback != null) {
//                c.vrWorldTimeHour.clear();
//                if (valueCallback.contains(",")) {
//                    for (String s : valueCallback.replace(" ", "").split("[,]")) {
//                        if (MathUtils.isInteger(s)) {
//                            c.vrWorldTimeHour.add(Integer.parseInt(s));
//                        }
//                    }
//                } else {
//                    if (MathUtils.isInteger(valueCallback.replace(" ", ""))) {
//                        c.vrWorldTimeHour.add(Integer.parseInt(valueCallback.replace(" ", "")));
//                    }
//                }
//            }
//        }, worldTimeHourCharFilter, worldTimeHourValuePreset);
//        this.requirements.add(worldTimeHour);
//
//        /** Is World Time Minute **/
//        String worldTimeMinuteValuePreset = "";
//        for (int i : c.vrWorldTimeMinute) {
//            worldTimeMinuteValuePreset += i + ",";
//        }
//        if (worldTimeMinuteValuePreset.length() > 0) {
//            worldTimeMinuteValuePreset = worldTimeMinuteValuePreset.substring(0, worldTimeMinuteValuePreset.length() -1);
//        } else {
//            worldTimeMinuteValuePreset = "1, 4";
//        }
//        String worldTimeMinuteName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.worldtimeminute");
//        String worldTimeMinuteDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.worldtimeminute.desc");
//        String worldTimeMinuteValueName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.worldtimeminute.valuename");
//        Requirement worldTimeMinute = new Requirement(this, worldTimeMinuteName, worldTimeMinuteDesc, worldTimeMinuteValueName, c.vrCheckForWorldTimeMinute, c.vrShowIfWorldTimeMinute,
//                (enabledCallback) -> {
//                    c.vrCheckForWorldTimeMinute = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfWorldTimeMinute = showIfCallback;
//        }, (valueCallback) -> {
//            if (valueCallback != null) {
//                c.vrWorldTimeMinute.clear();
//                if (valueCallback.contains(",")) {
//                    for (String s : valueCallback.replace(" ", "").split("[,]")) {
//                        if (MathUtils.isInteger(s)) {
//                            c.vrWorldTimeMinute.add(Integer.parseInt(s));
//                        }
//                    }
//                } else {
//                    if (MathUtils.isInteger(valueCallback.replace(" ", ""))) {
//                        c.vrWorldTimeMinute.add(Integer.parseInt(valueCallback.replace(" ", "")));
//                    }
//                }
//            }
//        }, worldTimeHourCharFilter, worldTimeMinuteValuePreset);
//        this.requirements.add(worldTimeMinute);
//
//        /** Is Real Time Hour **/
//        String realTimeHourValuePreset = "";
//        for (int i : c.vrRealTimeHour) {
//            realTimeHourValuePreset += i + ",";
//        }
//        if (realTimeHourValuePreset.length() > 0) {
//            realTimeHourValuePreset = realTimeHourValuePreset.substring(0, realTimeHourValuePreset.length() -1);
//        } else {
//            realTimeHourValuePreset = "1, 4";
//        }
//        String realTimeHourName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.realtimehour");
//        String realTimeHourDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.realtimehour.desc");
//        String realTimeHourValueName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.realtimehour.valuename");
//        Requirement realTimeHour = new Requirement(this, realTimeHourName, realTimeHourDesc, realTimeHourValueName, c.vrCheckForRealTimeHour, c.vrShowIfRealTimeHour,
//                (enabledCallback) -> {
//                    c.vrCheckForRealTimeHour = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfRealTimeHour = showIfCallback;
//        }, (valueCallback) -> {
//            if (valueCallback != null) {
//                c.vrRealTimeHour.clear();
//                if (valueCallback.contains(",")) {
//                    for (String s : valueCallback.replace(" ", "").split("[,]")) {
//                        if (MathUtils.isInteger(s)) {
//                            c.vrRealTimeHour.add(Integer.parseInt(s));
//                        }
//                    }
//                } else {
//                    if (MathUtils.isInteger(valueCallback.replace(" ", ""))) {
//                        c.vrRealTimeHour.add(Integer.parseInt(valueCallback.replace(" ", "")));
//                    }
//                }
//            }
//        }, worldTimeHourCharFilter, realTimeHourValuePreset);
//        this.requirements.add(realTimeHour);
//
//        /** Is Real Time Minute **/
//        String realTimeMinuteValuePreset = "";
//        for (int i : c.vrRealTimeMinute) {
//            realTimeMinuteValuePreset += i + ",";
//        }
//        if (realTimeMinuteValuePreset.length() > 0) {
//            realTimeMinuteValuePreset = realTimeMinuteValuePreset.substring(0, realTimeMinuteValuePreset.length() -1);
//        } else {
//            realTimeMinuteValuePreset = "1, 4";
//        }
//        String realTimeMinuteName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.realtimeminute");
//        String realTimeMinuteDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.realtimeminute.desc");
//        String realTimeMinuteValueName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.realtimeminute.valuename");
//        Requirement realTimeMinute = new Requirement(this, realTimeMinuteName, realTimeMinuteDesc, realTimeMinuteValueName, c.vrCheckForRealTimeMinute, c.vrShowIfRealTimeMinute,
//                (enabledCallback) -> {
//                    c.vrCheckForRealTimeMinute = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfRealTimeMinute = showIfCallback;
//        }, (valueCallback) -> {
//            if (valueCallback != null) {
//                c.vrRealTimeMinute.clear();
//                if (valueCallback.contains(",")) {
//                    for (String s : valueCallback.replace(" ", "").split("[,]")) {
//                        if (MathUtils.isInteger(s)) {
//                            c.vrRealTimeMinute.add(Integer.parseInt(s));
//                        }
//                    }
//                } else {
//                    if (MathUtils.isInteger(valueCallback.replace(" ", ""))) {
//                        c.vrRealTimeMinute.add(Integer.parseInt(valueCallback.replace(" ", "")));
//                    }
//                }
//            }
//        }, worldTimeHourCharFilter, realTimeMinuteValuePreset);
//        this.requirements.add(realTimeMinute);
//
//        /** Is Real Time Second **/
//        String realTimeSecondValuePreset = "";
//        for (int i : c.vrRealTimeSecond) {
//            realTimeSecondValuePreset += i + ",";
//        }
//        if (realTimeSecondValuePreset.length() > 0) {
//            realTimeSecondValuePreset = realTimeSecondValuePreset.substring(0, realTimeSecondValuePreset.length() -1);
//        } else {
//            realTimeSecondValuePreset = "1, 4";
//        }
//        String realTimeSecondName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.realtimesecond");
//        String realTimeSecondDesc = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.realtimesecond.desc");
//        String realTimeSecondValueName = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.realtimesecond.valuename");
//        Requirement realTimeSecond = new Requirement(this, realTimeSecondName, realTimeSecondDesc, realTimeSecondValueName, c.vrCheckForRealTimeSecond, c.vrShowIfRealTimeSecond,
//                (enabledCallback) -> {
//                    c.vrCheckForRealTimeSecond = enabledCallback;
//                }, (showIfCallback) -> {
//            c.vrShowIfRealTimeSecond = showIfCallback;
//        }, (valueCallback) -> {
//            if (valueCallback != null) {
//                c.vrRealTimeSecond.clear();
//                if (valueCallback.contains(",")) {
//                    for (String s : valueCallback.replace(" ", "").split("[,]")) {
//                        if (MathUtils.isInteger(s)) {
//                            c.vrRealTimeSecond.add(Integer.parseInt(s));
//                        }
//                    }
//                } else {
//                    if (MathUtils.isInteger(valueCallback.replace(" ", ""))) {
//                        c.vrRealTimeSecond.add(Integer.parseInt(valueCallback.replace(" ", "")));
//                    }
//                }
//            }
//        }, worldTimeHourCharFilter, realTimeSecondValuePreset);
//        this.requirements.add(realTimeSecond);

    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, Screen renderIn) {
        super.render(matrix, mouseX, mouseY, renderIn);

        int centerX = renderIn.width / 2;
        int centerY = renderIn.height / 2;

        this.doneButton.x = centerX - (this.doneButton.getWidth() / 2);
        this.doneButton.y = centerY + 50;

        this.leftButton.x = centerX - this.leftButton.getWidth() - 135;
        this.leftButton.y = centerY - (this.leftButton.getHeight() / 2);

        this.rightButton.x = centerX + 135;
        this.rightButton.y = centerY - (this.leftButton.getHeight() / 2);

        Requirement r = this.requirements.get(this.currentRequirement);
        if (r != null) {
            r.render(matrix, mouseX, mouseY, renderIn);
        }

        this.renderButtons(matrix, mouseX, mouseY);
    }

    public static class Requirement extends AbstractGui {

        protected VisibilityRequirementsPopup parent;
        protected String name;
        protected String desc;
        protected String valueName;
        protected Consumer<Boolean> enabledCallback;
        protected Consumer<Boolean> showIfCallback;
        protected Consumer<String> valueCallback;
        protected CharacterFilter valueFilter;
        protected boolean enabled;
        protected boolean showIf;
        protected String valueString;

        protected List<Runnable> preRenderTasks = new ArrayList<Runnable>();
        protected List<AdvancedButton> buttonList = new ArrayList<AdvancedButton>();

        protected AdvancedButton enableRequirementButton;
        protected AdvancedButton showIfButton;
        protected AdvancedButton showIfNotButton;
        protected AdvancedTextField valueTextField;

        public Requirement(VisibilityRequirementsPopup parent, String name, String desc, @Nullable String valueName, boolean enabled, boolean showIf, Consumer<Boolean> enabledCallback, Consumer<Boolean> showIfCallback, @Nullable Consumer<String> valueCallback, CharacterFilter valueFilter, String valueString) {
            this.parent = parent;
            this.name = name;
            this.desc = desc;
            this.valueName = valueName;
            this.enabledCallback = enabledCallback;
            this.showIfCallback = showIfCallback;
            this.valueCallback = valueCallback;
            this.valueFilter = valueFilter;
            this.enabled = enabled;
            this.showIf = showIf;
            this.valueString = valueString;
            this.init();
        }

        protected void init() {

            /** Toggle Requirement Button **/
            String enabledString = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.enabled", this.name);
            if (!this.enabled) {
                enabledString = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.disabled", this.name);
            }
            this.enableRequirementButton = new AdvancedButton(0, 0, 150, 20, enabledString, true, (press) -> {
                if (this.enabled) {
                    this.enabled = false;
                    this.enabledCallback.accept(false);
                    ((AdvancedButton)press).setMessage(Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.disabled", this.name));
                } else {
                    this.enabled = true;
                    this.enabledCallback.accept(true);
                    ((AdvancedButton)press).setMessage(Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.enabled", this.name));
                }
            });
            List<String> descLines = new ArrayList<String>();
            descLines.addAll(Arrays.asList(StringUtils.splitLines(this.desc, "%n%")));
            descLines.add("");
            descLines.add(Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.toggle.btn.desc"));
            this.enableRequirementButton.setDescription(descLines.toArray(new String[0]));
            this.preRenderTasks.add(() -> enableRequirementButton.setWidth(Minecraft.getInstance().fontRenderer.getStringPropertyWidth(enableRequirementButton.getMessage()) + 10));
            this.addButton(this.enableRequirementButton);

            /** Show If Button **/
            String showIfString = "§a" + Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.showif");
            if (!this.showIf) {
                showIfString = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.showif");
            }
            this.showIfButton = new AdvancedButton(0, 0, 100, 20, showIfString, true, (press) -> {
                this.showIf = true;
                this.showIfCallback.accept(true);
                ((AdvancedButton)press).setMessage("§a" + Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.showif"));
                this.showIfNotButton.setMessage(Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.showifnot"));
            });
            this.showIfButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.showif.btn.desc"), "%n%"));
            this.addButton(this.showIfButton);

            /** Show If Not Button **/
            String showIfNotString = "§a" + Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.showifnot");
            if (this.showIf) {
                showIfNotString = Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.showifnot");
            }
            this.showIfNotButton = new AdvancedButton(0, 0, 100, 20, showIfNotString, true, (press) -> {
                this.showIf = false;
                this.showIfCallback.accept(false);
                ((AdvancedButton)press).setMessage("§a" + Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.showifnot"));
                this.showIfButton.setMessage(Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.showif"));
            });
            this.showIfNotButton.setDescription(StringUtils.splitLines(Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.showifnot.btn.desc"), "%n%"));
            this.addButton(this.showIfNotButton);

            if ((this.valueCallback != null) && (this.valueName != null)) {
                this.valueTextField = new AdvancedTextField(Minecraft.getInstance().fontRenderer, 0, 0, 150, 20, true, this.valueFilter);
                this.valueTextField.setCanLoseFocus(true);
                this.valueTextField.setFocused2(false);
                this.valueTextField.setMaxStringLength(1000);
                if (this.valueString != null) {
                    this.valueTextField.setText(this.valueString);
                }
            }

        }

        public void render(MatrixStack matrix, int mouseX, int mouseY, Screen renderIn) {

            for (Runnable r : this.preRenderTasks) {
                r.run();
            }

            float partial = Minecraft.getInstance().getRenderPartialTicks();
            int centerX = renderIn.width / 2;
            int centerY = renderIn.height / 2;

            drawCenteredString(matrix, Minecraft.getInstance().fontRenderer, Locals.localize("drippyloadingscreen.helper.creator.items.visibilityrequirements.requirement") + ":", centerX, centerY - 83, -1);
            this.enableRequirementButton.x = centerX - (this.enableRequirementButton.getWidth() / 2);
            this.enableRequirementButton.y = centerY - 70;

            this.showIfButton.x = centerX - this.showIfButton.getWidth() - 5;
            this.showIfButton.y = centerY - 40;
            this.showIfNotButton.active = this.enabled;

            this.showIfNotButton.x = centerX + 5;
            this.showIfNotButton.y = centerY - 40;
            this.showIfButton.active = this.enabled;

            if (this.valueTextField != null) {
                drawCenteredString(matrix, Minecraft.getInstance().fontRenderer, this.valueName + ":", centerX, centerY - 10, -1);

                this.valueTextField.x = centerX - (this.valueTextField.getWidth() / 2);
                this.valueTextField.y = centerY + 3;
                this.valueTextField.render(matrix, mouseX, mouseY, partial);
                this.valueTextField.active = this.enabled;
                this.valueTextField.setEnabled(this.enabled);
                this.valueCallback.accept(this.valueTextField.getText());
                this.valueString = this.valueTextField.getText();
            }

            this.renderButtons(matrix, mouseX, mouseY, partial);

        }

        protected void renderButtons(MatrixStack matrix, int mouseX, int mouseY, float partial) {
            for (AdvancedButton b : this.buttonList) {
                b.render(matrix, mouseX, mouseY, partial);
            }
        }

        protected void addButton(AdvancedButton b) {
            if (!this.buttonList.contains(b)) {
                this.buttonList.add(b);
                b.ignoreBlockedInput = true;
                this.parent.colorizePopupButton(b);
            }
        }

    }

}
