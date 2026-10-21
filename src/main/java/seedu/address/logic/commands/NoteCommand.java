package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.client.Client;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.PetNote;

/**
 * Adds or replaces a note for a pet belonging to a displayed client.
 */
public class NoteCommand extends Command {

    public static final String COMMAND_WORD = "note";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + " c/CLIENT_INDEX pn/PET_NAME ty/TYPE nt/NOTE";
    public static final String MESSAGE_INVALID_FORMAT = "Invalid command format. Usage: " + MESSAGE_USAGE;
    public static final String MESSAGE_INVALID_CLIENT_INDEX = "The client index provided is invalid.";
    public static final String MESSAGE_UPDATED_NOTE = "Updated %1$s note for %2$s: %3$s";

    private final Index clientIndex;
    private final PetName petName;
    private final NoteType noteType;
    private final PetNote note;

    /**
     * Creates a command to update a pet note.
     *
     * @param clientIndex displayed client index
     * @param petName target pet name
     * @param noteType note category
     * @param note non-empty note
     */
    public NoteCommand(Index clientIndex, PetName petName, NoteType noteType, PetNote note) {
        CollectionUtil.requireAllNonNull(clientIndex, petName, noteType, note);
        this.clientIndex = clientIndex;
        this.petName = petName;
        this.noteType = noteType;
        this.note = note;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Client> displayedClients = model.getFilteredClientList();
        if (clientIndex.getZeroBased() >= displayedClients.size()) {
            throw new CommandException(MESSAGE_INVALID_CLIENT_INDEX);
        }

        Client targetClient = displayedClients.get(clientIndex.getZeroBased());
        Pet targetPet = targetClient.getPet(petName)
                .orElseThrow(() -> new CommandException(String.format(Messages.MESSAGE_UNKNOWN_PET, petName)));

        Pet updatedPet = targetPet.withNote(noteType, note);
        Client updatedClient = targetClient.withPetReplaced(petName, updatedPet);
        model.setClient(targetClient, updatedClient);
        return new CommandResult(String.format(MESSAGE_UPDATED_NOTE,
                noteType.getDisplayName(), targetPet.getName(), note));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof NoteCommand otherCommand)) {
            return false;
        }
        return clientIndex.equals(otherCommand.clientIndex)
                && petName.equals(otherCommand.petName)
                && noteType.equals(otherCommand.noteType)
                && note.equals(otherCommand.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientIndex, petName, noteType, note);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("clientIndex", clientIndex)
                .add("petName", petName)
                .add("noteType", noteType)
                .add("note", note)
                .toString();
    }
}
