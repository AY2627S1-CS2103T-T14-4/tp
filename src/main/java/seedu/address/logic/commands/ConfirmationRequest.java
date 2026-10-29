package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

/** An immutable prompt and the command to execute if the user confirms it. */
public final class ConfirmationRequest {
    public static final String MESSAGE_CONFIRMATION_SUFFIX = "\nConfirm (Y/n)?";
    public static final String MESSAGE_CANCELLED = "Deletion cancelled.";
    public static final String MESSAGE_INVALID_RESPONSE = "Invalid input.\n";

    private final String prompt;
    private final Command command;

    /** Creates a request for the described operation and its deferred command. */
    public ConfirmationRequest(String description, Command command) {
        this.prompt = requireNonNull(description) + MESSAGE_CONFIRMATION_SUFFIX;
        this.command = requireNonNull(command);
    }

    public String getPrompt() {
        return prompt;
    }

    public Command getCommand() {
        return command;
    }
}
