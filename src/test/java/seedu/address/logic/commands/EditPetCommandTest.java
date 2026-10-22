package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.PetNote;
import seedu.address.model.pet.Species;
import seedu.address.testutil.ClientBuilder;

public class EditPetCommandTest {

    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"))
            .withNote(NoteType.TEMP, new PetNote("Nervous around dryers"));
    private static final Pet LUNA = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));

    @Test
    public void execute_editBreed_keepsNameAndNotes() {
        Model model = modelWith(new ClientBuilder(ALICE).withPets(MILO, LUNA).build());
        Pet edited = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Toy Poodle"), MILO.getNotes());
        Model expectedModel = modelWith(new ClientBuilder(ALICE).withPets(edited, LUNA).build());

        EditPetCommand command = new EditPetCommand(INDEX_FIRST_CLIENT, new PetName("milo"),
                Optional.empty(), Optional.empty(), Optional.of(new Breed("Toy Poodle")));

        assertCommandSuccess(command, model, "Edited pet: Milo (Dog, Toy Poodle)", expectedModel);
    }

    @Test
    public void execute_renamePet_keepsNotesAndRenamesAppointmentLinks() {
        Appointment linked = new Appointment(new AppointmentDate("12-12-2030"), new AppointmentTime("14:00"),
                Optional.empty(), "", Set.of(new PetName("Milo")));
        Model model = modelWith(new ClientBuilder(ALICE).withPets(MILO).withAppointments(linked).build());
        EditPetCommand command = new EditPetCommand(INDEX_FIRST_CLIENT, new PetName("Milo"),
                Optional.of(new PetName("Max")), Optional.of(new Species("Cat")), Optional.empty());

        assertEquals(new CommandResult("Edited pet: Max (Cat, Poodle)"), assertDoesNotThrow(() ->
                command.execute(model)));

        Client updated = model.getFilteredClientList().get(0);
        Pet max = updated.getPet(new PetName("Max")).orElseThrow();
        assertEquals(MILO.getNotes(), max.getNotes());
        assertTrue(updated.getPet(new PetName("Milo")).isEmpty());
        assertEquals(Set.of(new PetName("Max")), updated.getAppointments().get(0).getPetNames());
    }

    @Test
    public void execute_changeCapitalisationOfName_success() {
        Model model = modelWith(new ClientBuilder(ALICE).withPets(MILO).build());
        EditPetCommand command = new EditPetCommand(INDEX_FIRST_CLIENT, new PetName("Milo"),
                Optional.of(new PetName("MILO")), Optional.empty(), Optional.empty());

        assertDoesNotThrow(() -> command.execute(model));
        assertEquals(new PetName("MILO"), model.getFilteredClientList().get(0).getPets().get(0).getName());
    }

    @Test
    public void execute_renameToOtherPetsName_throwsCommandException() {
        Model model = modelWith(new ClientBuilder(ALICE).withPets(MILO, LUNA).build());
        EditPetCommand command = new EditPetCommand(INDEX_FIRST_CLIENT, new PetName("Milo"),
                Optional.of(new PetName("luna")), Optional.empty(), Optional.empty());

        assertCommandFailure(command, model, AddPetCommand.MESSAGE_DUPLICATE_PET);
    }

    @Test
    public void execute_afterFilter_usesDisplayedIndex() {
        PawPals pawPals = new PawPals();
        pawPals.addClient(new ClientBuilder(ALICE).withPets(MILO).build());
        pawPals.addClient(new ClientBuilder(BENSON).withPets(MILO).build());
        Model model = new ModelManager(pawPals, new UserPrefs());
        model.updateFilteredClientList(client -> client.isSameClient(BENSON));
        EditPetCommand command = new EditPetCommand(INDEX_FIRST_CLIENT, new PetName("Milo"),
                Optional.empty(), Optional.empty(), Optional.of(new Breed("Toy Poodle")));

        assertDoesNotThrow(() -> command.execute(model));

        assertEquals(new Breed("Toy Poodle"), model.getPawPals().getClientList().get(1).getPets().get(0).getBreed());
        assertEquals(List.of(MILO), model.getPawPals().getClientList().get(0).getPets());
    }

    @Test
    public void execute_unknownPet_throwsCommandException() {
        Model model = modelWith(new ClientBuilder(ALICE).withPets(MILO).build());
        EditPetCommand command = new EditPetCommand(INDEX_FIRST_CLIENT, new PetName("Rex"),
                Optional.empty(), Optional.empty(), Optional.of(new Breed("Beagle")));

        assertCommandFailure(command, model, String.format(Messages.MESSAGE_UNKNOWN_PET, "Rex"));
    }

    @Test
    public void execute_invalidClientIndex_throwsCommandException() {
        Model model = modelWith(new ClientBuilder(ALICE).withPets(MILO).build());
        EditPetCommand command = new EditPetCommand(Index.fromOneBased(2), new PetName("Milo"),
                Optional.empty(), Optional.empty(), Optional.of(new Breed("Beagle")));

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        PetName milo = new PetName("Milo");
        assertThrows(NullPointerException.class, () ->
                new EditPetCommand(null, milo, Optional.empty(), Optional.empty(), Optional.empty()));
        assertThrows(NullPointerException.class, () ->
                new EditPetCommand(INDEX_FIRST_CLIENT, null, Optional.empty(), Optional.empty(), Optional.empty()));
        assertThrows(NullPointerException.class, () ->
                new EditPetCommand(INDEX_FIRST_CLIENT, milo, null, Optional.empty(), Optional.empty()));
        assertThrows(NullPointerException.class, () ->
                new EditPetCommand(INDEX_FIRST_CLIENT, milo, Optional.empty(), null, Optional.empty()));
        assertThrows(NullPointerException.class, () ->
                new EditPetCommand(INDEX_FIRST_CLIENT, milo, Optional.empty(), Optional.empty(), null));
    }

    @Test
    public void equals() {
        EditPetCommand command = breedCommand(INDEX_FIRST_CLIENT, "Milo", "Beagle");

        assertTrue(command.equals(command));
        assertTrue(command.equals(breedCommand(INDEX_FIRST_CLIENT, "Milo", "Beagle")));
        assertEquals(command.hashCode(), breedCommand(INDEX_FIRST_CLIENT, "Milo", "Beagle").hashCode());
        assertFalse(command.equals(breedCommand(Index.fromOneBased(2), "Milo", "Beagle")));
        assertFalse(command.equals(breedCommand(INDEX_FIRST_CLIENT, "Luna", "Beagle")));
        assertFalse(command.equals(breedCommand(INDEX_FIRST_CLIENT, "Milo", "Poodle")));
        assertFalse(command.equals(new EditPetCommand(INDEX_FIRST_CLIENT, new PetName("Milo"),
                Optional.of(new PetName("Max")), Optional.empty(), Optional.of(new Breed("Beagle")))));
        assertFalse(command.equals(new EditPetCommand(INDEX_FIRST_CLIENT, new PetName("Milo"),
                Optional.empty(), Optional.of(new Species("Dog")), Optional.of(new Breed("Beagle")))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new Object()));
    }

    @Test
    public void toStringMethod() {
        EditPetCommand command = breedCommand(INDEX_FIRST_CLIENT, "Milo", "Beagle");
        String expected = EditPetCommand.class.getCanonicalName()
                + "{clientIndex=" + INDEX_FIRST_CLIENT + ", petName=Milo, newName=Optional.empty, "
                + "newSpecies=Optional.empty, newBreed=Optional[Beagle]}";

        assertEquals(expected, command.toString());
    }

    private static EditPetCommand breedCommand(Index index, String petName, String breed) {
        return new EditPetCommand(index, new PetName(petName), Optional.empty(), Optional.empty(),
                Optional.of(new Breed(breed)));
    }

    private static Model modelWith(Client client) {
        PawPals pawPals = new PawPals();
        pawPals.addClient(client);
        return new ModelManager(pawPals, new UserPrefs());
    }
}
