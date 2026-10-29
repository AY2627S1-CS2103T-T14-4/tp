package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.PawPals;

/**
 * Clears the PawPals data.
 */
public class ClearCommand extends Command implements ConfirmableCommand {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "PawPals has been cleared!";
    private static final String DESCRIPTION = "Clearing all client data:";
    public static final String MESSAGE_CONFIRMATION = DESCRIPTION + ConfirmationRequest.MESSAGE_CONFIRMATION_SUFFIX;
    public static final String MESSAGE_CANCELLED = ConfirmationRequest.MESSAGE_CANCELLED;
    public static final String MESSAGE_INVALID_CONFIRMATION =
            ConfirmationRequest.MESSAGE_INVALID_RESPONSE + MESSAGE_CONFIRMATION;

    @Override
    public ConfirmationRequest prepareConfirmation(Model model) {
        requireNonNull(model);
        return new ConfirmationRequest(DESCRIPTION, this);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setPawPals(new PawPals());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
