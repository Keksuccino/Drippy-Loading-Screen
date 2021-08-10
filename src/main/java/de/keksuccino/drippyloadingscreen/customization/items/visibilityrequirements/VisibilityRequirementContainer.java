package de.keksuccino.drippyloadingscreen.customization.items.visibilityrequirements;

import de.keksuccino.drippyloadingscreen.customization.items.CustomizationItemBase;
import de.keksuccino.drippyloadingscreen.customization.rendering.ItemRenderUtils;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import net.minecraft.item.AirItem;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public class VisibilityRequirementContainer {

    //Visibility Requirements
    //VR show-if values are always the requirement that must be met to show the element.
    //So if the system should check for the main hand item and it's show-if value is set to FALSE, the element is visible if NO ITEM IS IN THE MAIN HAND.
    //---------
    public boolean vrCheckForActiveSlot = false;
    public boolean vrShowIfActiveSlot = false;
    public int vrActiveSlot = -1000;
    //---------
    public boolean vrCheckForItemInMainHand = false;
    public boolean vrShowIfItemInMainHand = false;
    //---------
    public boolean vrCheckForItemInOffHand = false;
    public boolean vrShowIfItemInOffHand = false;
    //---------
    public boolean vrCheckForActiveItemType = false;
    public boolean vrShowIfActiveItemType = false;
    public String vrActiveItemType = "weapon"; //weapon, tool, food, block, potion, misc
    //---------
    public boolean vrCheckForActiveItemName = false;
    public boolean vrShowIfActiveItemName = false;
    public String vrActiveItemName = "minecraft:stick";
    //---------
    public boolean vrCheckForSingleplayer = false;
    public boolean vrShowIfSingleplayer = false;
    //---------
    public boolean vrCheckForMultiplayer = false;
    public boolean vrShowIfMultiplayer = false;
    //---------
    public boolean vrCheckForPlayerOnGround = false;
    public boolean vrShowIfPlayerOnGround = false;
    //---------
    public boolean vrCheckForPlayerUnderwater = false;
    public boolean vrShowIfPlayerUnderwater = false;
    //---------
    public boolean vrCheckForPlayerIsRidingHorse = false;
    public boolean vrShowIfPlayerIsRidingHorse = false;
    //---------
    public boolean vrCheckForPlayerIsRidingEntity = false;
    public boolean vrShowIfPlayerIsRidingEntity = false;
    //---------
    public boolean vrCheckForPlayerIsInWater = false;
    public boolean vrShowIfPlayerIsInWater = false;
    //---------
    public boolean vrCheckForPlayerIsRunning = false;
    public boolean vrShowIfPlayerIsRunning = false;
    //---------
    public boolean vrCheckForSlotItemName = false;
    public boolean vrShowIfSlotItemName = false;
    public String vrSlotItemName = "minecraft:stick";
    public int vrSlotItemNameSlot = 0;
    //---------
    public boolean vrCheckForDebugOpen = false;
    public boolean vrShowIfDebugOpen = false;
    //---------
    public boolean vrCheckForGamePaused = false;
    public boolean vrShowIfGamePaused = false;
    //---------
    public boolean vrCheckForRaining = false;
    public boolean vrShowIfRaining = false;
    //---------
    public boolean vrCheckForThundering = false;
    public boolean vrShowIfThundering = false;
    //---------
    public boolean vrCheckForHealthLowerThan = false;
    public boolean vrShowIfHealthLowerThan = false;
    public float vrHealthLowerThan = 100.0F;
    //---------
    public boolean vrCheckForHealthLowerThanPercent = false;
    public boolean vrShowIfHealthLowerThanPercent = false;
    public float vrHealthLowerThanPercent = 100.0F;
    //---------
    public boolean vrCheckForFoodLowerThan = false;
    public boolean vrShowIfFoodLowerThan = false;
    public int vrFoodLowerThan = 100;
    //---------
    public boolean vrCheckForFoodLowerThanPercent = false;
    public boolean vrShowIfFoodLowerThanPercent = false;
    public float vrFoodLowerThanPercent = 100.0F;
    //---------
    public boolean vrCheckForWithered = false;
    public boolean vrShowIfWithered = false;
    //---------
    public boolean vrCheckForSurvival = false;
    public boolean vrShowIfSurvival = false;
    //---------
    public boolean vrCheckForCreative = false;
    public boolean vrShowIfCreative = false;
    //---------
    public boolean vrCheckForAdventure = false;
    public boolean vrShowIfAdventure = false;
    //---------
    public boolean vrCheckForSpectator = false;
    public boolean vrShowIfSpectator = false;
    //---------
    public boolean vrCheckForPoisoned = false;
    public boolean vrShowIfPoisoned = false;
    //---------
    public boolean vrCheckForBadStomach = false;
    public boolean vrShowIfBadStomach = false;
    //---------
    public boolean vrCheckForWorldTimeHour = false;
    public boolean vrShowIfWorldTimeHour = false;
    public List<Integer> vrWorldTimeHour = new ArrayList<Integer>();
    //---------
    public boolean vrCheckForWorldTimeMinute = false;
    public boolean vrShowIfWorldTimeMinute = false;
    public List<Integer> vrWorldTimeMinute = new ArrayList<Integer>();
    //---------
    public boolean vrCheckForRealTimeHour = false;
    public boolean vrShowIfRealTimeHour = false;
    public List<Integer> vrRealTimeHour = new ArrayList<Integer>();
    //---------
    public boolean vrCheckForRealTimeMinute = false;
    public boolean vrShowIfRealTimeMinute = false;
    public List<Integer> vrRealTimeMinute = new ArrayList<Integer>();
    //---------
    public boolean vrCheckForRealTimeSecond = false;
    public boolean vrShowIfRealTimeSecond = false;
    public List<Integer> vrRealTimeSecond = new ArrayList<Integer>();
    //---------

    public CustomizationItemBase item;

    public VisibilityRequirementContainer(PropertiesSection properties, CustomizationItemBase item) {

        this.item = item;

        //VR: Active Slot
        String vrStringShowIfActiveSlot = properties.getEntryValue("vr:showif:activeslot");
        if (vrStringShowIfActiveSlot != null) {
            if (vrStringShowIfActiveSlot.equalsIgnoreCase("true")) {
                this.vrShowIfActiveSlot = true;
            }
            String activeSlot = properties.getEntryValue("vr:value:activeslot");
            if ((activeSlot != null) && MathUtils.isInteger(activeSlot)) {
                this.vrCheckForActiveSlot = true;
                this.vrActiveSlot = Integer.parseInt(activeSlot);
            }
        }

        //VR: Is Item In Main Hand
        String vrStringShowIfItemInMainHand = properties.getEntryValue("vr:showif:iteminmainhand");
        if (vrStringShowIfItemInMainHand != null) {
            this.vrCheckForItemInMainHand = true;
            if (vrStringShowIfItemInMainHand.equalsIgnoreCase("true")) {
                this.vrShowIfItemInMainHand = true;
            }
        }

        //VR: Is Item In Off Hand
        String vrStringShowIfItemInOffHand = properties.getEntryValue("vr:showif:iteminoffhand");
        if (vrStringShowIfItemInOffHand != null) {
            this.vrCheckForItemInOffHand = true;
            if (vrStringShowIfItemInOffHand.equalsIgnoreCase("true")) {
                this.vrShowIfItemInOffHand = true;
            }
        }

        //VR: Active Item Type
        String vrStringShowIfActiveItemType = properties.getEntryValue("vr:showif:activeitemtype");
        if (vrStringShowIfActiveItemType != null) {
            if (vrStringShowIfActiveItemType.equalsIgnoreCase("true")) {
                this.vrShowIfActiveItemType = true;
            }
            String itemType = properties.getEntryValue("vr:value:activeitemtype");
            if (itemType != null) {
                //TODO enum for item types
                if (itemType.equals("weapon") || itemType.equals("tool") || itemType.equals("food") || itemType.equals("block") || itemType.equals("potion")) {
                    this.vrCheckForActiveItemType = true;
                    this.vrActiveItemType = itemType;
                }
            }
        }

        //VR: Active Item Name
        String vrStringShowIfActiveItemName = properties.getEntryValue("vr:showif:activeitemname");
        if (vrStringShowIfActiveItemName != null) {
            if (vrStringShowIfActiveItemName.equalsIgnoreCase("true")) {
                this.vrShowIfActiveItemName = true;
            }
            String itemName = properties.getEntryValue("vr:value:activeitemname");
            if (itemName != null) {
                Item i = ItemRenderUtils.getItemByName(itemName);
                if ((i != null) && (itemName.equalsIgnoreCase("minecraft:air") || !(i instanceof AirItem))) {
                    this.vrCheckForActiveItemName = true;
                    this.vrActiveItemName = itemName;
                }
            }
        }

        //VR: Slot Item Name
        String vrStringShowIfSlotItemName = properties.getEntryValue("vr:showif:slotitemname");
        if (vrStringShowIfSlotItemName != null) {
            if (vrStringShowIfSlotItemName.equalsIgnoreCase("true")) {
                this.vrShowIfSlotItemName = true;
            }
            String slotAndItemName = properties.getEntryValue("vr:value:slotitemname");
            if ((slotAndItemName != null) && slotAndItemName.contains(":")) {
                String slotString = slotAndItemName.split("[:]", 2)[0];
                String itemNameString = slotAndItemName.split("[:]", 2)[1];
                if (MathUtils.isInteger(slotString)) {
                    Item i = ItemRenderUtils.getItemByName(itemNameString);
                    if ((i != null) && (itemNameString.equalsIgnoreCase("minecraft:air") || !(i instanceof AirItem))) {
                        this.vrCheckForSlotItemName = true;
                        this.vrSlotItemName = itemNameString;
                        this.vrSlotItemNameSlot = Integer.parseInt(slotString);
                    }
                }
            }
        }

        //VR: Is Singleplayer
        String vrStringShowIfSingleplayer = properties.getEntryValue("vr:showif:singleplayer");
        if (vrStringShowIfSingleplayer != null) {
            this.vrCheckForSingleplayer = true;
            if (vrStringShowIfSingleplayer.equalsIgnoreCase("true")) {
                this.vrShowIfSingleplayer = true;
            }
        }

        //VR: Is Multiplayer
        String vrStringShowIfMultiplayer = properties.getEntryValue("vr:showif:multiplayer");
        if (vrStringShowIfMultiplayer != null) {
            this.vrCheckForMultiplayer = true;
            if (vrStringShowIfMultiplayer.equalsIgnoreCase("true")) {
                this.vrShowIfMultiplayer = true;
            }
        }

        //VR: Player On Ground
        String vrStringShowIfPlayerOnGround = properties.getEntryValue("vr:showif:playeronground");
        if (vrStringShowIfPlayerOnGround != null) {
            this.vrCheckForPlayerOnGround = true;
            if (vrStringShowIfPlayerOnGround.equalsIgnoreCase("true")) {
                this.vrShowIfPlayerOnGround = true;
            }
        }

        //VR: Player Underwater
        String vrStringShowIfPlayerUnderwater = properties.getEntryValue("vr:showif:playerunderwater");
        if (vrStringShowIfPlayerUnderwater != null) {
            this.vrCheckForPlayerUnderwater = true;
            if (vrStringShowIfPlayerUnderwater.equalsIgnoreCase("true")) {
                this.vrShowIfPlayerUnderwater = true;
            }
        }

        //VR: Player Is Riding Horse
        String vrStringShowIfPlayerIsRidingHorse = properties.getEntryValue("vr:showif:playerisridinghorse");
        if (vrStringShowIfPlayerIsRidingHorse != null) {
            this.vrCheckForPlayerIsRidingHorse = true;
            if (vrStringShowIfPlayerIsRidingHorse.equalsIgnoreCase("true")) {
                this.vrShowIfPlayerIsRidingHorse = true;
            }
        }

        //VR: Player Is Riding Entity
        String vrStringShowIfPlayerIsRidingEntity = properties.getEntryValue("vr:showif:playerisridingentity");
        if (vrStringShowIfPlayerIsRidingEntity != null) {
            this.vrCheckForPlayerIsRidingEntity = true;
            if (vrStringShowIfPlayerIsRidingEntity.equalsIgnoreCase("true")) {
                this.vrShowIfPlayerIsRidingEntity = true;
            }
        }

        //VR: Player Is In Water
        String vrStringShowIfPlayerIsInWater = properties.getEntryValue("vr:showif:playerisinwater");
        if (vrStringShowIfPlayerIsInWater != null) {
            this.vrCheckForPlayerIsInWater = true;
            if (vrStringShowIfPlayerIsInWater.equalsIgnoreCase("true")) {
                this.vrShowIfPlayerIsInWater = true;
            }
        }

        //VR: Player Is Running
        String vrStringShowIfPlayerIsRunning = properties.getEntryValue("vr:showif:playerisrunning");
        if (vrStringShowIfPlayerIsRunning != null) {
            this.vrCheckForPlayerIsRunning = true;
            if (vrStringShowIfPlayerIsRunning.equalsIgnoreCase("true")) {
                this.vrShowIfPlayerIsRunning = true;
            }
        }

        //VR: Is Debug Open
        String vrStringShowIfDebugOpen = properties.getEntryValue("vr:showif:debugopen");
        if (vrStringShowIfDebugOpen != null) {
            this.vrCheckForDebugOpen = true;
            if (vrStringShowIfDebugOpen.equalsIgnoreCase("true")) {
                this.vrShowIfDebugOpen = true;
            }
        }

        //VR: Is Game Paused
        String vrStringShowIfGamePaused = properties.getEntryValue("vr:showif:gamepaused");
        if (vrStringShowIfGamePaused != null) {
            this.vrCheckForGamePaused = true;
            if (vrStringShowIfGamePaused.equalsIgnoreCase("true")) {
                this.vrShowIfGamePaused = true;
            }
        }

        //VR: Is Raining
        String vrStringShowIfRaining = properties.getEntryValue("vr:showif:raining");
        if (vrStringShowIfRaining != null) {
            this.vrCheckForRaining = true;
            if (vrStringShowIfRaining.equalsIgnoreCase("true")) {
                this.vrShowIfRaining = true;
            }
        }

        //VR: Is Thundering
        String vrStringShowIfThundering = properties.getEntryValue("vr:showif:thundering");
        if (vrStringShowIfThundering != null) {
            this.vrCheckForThundering = true;
            if (vrStringShowIfThundering.equalsIgnoreCase("true")) {
                this.vrShowIfThundering = true;
            }
        }

        //VR: Health Lower Than
        String vrStringShowIfHealthLowerThan = properties.getEntryValue("vr:showif:healthlowerthan");
        if (vrStringShowIfHealthLowerThan != null) {
            if (vrStringShowIfHealthLowerThan.equalsIgnoreCase("true")) {
                this.vrShowIfHealthLowerThan = true;
            }
            String healthLowerThan = properties.getEntryValue("vr:value:healthlowerthan");
            if ((healthLowerThan != null) && MathUtils.isFloat(healthLowerThan)) {
                this.vrCheckForHealthLowerThan = true;
                this.vrHealthLowerThan = Float.parseFloat(healthLowerThan);
            }
        }

        //VR: Health Lower Than Percent
        String vrStringShowIfHealthLowerThanPercent = properties.getEntryValue("vr:showif:healthlowerthanpercent");
        if (vrStringShowIfHealthLowerThanPercent != null) {
            if (vrStringShowIfHealthLowerThanPercent.equalsIgnoreCase("true")) {
                this.vrShowIfHealthLowerThanPercent = true;
            }
            String healthLowerThanPercent = properties.getEntryValue("vr:value:healthlowerthanpercent");
            if ((healthLowerThanPercent != null) && MathUtils.isFloat(healthLowerThanPercent)) {
                this.vrCheckForHealthLowerThanPercent = true;
                this.vrHealthLowerThanPercent = Float.parseFloat(healthLowerThanPercent);
            }
        }

        //VR: Health Lower Than
        String vrStringShowIfFoodLowerThan = properties.getEntryValue("vr:showif:foodlowerthan");
        if (vrStringShowIfFoodLowerThan != null) {
            if (vrStringShowIfFoodLowerThan.equalsIgnoreCase("true")) {
                this.vrShowIfFoodLowerThan = true;
            }
            String foodLowerThan = properties.getEntryValue("vr:value:foodlowerthan");
            if ((foodLowerThan != null) && MathUtils.isFloat(foodLowerThan)) {
                this.vrCheckForFoodLowerThan = true;
                this.vrFoodLowerThan = (int)Float.parseFloat(foodLowerThan);
            }
        }

        //VR: Food Lower Than Percent
        String vrStringShowIfFoodLowerThanPercent = properties.getEntryValue("vr:showif:foodlowerthanpercent");
        if (vrStringShowIfFoodLowerThanPercent != null) {
            if (vrStringShowIfFoodLowerThanPercent.equalsIgnoreCase("true")) {
                this.vrShowIfFoodLowerThanPercent = true;
            }
            String foodLowerThanPercent = properties.getEntryValue("vr:value:foodlowerthanpercent");
            if ((foodLowerThanPercent != null) && MathUtils.isFloat(foodLowerThanPercent)) {
                this.vrCheckForFoodLowerThanPercent = true;
                this.vrFoodLowerThanPercent = Float.parseFloat(foodLowerThanPercent);
            }
        }

        //VR: Is Player Withered
        String vrStringShowIfWithered = properties.getEntryValue("vr:showif:withered");
        if (vrStringShowIfWithered != null) {
            this.vrCheckForWithered = true;
            if (vrStringShowIfWithered.equalsIgnoreCase("true")) {
                this.vrShowIfWithered = true;
            }
        }

        //VR: Is Survival
        String vrStringShowIfSurvival = properties.getEntryValue("vr:showif:survival");
        if (vrStringShowIfSurvival != null) {
            this.vrCheckForSurvival = true;
            if (vrStringShowIfSurvival.equalsIgnoreCase("true")) {
                this.vrShowIfSurvival = true;
            }
        }

        //VR: Is Creative
        String vrStringShowIfCreative = properties.getEntryValue("vr:showif:creative");
        if (vrStringShowIfCreative != null) {
            this.vrCheckForCreative = true;
            if (vrStringShowIfCreative.equalsIgnoreCase("true")) {
                this.vrShowIfCreative = true;
            }
        }

        //VR: Is Adventure
        String vrStringShowIfAdventure = properties.getEntryValue("vr:showif:adventure");
        if (vrStringShowIfAdventure != null) {
            this.vrCheckForAdventure = true;
            if (vrStringShowIfAdventure.equalsIgnoreCase("true")) {
                this.vrShowIfAdventure = true;
            }
        }

        //VR: Is Spectator
        String vrStringShowIfSpectator = properties.getEntryValue("vr:showif:spectator");
        if (vrStringShowIfSpectator != null) {
            this.vrCheckForSpectator = true;
            if (vrStringShowIfSpectator.equalsIgnoreCase("true")) {
                this.vrShowIfSpectator = true;
            }
        }

        //VR: Is Poisoned
        String vrStringShowIfPoisoned = properties.getEntryValue("vr:showif:poisoned");
        if (vrStringShowIfPoisoned != null) {
            this.vrCheckForPoisoned = true;
            if (vrStringShowIfPoisoned.equalsIgnoreCase("true")) {
                this.vrShowIfPoisoned = true;
            }
        }

        //VR: Has Bad Stomach
        String vrStringShowIfBadStomach = properties.getEntryValue("vr:showif:badstomach");
        if (vrStringShowIfBadStomach != null) {
            this.vrCheckForBadStomach = true;
            if (vrStringShowIfBadStomach.equalsIgnoreCase("true")) {
                this.vrShowIfBadStomach = true;
            }
        }

        //VR: Is World Time Hour
        String vrStringShowIfWorldTimeHour = properties.getEntryValue("vr:showif:worldtimehour");
        if (vrStringShowIfWorldTimeHour != null) {
            if (vrStringShowIfWorldTimeHour.equalsIgnoreCase("true")) {
                this.vrShowIfWorldTimeHour = true;
            }
            String worldTimeHour = properties.getEntryValue("vr:value:worldtimehour");
            if (worldTimeHour != null) {
                this.vrWorldTimeHour.clear();
                if (worldTimeHour.contains(",")) {
                    for (String s : worldTimeHour.replace(" ", "").split("[,]")) {
                        if (MathUtils.isInteger(s)) {
                            this.vrWorldTimeHour.add(Integer.parseInt(s));
                        }
                    }
                } else {
                    if (MathUtils.isInteger(worldTimeHour.replace(" ", ""))) {
                        this.vrWorldTimeHour.add(Integer.parseInt(worldTimeHour.replace(" ", "")));
                    }
                }
                if (!this.vrWorldTimeHour.isEmpty()) {
                    this.vrCheckForWorldTimeHour = true;
                }
            }
        }

        //VR: Is World Time Minute
        String vrStringShowIfWorldTimeMinute = properties.getEntryValue("vr:showif:worldtimeminute");
        if (vrStringShowIfWorldTimeMinute != null) {
            if (vrStringShowIfWorldTimeMinute.equalsIgnoreCase("true")) {
                this.vrShowIfWorldTimeMinute = true;
            }
            String worldTimeMinute = properties.getEntryValue("vr:value:worldtimeminute");
            if (worldTimeMinute != null) {
                this.vrWorldTimeMinute.clear();
                if (worldTimeMinute.contains(",")) {
                    for (String s : worldTimeMinute.replace(" ", "").split("[,]")) {
                        if (MathUtils.isInteger(s)) {
                            this.vrWorldTimeMinute.add(Integer.parseInt(s));
                        }
                    }
                } else {
                    if (MathUtils.isInteger(worldTimeMinute.replace(" ", ""))) {
                        this.vrWorldTimeMinute.add(Integer.parseInt(worldTimeMinute.replace(" ", "")));
                    }
                }
                if (!this.vrWorldTimeMinute.isEmpty()) {
                    this.vrCheckForWorldTimeMinute = true;
                }
            }
        }

        //VR: Is Real Time Hour
        String vrStringShowIfRealTimeHour = properties.getEntryValue("vr:showif:realtimehour");
        if (vrStringShowIfRealTimeHour != null) {
            if (vrStringShowIfRealTimeHour.equalsIgnoreCase("true")) {
                this.vrShowIfRealTimeHour = true;
            }
            String realTimeHour = properties.getEntryValue("vr:value:realtimehour");
            if (realTimeHour != null) {
                this.vrRealTimeHour.clear();
                if (realTimeHour.contains(",")) {
                    for (String s : realTimeHour.replace(" ", "").split("[,]")) {
                        if (MathUtils.isInteger(s)) {
                            this.vrRealTimeHour.add(Integer.parseInt(s));
                        }
                    }
                } else {
                    if (MathUtils.isInteger(realTimeHour.replace(" ", ""))) {
                        this.vrRealTimeHour.add(Integer.parseInt(realTimeHour.replace(" ", "")));
                    }
                }
                if (!this.vrRealTimeHour.isEmpty()) {
                    this.vrCheckForRealTimeHour = true;
                }
            }
        }

        //VR: Is Real Time Minute
        String vrStringShowIfRealTimeMinute = properties.getEntryValue("vr:showif:realtimeminute");
        if (vrStringShowIfRealTimeMinute != null) {
            if (vrStringShowIfRealTimeMinute.equalsIgnoreCase("true")) {
                this.vrShowIfRealTimeMinute = true;
            }
            String realTimeMinute = properties.getEntryValue("vr:value:realtimeminute");
            if (realTimeMinute != null) {
                this.vrRealTimeMinute.clear();
                if (realTimeMinute.contains(",")) {
                    for (String s : realTimeMinute.replace(" ", "").split("[,]")) {
                        if (MathUtils.isInteger(s)) {
                            this.vrRealTimeMinute.add(Integer.parseInt(s));
                        }
                    }
                } else {
                    if (MathUtils.isInteger(realTimeMinute.replace(" ", ""))) {
                        this.vrRealTimeMinute.add(Integer.parseInt(realTimeMinute.replace(" ", "")));
                    }
                }
                if (!this.vrRealTimeMinute.isEmpty()) {
                    this.vrCheckForRealTimeMinute = true;
                }
            }
        }

        //VR: Is Real Time Second
        String vrStringShowIfRealTimeSecond = properties.getEntryValue("vr:showif:realtimesecond");
        if (vrStringShowIfRealTimeSecond != null) {
            if (vrStringShowIfRealTimeSecond.equalsIgnoreCase("true")) {
                this.vrShowIfRealTimeSecond = true;
            }
            String realTimeSecond = properties.getEntryValue("vr:value:realtimesecond");
            if (realTimeSecond != null) {
                this.vrRealTimeSecond.clear();
                if (realTimeSecond.contains(",")) {
                    for (String s : realTimeSecond.replace(" ", "").split("[,]")) {
                        if (MathUtils.isInteger(s)) {
                            this.vrRealTimeSecond.add(Integer.parseInt(s));
                        }
                    }
                } else {
                    if (MathUtils.isInteger(realTimeSecond.replace(" ", ""))) {
                        this.vrRealTimeSecond.add(Integer.parseInt(realTimeSecond.replace(" ", "")));
                    }
                }
                if (!this.vrRealTimeSecond.isEmpty()) {
                    this.vrCheckForRealTimeSecond = true;
                }
            }
        }

    }

    public boolean isVisible() {

        //VR: Active Slot
        if (this.vrCheckForActiveSlot) {
            if (this.vrShowIfActiveSlot) {
                if (this.vrActiveSlot != VisibilityRequirementHandler.activeSlot) {
                    return false;
                }
            } else {
                if (this.vrActiveSlot == VisibilityRequirementHandler.activeSlot) {
                    return false;
                }
            }
        }

        //VR: Is Item In Main Hand
        if (this.vrCheckForItemInMainHand) {
            boolean isItemInHand = VisibilityRequirementHandler.isItemInMainHand;
            if (this.vrShowIfItemInMainHand) {
                if (!isItemInHand) {
                    return false;
                }
            } else {
                if (isItemInHand) {
                    return false;
                }
            }
        }

        //VR: Is Item In Off Hand
        if (this.vrCheckForItemInOffHand) {
            boolean isItemInHand = VisibilityRequirementHandler.isItemInOffHand;
            if (this.vrShowIfItemInOffHand) {
                if (!isItemInHand) {
                    return false;
                }
            } else {
                if (isItemInHand) {
                    return false;
                }
            }
        }

        //VR: Active Item Type
        if (this.vrCheckForActiveItemType) {
            //TODO implementieren
        }

        //VR: Active Item Name
        if (this.vrCheckForActiveItemName) {
            String itemName = VisibilityRequirementHandler.activeItemName;
            if (itemName != null) {
                if (this.vrShowIfActiveItemName) {
                    if (!itemName.equalsIgnoreCase(this.vrActiveItemName)) {
                        return false;
                    }
                } else {
                    if (itemName.equalsIgnoreCase(this.vrActiveItemName)) {
                        return false;
                    }
                }
            }
        }

        //VR: Slot Item Name
        if (this.vrCheckForSlotItemName) {
            String itemName = VisibilityRequirementHandler.inventoryItemNames.get(this.vrSlotItemNameSlot);
            if (itemName != null) {
                if (this.vrShowIfSlotItemName) {
                    if (!itemName.equalsIgnoreCase(this.vrSlotItemName)) {
                        return false;
                    }
                } else {
                    if (itemName.equalsIgnoreCase(this.vrSlotItemName)) {
                        return false;
                    }
                }
            }
        }

        //VR: Is Singleplayer
        if (this.vrCheckForSingleplayer) {
            if (this.vrShowIfSingleplayer) {
                if (!VisibilityRequirementHandler.isSingleplayer) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.isSingleplayer) {
                    return false;
                }
            }
        }

        //VR: Is Multiplayer
        if (this.vrCheckForMultiplayer) {
            if (this.vrShowIfMultiplayer) {
                if (VisibilityRequirementHandler.isSingleplayer) {
                    return false;
                }
            } else {
                if (!VisibilityRequirementHandler.isSingleplayer) {
                    return false;
                }
            }
        }

        //VR: Player On Ground
        if (this.vrCheckForPlayerOnGround) {
            if (this.vrShowIfPlayerOnGround) {
                if (!VisibilityRequirementHandler.isPlayerOnGround) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.isPlayerOnGround) {
                    return false;
                }
            }
        }

        //VR: Player Underwater
        if (this.vrCheckForPlayerUnderwater) {
            if (this.vrShowIfPlayerUnderwater) {
                if (!VisibilityRequirementHandler.isPlayerUnderwater) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.isPlayerUnderwater) {
                    return false;
                }
            }
        }

        //VR: Player Is Riding Horse
        if (this.vrCheckForPlayerIsRidingHorse) {
            if (this.vrShowIfPlayerIsRidingHorse) {
                if (!VisibilityRequirementHandler.isPlayerRidingHorse) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.isPlayerRidingHorse) {
                    return false;
                }
            }
        }

        //VR: Player Is Riding Entity
        if (this.vrCheckForPlayerIsRidingEntity) {
            if (this.vrShowIfPlayerIsRidingEntity) {
                if (!VisibilityRequirementHandler.isPlayerRidingEntity) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.isPlayerRidingEntity) {
                    return false;
                }
            }
        }

        //VR: Player Is In Water
        if (this.vrCheckForPlayerIsInWater) {
            if (this.vrShowIfPlayerIsInWater) {
                if (!VisibilityRequirementHandler.isPlayerInWater) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.isPlayerInWater) {
                    return false;
                }
            }
        }

        //VR: Player Is Running
        if (this.vrCheckForPlayerIsRunning) {
            if (this.vrShowIfPlayerIsRunning) {
                if (!VisibilityRequirementHandler.isPlayerRunning) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.isPlayerRunning) {
                    return false;
                }
            }
        }

        //VR: Is Debug Open
        if (this.vrCheckForDebugOpen) {
            if (this.vrShowIfDebugOpen) {
                if (!VisibilityRequirementHandler.isDebugOpen) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.isDebugOpen) {
                    return false;
                }
            }
        }

        //VR: Is Game Paused
        if (this.vrCheckForGamePaused) {
            if (this.vrShowIfGamePaused) {
                if (!VisibilityRequirementHandler.isGamePaused) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.isGamePaused) {
                    return false;
                }
            }
        }

        //VR: Is Raining
        if (this.vrCheckForRaining) {
            if (this.vrShowIfRaining) {
                if (!VisibilityRequirementHandler.isRaining) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.isRaining) {
                    return false;
                }
            }
        }

        //VR: Is Thundering
        if (this.vrCheckForThundering) {
            if (this.vrShowIfThundering) {
                if (!VisibilityRequirementHandler.isThundering) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.isThundering) {
                    return false;
                }
            }
        }

        //VR: Is Health Lower Than
        if (this.vrCheckForHealthLowerThan) {
            if (this.vrShowIfHealthLowerThan) {
                if (VisibilityRequirementHandler.playerHealth > this.vrHealthLowerThan) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.playerHealth < this.vrHealthLowerThan) {
                    return false;
                }
            }
        }

        //VR: Is Health Lower Than Percent
        if (this.vrCheckForHealthLowerThanPercent) {
            if (this.vrShowIfHealthLowerThanPercent) {
                if (VisibilityRequirementHandler.playerHealthPercent > this.vrHealthLowerThanPercent) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.playerHealthPercent < this.vrHealthLowerThanPercent) {
                    return false;
                }
            }
        }

        //VR: Is Food Lower Than
        if (this.vrCheckForFoodLowerThan) {
            if (this.vrShowIfFoodLowerThan) {
                if (VisibilityRequirementHandler.playerFood > this.vrFoodLowerThan) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.playerFood < this.vrFoodLowerThan) {
                    return false;
                }
            }
        }

        //VR: Is Food Lower Than Percent
        if (this.vrCheckForFoodLowerThanPercent) {
            if (this.vrShowIfFoodLowerThanPercent) {
                if (VisibilityRequirementHandler.playerFoodPercent > this.vrFoodLowerThanPercent) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.playerFoodPercent < this.vrFoodLowerThanPercent) {
                    return false;
                }
            }
        }

        //VR: Is Player Withered
        if (this.vrCheckForWithered) {
            if (this.vrShowIfWithered) {
                if (!VisibilityRequirementHandler.isPlayerWithered) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.isPlayerWithered) {
                    return false;
                }
            }
        }

        //VR: Is Survival
        if (this.vrCheckForSurvival) {
            if (this.vrShowIfSurvival) {
                if (!VisibilityRequirementHandler.isSurvival) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.isSurvival) {
                    return false;
                }
            }
        }

        //VR: Is Creative
        if (this.vrCheckForCreative) {
            if (this.vrShowIfCreative) {
                if (!VisibilityRequirementHandler.isCreative) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.isCreative) {
                    return false;
                }
            }
        }

        //VR: Is Adventure
        if (this.vrCheckForAdventure) {
            if (this.vrShowIfAdventure) {
                if (!VisibilityRequirementHandler.isAdventure) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.isAdventure) {
                    return false;
                }
            }
        }

        //VR: Is Spectator
        if (this.vrCheckForSpectator) {
            if (this.vrShowIfSpectator) {
                if (!VisibilityRequirementHandler.isSpectator) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.isSpectator) {
                    return false;
                }
            }
        }

        //VR: Is Poisoned
        if (this.vrCheckForPoisoned) {
            if (this.vrShowIfPoisoned) {
                if (!VisibilityRequirementHandler.isPlayerPoisoned) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.isPlayerPoisoned) {
                    return false;
                }
            }
        }

        //VR: Has Bad Stomach
        if (this.vrCheckForBadStomach) {
            if (this.vrShowIfBadStomach) {
                if (!VisibilityRequirementHandler.hasPlayerBadStomach) {
                    return false;
                }
            } else {
                if (VisibilityRequirementHandler.hasPlayerBadStomach) {
                    return false;
                }
            }
        }

        //VR: Is World Time Hour
        if (this.vrCheckForWorldTimeHour) {
            if (this.vrShowIfWorldTimeHour) {
                if (!this.vrWorldTimeHour.contains(VisibilityRequirementHandler.worldTimeHour)) {
                    return false;
                }
            } else {
                if (this.vrWorldTimeHour.contains(VisibilityRequirementHandler.worldTimeHour)) {
                    return false;
                }
            }
        }

        //VR: Is World Time Minute
        if (this.vrCheckForWorldTimeMinute) {
            if (this.vrShowIfWorldTimeMinute) {
                if (!this.vrWorldTimeMinute.contains(VisibilityRequirementHandler.worldTimeMinute)) {
                    return false;
                }
            } else {
                if (this.vrWorldTimeMinute.contains(VisibilityRequirementHandler.worldTimeMinute)) {
                    return false;
                }
            }
        }

        //VR: Is Real Time Hour
        if (this.vrCheckForRealTimeHour) {
            if (this.vrShowIfRealTimeHour) {
                if (!this.vrRealTimeHour.contains(VisibilityRequirementHandler.realTimeHour)) {
                    return false;
                }
            } else {
                if (this.vrRealTimeHour.contains(VisibilityRequirementHandler.realTimeHour)) {
                    return false;
                }
            }
        }

        //VR: Is Real Time Minute
        if (this.vrCheckForRealTimeMinute) {
            if (this.vrShowIfRealTimeMinute) {
                if (!this.vrRealTimeMinute.contains(VisibilityRequirementHandler.realTimeMinute)) {
                    return false;
                }
            } else {
                if (this.vrRealTimeMinute.contains(VisibilityRequirementHandler.realTimeMinute)) {
                    return false;
                }
            }
        }

        //VR: Is Real Time Second
        if (this.vrCheckForRealTimeSecond) {
            if (this.vrShowIfRealTimeSecond) {
                if (!this.vrRealTimeSecond.contains(VisibilityRequirementHandler.realTimeSecond)) {
                    return false;
                }
            } else {
                if (this.vrRealTimeSecond.contains(VisibilityRequirementHandler.realTimeSecond)) {
                    return false;
                }
            }
        }

        return true;

    }

}
