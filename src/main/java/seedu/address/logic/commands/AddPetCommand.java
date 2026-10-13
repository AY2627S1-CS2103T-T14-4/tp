package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.exceptions.DuplicatePetException;

/**
 * Adds a pet to a client identified by the index in the displayed client list.
 */
public class AddPetCommand extends Command {

    public static final String COMMAND_WORD = "addp";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds a pet to the client at the specified displayed index.\n"
            + "Parameters: c/CLIENT_INDEX n/NAME s/SPECIES b/BREED\n"
            + "Example: " + COMMAND_WORD + " c/1 n/Milo s/Dog b/Poodle";

    public static final String MESSAGE_SUCCESS = "New pet added: %1$s";
    public static final String MESSAGE_DUPLICATE_PET = "This client already has a pet with the same name.";

    private final Index clientIndex;
    private final Pet petToAdd;

    /**
     * Creates an {@code AddPetCommand} to add {@code petToAdd} to the client at {@code clientIndex}.
     */
    public AddPetCommand(Index clientIndex, Pet petToAdd) {
        requireNonNull(clientIndex);
        requireNonNull(petToAdd);
        this.clientIndex = clientIndex;
        this.petToAdd = petToAdd;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Client> displayedClients = model.getFilteredClientList();

        if (clientIndex.getZeroBased() >= displayedClients.size()) {
            throw new CommandException(seedu.address.logic.Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        }

        Client targetClient = displayedClients.get(clientIndex.getZeroBased());
        Client updatedClient;
        try {
            updatedClient = targetClient.withPet(petToAdd);
        } catch (DuplicatePetException e) {
            throw new CommandException(MESSAGE_DUPLICATE_PET);
        }

        model.setClient(targetClient, updatedClient);
        return new CommandResult(String.format(MESSAGE_SUCCESS, petToAdd.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof AddPetCommand otherCommand)) {
            return false;
        }
        return clientIndex.equals(otherCommand.clientIndex) && petToAdd.equals(otherCommand.petToAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("clientIndex", clientIndex)
                .add("petToAdd", petToAdd)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientIndex.getZeroBased(), petToAdd);
    }
}
