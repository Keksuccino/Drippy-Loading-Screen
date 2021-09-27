package de.keksuccino.drippyloadingscreen.customization.items.visibilityrequirements;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class VisibilityRequirementHandler {

    public static int activeSlot = -1000;
    public static boolean isItemInMainHand = false;
    public static boolean isItemInOffHand = false;
    public static String activeItemType = "weapon"; //weapon, tool, food, block, potion, misc
    public static String activeItemName = null;
    public static boolean isSingleplayer = false;
    public static boolean isPlayerOnGround = false;
    public static boolean isPlayerUnderwater = false;
    public static boolean isPlayerRidingHorse = false;
    public static boolean isPlayerRidingEntity = false;
    public static boolean isPlayerInWater = false;
    public static boolean isPlayerRunning = false;
    public static boolean isDebugOpen = false;
    public static boolean isGamePaused = false;
    public static Map<Integer, String> inventoryItemNames = new HashMap<Integer, String>();
    public static boolean isRaining = false;
    public static boolean isThundering = false;
    public static float playerHealth = 100;
    public static float playerHealthPercent = 100;
    public static int playerFood = 100;
    public static float playerFoodPercent = 100;
    public static boolean isPlayerWithered = false;
    public static boolean isCreative = false;
    public static boolean isSurvival = false;
    public static boolean isAdventure = false;
    public static boolean isSpectator = false;
    public static boolean isPlayerPoisoned = false;
    public static boolean hasPlayerBadStomach = false;
    public static int worldTimeHour = 12;
    public static int worldTimeMinute = 0;
    public static int realTimeHour = 12;
    public static int realTimeMinute = 0;
    public static int realTimeSecond = 0;

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new VisibilityRequirementHandler());
    }

    public static void tick() {

        //VR: Is Debug Open
        isDebugOpen = Minecraft.getInstance().options.renderDebug;

        //VR: Is Game Paused
        isGamePaused = Minecraft.getInstance().isPaused();

        Calendar c = Calendar.getInstance();
        if (c != null) {
            realTimeHour = c.get(Calendar.HOUR_OF_DAY);
            realTimeMinute = c.get(Calendar.MINUTE);
            realTimeSecond = c.get(Calendar.SECOND);
        }

    }

    @SubscribeEvent
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre e) {
        if (e.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            tick();
        }
    }

}
