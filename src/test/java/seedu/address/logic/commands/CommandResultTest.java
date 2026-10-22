package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CommandResultTest {
    @Test
    public void constructor_defaultFeedbackType_success() {
        assertEquals(FeedbackType.SUCCESS, new CommandResult("feedback").getFeedbackType());
        assertEquals(FeedbackType.SUCCESS, new CommandResult("feedback", true, false).getFeedbackType());
    }

    @Test
    public void constructor_nullFeedbackOrType_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CommandResult(null, FeedbackType.NEUTRAL));
        assertThrows(NullPointerException.class, () -> new CommandResult("feedback", null));
    }

    @Test
    public void equals_explicitFeedbackTypes_comparesTypeAndPreservesActions() {
        for (FeedbackType type : FeedbackType.values()) {
            CommandResult result = new CommandResult("feedback", type, true, false);
            CommandResult same = new CommandResult("feedback", type, true, false);
            assertEquals(type, result.getFeedbackType());
            assertEquals(result, same);
            assertEquals(result.hashCode(), same.hashCode());
            assertTrue(result.isShowHelp());
            assertFalse(result.isExit());
            for (FeedbackType other : FeedbackType.values()) {
                if (other != type) {
                    assertNotEquals(result, new CommandResult("feedback", other, true, false));
                }
            }
        }
    }

    @Test
    public void equals() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns true
        assertTrue(commandResult.equals(new CommandResult("feedback")));
        assertTrue(commandResult.equals(new CommandResult("feedback", false, false)));

        // same object -> returns true
        assertTrue(commandResult.equals(commandResult));

        // null -> returns false
        assertFalse(commandResult.equals(null));

        // different types -> returns false
        assertFalse(commandResult.equals(0.5f));

        // different feedbackToUser value -> returns false
        assertFalse(commandResult.equals(new CommandResult("different")));

        // different showHelp value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", true, false)));

        // different exit value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", false, true)));
    }

    @Test
    public void hashcode() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns same hashcode
        assertEquals(commandResult.hashCode(), new CommandResult("feedback").hashCode());

        // different feedbackToUser value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("different").hashCode());

        // different showHelp value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", true, false).hashCode());

        // different exit value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", false, true).hashCode());
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser() + ", feedbackType=" + commandResult.getFeedbackType()
                + ", showHelp=" + commandResult.isShowHelp()
                + ", exit=" + commandResult.isExit() + "}";
        assertEquals(expected, commandResult.toString());
    }
}
