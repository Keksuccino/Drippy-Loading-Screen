package de.keksuccino.drippyloadingscreen.customization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrippyOverlayScreenTest {

    @Test
    void allowsHintAfterEnteringCustomizationWithoutAnActiveLayout() {
        assertTrue(DrippyOverlayScreen.isCustomizationHintEligible(true, true, false, true, false));
    }

    @Test
    void suppressesHintBeforeEnteringCustomization() {
        assertFalse(DrippyOverlayScreen.isCustomizationHintEligible(false, true, false, true, false));
    }

    @Test
    void suppressesHintOutsideDrippyScreenRendering() {
        assertFalse(DrippyOverlayScreen.isCustomizationHintEligible(true, false, false, true, false));
    }

    @Test
    void suppressesHintWhileTheLoadingOverlayIsActive() {
        assertFalse(DrippyOverlayScreen.isCustomizationHintEligible(true, true, true, true, false));
    }

    @Test
    void suppressesHintWhenALayoutIsAlreadyActive() {
        assertFalse(DrippyOverlayScreen.isCustomizationHintEligible(true, true, false, true, true));
    }

}
