package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/** A command that requires user confirmation before Logic executes it. */
public interface ConfirmableCommand {
    /**
     * Validates and prepares a confirmation request without changing the model.
     *
     * @throws CommandException if the request cannot be prepared.
     */
    ConfirmationRequest prepareConfirmation(Model model) throws CommandException;
}
