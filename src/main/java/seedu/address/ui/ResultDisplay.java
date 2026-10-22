package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.FeedbackType;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";

    @FXML
    private TextArea resultDisplay;

    @FXML
    private Label feedbackStatus;

    /**
     * Creates a feedback display with neutral guidance for the first command.
     */
    public ResultDisplay() {
        super(FXML);
        setFeedbackToUser("Enter a command. Use help to view available commands.", FeedbackType.NEUTRAL);
    }

    /**
     * Displays successful feedback without changing the message text.
     */
    public void setFeedbackToUser(String feedbackToUser) {
        setFeedbackToUser(feedbackToUser, FeedbackType.SUCCESS);
    }

    /**
     * Replaces the feedback and its status, clearing the previous outcome's styling and scroll position.
     */
    public void setFeedbackToUser(String feedbackToUser, FeedbackType feedbackType) {
        requireNonNull(feedbackToUser);
        requireNonNull(feedbackType);
        String status = switch (feedbackType) {
            case NEUTRAL -> "Info";
            case SUCCESS -> "Success";
            case ERROR -> "Error";
        };
        getRoot().getStyleClass().removeAll("feedback-neutral", "feedback-success", "feedback-error");
        getRoot().getStyleClass().add("feedback-" + feedbackType.name().toLowerCase(Locale.ROOT));
        feedbackStatus.setText(status);
        resultDisplay.setText(feedbackToUser);
        resultDisplay.positionCaret(0);
        resultDisplay.setScrollTop(0);
        resultDisplay.setScrollLeft(0);
    }

}
