package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;

/**
 * Deletes a pet, and its notes, from a client identified by the index in the displayed client list.
 * The pet is also unlinked from the client's appointments; completed-session history is kept.
 */
public class DeletePetCommand extends Command {

    public static final String COMMAND_WORD = "delp";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Deletes a pet from a client.\n"
            + "Parameters: c/CLIENT_INDEX pn/PET_NAME\n"
            + "Example: " + COMMAND_WORD + " c/1 pn/Milo";
    public static final String MESSAGE_SUCCESS = "Deleted pet: %1$s (owner: %2$s)";

    private static final Logger logger = LogsCenter.getLogger(DeletePetCommand.class);

    private final Index clientIndex;
    private final PetName petName;

    /**
     * Creates a command to delete the pet named {@code petName} from the client at {@code clientIndex}.
     *
     * @param clientIndex index of the client in the displayed client list
     * @param petName name of the client's pet, matched ignoring case
     */
    public DeletePetCommand(Index clientIndex, PetName petName) {
        requireAllNonNull(clientIndex, petName);
        this.clientIndex = clientIndex;
        this.petName = petName;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Client targetClient = getDisplayedClient(model, clientIndex);
        Pet targetPet = getPet(targetClient, petName);

        model.setClient(targetClient, targetClient.withoutPet(petName));
        logger.fine("Deleted pet " + targetPet.getName() + " of " + targetClient.getName());
        return new CommandResult(String.format(MESSAGE_SUCCESS, targetPet.getName(), targetClient.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof DeletePetCommand otherCommand)) {
            return false;
        }
        return clientIndex.equals(otherCommand.clientIndex) && petName.equals(otherCommand.petName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientIndex.getZeroBased(), petName);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("clientIndex", clientIndex)
                .add("petName", petName)
                .toString();
    }
}
