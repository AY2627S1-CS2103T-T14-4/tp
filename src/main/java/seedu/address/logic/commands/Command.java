package seedu.address.logic.commands;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /**
     * Executes the command and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

    /**
     * Returns the client at {@code index} in the currently displayed client list.
     *
     * @throws CommandException if {@code index} is outside the displayed list.
     */
    protected static Client getDisplayedClient(Model model, Index index) throws CommandException {
        List<Client> displayedClients = model.getFilteredClientList();
        if (index.getZeroBased() >= displayedClients.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        }
        return displayedClients.get(index.getZeroBased());
    }

    /**
     * Returns {@code client}'s pet named {@code petName}, ignoring case.
     *
     * @throws CommandException if the client has no such pet.
     */
    protected static Pet getPet(Client client, PetName petName) throws CommandException {
        return client.getPet(petName)
                .orElseThrow(() -> new CommandException(String.format(Messages.MESSAGE_UNKNOWN_PET, petName)));
    }
}
