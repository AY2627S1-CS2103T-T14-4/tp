package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;
import seedu.address.model.pet.exceptions.DuplicatePetException;

/**
 * Edits the name, species or breed of a client's pet. The pet's notes are kept.
 * Renaming a pet also renames it in the client's appointments.
 */
public class EditPetCommand extends Command {

    public static final String COMMAND_WORD = "editp";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits a pet of a client.\n"
            + "Parameters: c/CLIENT_INDEX pn/PET_NAME [n/NAME] [s/SPECIES] [b/BREED]\n"
            + "At least one of n/, s/ or b/ must be provided.\n"
            + "Example: " + COMMAND_WORD + " c/1 pn/Milo b/Toy Poodle";
    public static final String MESSAGE_SUCCESS = "Edited pet: %1$s (%2$s, %3$s)";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";

    private static final Logger logger = LogsCenter.getLogger(EditPetCommand.class);

    private final Index clientIndex;
    private final PetName petName;
    private final Optional<PetName> newName;
    private final Optional<Species> newSpecies;
    private final Optional<Breed> newBreed;

    /**
     * Creates a command to edit the pet named {@code petName} of the client at {@code clientIndex}.
     * Fields given as empty optionals are left unchanged.
     *
     * @param clientIndex index of the client in the displayed client list
     * @param petName current name of the pet, matched ignoring case
     * @param newName the pet's new name, if it changes
     * @param newSpecies the pet's new species, if it changes
     * @param newBreed the pet's new breed, if it changes
     */
    public EditPetCommand(Index clientIndex, PetName petName, Optional<PetName> newName,
            Optional<Species> newSpecies, Optional<Breed> newBreed) {
        requireAllNonNull(clientIndex, petName, newName, newSpecies, newBreed);
        this.clientIndex = clientIndex;
        this.petName = petName;
        this.newName = newName;
        this.newSpecies = newSpecies;
        this.newBreed = newBreed;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Client targetClient = getDisplayedClient(model, clientIndex);
        Pet targetPet = getPet(targetClient, petName);
        Pet editedPet = new Pet(newName.orElse(targetPet.getName()), newSpecies.orElse(targetPet.getSpecies()),
                newBreed.orElse(targetPet.getBreed()), targetPet.getNotes());

        Client updatedClient;
        try {
            updatedClient = targetClient.withPetReplaced(petName, editedPet);
        } catch (DuplicatePetException e) {
            throw new CommandException(AddPetCommand.MESSAGE_DUPLICATE_PET);
        }

        model.setClient(targetClient, updatedClient);
        logger.fine("Edited pet " + targetPet.getName() + " of " + targetClient.getName() + " to " + editedPet);
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                editedPet.getName(), editedPet.getSpecies(), editedPet.getBreed()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof EditPetCommand otherCommand)) {
            return false;
        }
        return clientIndex.equals(otherCommand.clientIndex)
                && petName.equals(otherCommand.petName)
                && newName.equals(otherCommand.newName)
                && newSpecies.equals(otherCommand.newSpecies)
                && newBreed.equals(otherCommand.newBreed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientIndex.getZeroBased(), petName, newName, newSpecies, newBreed);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("clientIndex", clientIndex)
                .add("petName", petName)
                .add("newName", newName)
                .add("newSpecies", newSpecies)
                .add("newBreed", newBreed)
                .toString();
    }
}
