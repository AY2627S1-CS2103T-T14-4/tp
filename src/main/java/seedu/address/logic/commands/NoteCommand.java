package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.client.Client;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.PetNote;

/**
 * Adds, replaces, or clears a note for a pet belonging to a displayed client.
 */
public class NoteCommand extends Command {

    public static final String COMMAND_WORD = "note";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + " c/CLIENT_INDEX pn/PET_NAME ty/TYPE [nt/NOTE]";
    public static final String MESSAGE_INVALID_FORMAT = "Invalid command format. Usage: " + MESSAGE_USAGE;
    public static final String MESSAGE_INVALID_CLIENT_INDEX = "The client index provided is invalid.";
    public static final String MESSAGE_UNKNOWN_PET = "This client has no pet named %1$s.";
    public static final String MESSAGE_MISSING_NOTE = "%1$s has no %2$s note to clear.";
    public static final String MESSAGE_UPDATED_NOTE = "Updated %1$s note for %2$s: %3$s";
    public static final String MESSAGE_CLEARED_NOTE = "Cleared %1$s note for %2$s";

    private final Index clientIndex;
    private final PetName petName;
    private final NoteType noteType;
    private final Optional<PetNote> note;

    /**
     * Creates a command to update or clear a pet note.
     *
     * @param clientIndex displayed client index
     * @param petName target pet name
     * @param noteType note category
     * @param note non-empty note, or empty when the note should be cleared
     */
    public NoteCommand(Index clientIndex, PetName petName, NoteType noteType, Optional<PetNote> note) {
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
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_UNKNOWN_PET, petName)));

        Pet updatedPet;
        String displayType = noteType.getDisplayName();
        if (note.isPresent()) {
            updatedPet = targetPet.withNote(noteType, note.get());
            Client updatedClient = targetClient.withPetReplaced(petName, updatedPet);
            model.setClient(targetClient, updatedClient);
            return new CommandResult(String.format(MESSAGE_UPDATED_NOTE,
                    displayType, targetPet.getName(), note.get()));
        }

        if (!targetPet.hasNote(noteType)) {
            throw new CommandException(String.format(MESSAGE_MISSING_NOTE, targetPet.getName(), displayType));
        }
        updatedPet = targetPet.withoutNote(noteType);
        Client updatedClient = targetClient.withPetReplaced(petName, updatedPet);
        model.setClient(targetClient, updatedClient);
        return new CommandResult(String.format(MESSAGE_CLEARED_NOTE, displayType, targetPet.getName()));
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
