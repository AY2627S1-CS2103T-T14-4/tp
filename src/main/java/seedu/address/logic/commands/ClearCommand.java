package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.PawPals;

/**
 * Clears the PawPals data.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "PawPals has been cleared!";
    public static final String MESSAGE_CONFIRMATION = "Clearing all client data:\nConfirm (Y/n)?";
    public static final String MESSAGE_CANCELLED = "Deletion cancelled.";
    public static final String MESSAGE_INVALID_CONFIRMATION = "Invalid input.\n" + MESSAGE_CONFIRMATION;


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setPawPals(new PawPals());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
