package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class HelpWindowTest {

    private static final String PAWPALS_USER_GUIDE_URL =
            "https://ay2627s1-cs2103t-t14-4.github.io/tp/UserGuide.html";

    @Test
    public void userGuideUrl_pawPalsGuideUrl() {
        assertEquals(PAWPALS_USER_GUIDE_URL, HelpWindow.USERGUIDE_URL);
    }

    @Test
    public void helpMessage_containsPawPalsGuideUrl() {
        assertTrue(HelpWindow.HELP_MESSAGE.contains(PAWPALS_USER_GUIDE_URL));
    }
}
