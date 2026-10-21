package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.client.Client;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;

/**
 * Deletes one note of a pet belonging to a displayed client.
 */
public class DeleteNoteCommand extends Command {

    public static final String COMMAND_WORD = "delnote";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Deletes a note of a client's pet.\n"
            + "Parameters: c/CLIENT_INDEX pn/PET_NAME ty/TYPE\n"
            + "TYPE is pref, sens or temp.\n"
            + "Example: " + COMMAND_WORD + " c/1 pn/Milo ty/temp";
    public static final String MESSAGE_SUCCESS = "Deleted %1$s note for %2$s";
    public static final String MESSAGE_NO_SUCH_NOTE = "%1$s has no %2$s note to delete.";

    private static final Logger logger = LogsCenter.getLogger(DeleteNoteCommand.class);

    private final Index clientIndex;
    private final PetName petName;
    private final NoteType noteType;

    /**
     * Creates a command to delete the note of type {@code noteType} from the pet named {@code petName}.
     *
     * @param clientIndex index of the client in the displayed client list
     * @param petName name of the client's pet, matched ignoring case
     * @param noteType type of note to delete
     */
    public DeleteNoteCommand(Index clientIndex, PetName petName, NoteType noteType) {
        requireAllNonNull(clientIndex, petName, noteType);
        this.clientIndex = clientIndex;
        this.petName = petName;
        this.noteType = noteType;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Client> displayedClients = model.getFilteredClientList();
        if (clientIndex.getZeroBased() >= displayedClients.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        }

        Client targetClient = displayedClients.get(clientIndex.getZeroBased());
        Pet targetPet = targetClient.getPet(petName)
                .orElseThrow(() -> new CommandException(String.format(NoteCommand.MESSAGE_UNKNOWN_PET, petName)));
        String displayType = noteType.getDisplayName();
        if (!targetPet.hasNote(noteType)) {
            throw new CommandException(String.format(MESSAGE_NO_SUCH_NOTE, targetPet.getName(), displayType));
        }

        Client updatedClient = targetClient.withPetReplaced(petName, targetPet.withoutNote(noteType));
        model.setClient(targetClient, updatedClient);
        logger.fine("Deleted " + displayType + " note of " + targetPet.getName() + " for " + targetClient.getName());
        return new CommandResult(String.format(MESSAGE_SUCCESS, displayType, targetPet.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof DeleteNoteCommand otherCommand)) {
            return false;
        }
        return clientIndex.equals(otherCommand.clientIndex)
                && petName.equals(otherCommand.petName)
                && noteType == otherCommand.noteType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientIndex.getZeroBased(), petName, noteType);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("clientIndex", clientIndex)
                .add("petName", petName)
                .add("noteType", noteType)
                .toString();
    }
}
