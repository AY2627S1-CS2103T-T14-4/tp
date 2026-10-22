package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxTestUtil.initializeToolkit;
import static seedu.address.testutil.JavaFxTestUtil.runOnFxThread;

import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.FeedbackType;

public class ResultDisplayTest {
    @BeforeAll
    public static void setUpJavaFxToolkit() throws InterruptedException {
        initializeToolkit();
    }

    @Test
    public void constructor_displaysNeutralGuidance() throws Exception {
        runOnFxThread(() -> {
            ResultDisplay display = new ResultDisplay();
            assertEquals("Enter a command. Use help to view available commands.", textArea(display).getText());
            assertState(display, FeedbackType.NEUTRAL);
            return null;
        });
    }

    @Test
    public void setFeedbackToUser_allStateTransitions_replacesStatusAndStyle() throws Exception {
        runOnFxThread(() -> {
            ResultDisplay display = new ResultDisplay();
            for (FeedbackType previous : FeedbackType.values()) {
                for (FeedbackType next : FeedbackType.values()) {
                    display.setFeedbackToUser("Previous", previous);
                    display.setFeedbackToUser("Message\nwith details", next);
                    assertEquals("Message\nwith details", textArea(display).getText());
                    assertState(display, next);
                }
            }
            display.setFeedbackToUser("Compatibility overload");
            assertState(display, FeedbackType.SUCCESS);
            return null;
        });
    }

    @Test
    public void setFeedbackToUser_longMessage_wrapsScrollsAndResetsForNextOutcome() throws Exception {
        runOnFxThread(() -> {
            ResultDisplay display = new ResultDisplay();
            Region root = display.getRoot();
            Scene scene = new Scene(root, 400, 90);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            String message = ("A long feedback line with details that must remain readable. ".repeat(4) + "\n")
                    .repeat(30) + "Final detail";
            display.setFeedbackToUser(message, FeedbackType.ERROR);
            root.applyCss();
            root.layout();
            TextArea textArea = textArea(display);
            assertEquals(message, textArea.getText());
            assertTrue(textArea.isWrapText());
            assertFalse(textArea.isEditable());
            textArea.selectAll();
            assertEquals(message, textArea.getSelectedText());
            textArea.positionCaret(message.length());
            textArea.setScrollTop(Double.MAX_VALUE);
            root.layout();
            assertTrue(textArea.getScrollTop() > 0);
            Label status = (Label) root.lookup("#feedbackStatus");
            assertTrue(status.getHeight() > 0);
            assertTrue(textArea.getHeight() > 0);
            assertTrue(status.getBoundsInParent().getMaxY() <= textArea.getBoundsInParent().getMinY());

            display.setFeedbackToUser("Next result", FeedbackType.NEUTRAL);
            assertEquals(0, textArea.getCaretPosition());
            assertEquals(0, textArea.getScrollTop());
            assertEquals(0, textArea.getScrollLeft());
            assertState(display, FeedbackType.NEUTRAL);
            return null;
        });
    }

    @Test
    public void setFeedbackToUser_nullArgument_preservesPreviousOutcome() throws Exception {
        runOnFxThread(() -> {
            ResultDisplay display = new ResultDisplay();
            display.setFeedbackToUser("Previous", FeedbackType.SUCCESS);
            assertThrows(NullPointerException.class, () -> display.setFeedbackToUser(null, FeedbackType.ERROR));
            assertThrows(NullPointerException.class, () -> display.setFeedbackToUser("Next", null));
            assertEquals("Previous", textArea(display).getText());
            assertState(display, FeedbackType.SUCCESS);
            return null;
        });
    }

    private static TextArea textArea(ResultDisplay display) {
        return (TextArea) display.getRoot().lookup("#resultDisplay");
    }

    private static void assertState(ResultDisplay display, FeedbackType type) {
        String expectedStatus = switch (type) {
            case NEUTRAL -> "Info";
            case SUCCESS -> "Success";
            case ERROR -> "Error";
        };
        assertEquals(expectedStatus, ((Label) display.getRoot().lookup("#feedbackStatus")).getText());
        for (FeedbackType candidate : FeedbackType.values()) {
            String style = "feedback-" + candidate.name().toLowerCase(Locale.ROOT);
            long occurrences = display.getRoot().getStyleClass().stream().filter(style::equals).count();
            assertEquals(candidate == type ? 1L : 0L, occurrences);
        }
    }
}
