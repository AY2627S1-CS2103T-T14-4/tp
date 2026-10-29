package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.client.Client;

/**
 * Deletes a client identified using its displayed index from the PawPals data.
 */
public class DeleteClientCommand extends Command implements ConfirmableCommand {

    public static final String COMMAND_WORD = "delc";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the client identified by the index number used in the displayed client list.\n"
            + "Parameters: c/CLIENT_INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " c/1";

    public static final String MESSAGE_DELETE_CLIENT_SUCCESS = "Deleted client: %1$s";
    public static final String MESSAGE_CONFIRMATION = "Deleting client: %1$s";
    public static final String MESSAGE_CLIENT_CHANGED = "Client changed or no longer exists. Run delc again.";

    private final Index targetIndex;

    public DeleteClientCommand(Index targetIndex) {
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        return deleteClient(model, resolveClient(model));
    }

    @Override
    public ConfirmationRequest prepareConfirmation(Model model) throws CommandException {
        Client client = resolveClient(model);
        return new ConfirmationRequest(String.format(MESSAGE_CONFIRMATION, Messages.format(client)),
                new ConfirmedDeleteClientCommand(client));
    }

    /** Resolves and validates the index against the currently displayed list. */
    private Client resolveClient(Model model) throws CommandException {
        requireNonNull(model);
        List<Client> lastShownList = model.getFilteredClientList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        }

        return lastShownList.get(targetIndex.getZeroBased());
    }

    /** Deletes the resolved client and formats the success feedback. */
    private static CommandResult deleteClient(Model model, Client clientToDelete) {
        model.deleteClient(clientToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_CLIENT_SUCCESS, Messages.format(clientToDelete)));
    }

    /** Deletes the captured client without resolving its displayed index again. */
    private static final class ConfirmedDeleteClientCommand extends Command {
        private final Client client;

        private ConfirmedDeleteClientCommand(Client client) {
            this.client = client;
        }

        @Override
        public CommandResult execute(Model model) throws CommandException {
            requireNonNull(model);
            if (!model.getPawPals().getClientList().contains(client)) {
                throw new CommandException(MESSAGE_CLIENT_CHANGED);
            }
            return deleteClient(model, client);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteClientCommand otherDeleteClientCommand)) {
            return false;
        }

        return targetIndex.equals(otherDeleteClientCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
