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

public class DeletePetCommandTest {

    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"))
            .withNote(NoteType.TEMP, new PetNote("Nervous around dryers"));
    private static final Pet LUNA = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));

    @Test
    public void execute_existingPet_deletesOnlyThatPet() {
        Model model = modelWith(new ClientBuilder(ALICE).withPets(MILO, LUNA).build());
        Model expectedModel = modelWith(new ClientBuilder(ALICE).withPets(LUNA).build());

        DeletePetCommand command = new DeletePetCommand(INDEX_FIRST_CLIENT, new PetName("milo"));

        assertCommandSuccess(command, model,
                String.format(DeletePetCommand.MESSAGE_SUCCESS, "Milo", ALICE.getName()), expectedModel);
    }

    @Test
    public void execute_petLinkedToAppointment_unlinksPetAndKeepsAppointment() {
        Appointment linked = new Appointment(new AppointmentDate("12-12-2030"), new AppointmentTime("14:00"),
                Optional.empty(), "", Set.of(new PetName("Milo"), new PetName("Luna")));
        Model model = modelWith(new ClientBuilder(ALICE).withPets(MILO, LUNA).withAppointments(linked).build());

        assertDoesNotThrow(() -> new DeletePetCommand(INDEX_FIRST_CLIENT, new PetName("Milo")).execute(model));

        Client updated = model.getFilteredClientList().get(0);
        assertEquals(List.of(LUNA), updated.getPets());
        assertEquals(Set.of(new PetName("Luna")), updated.getAppointments().get(0).getPetNames());
    }

    @Test
    public void execute_afterFilter_usesDisplayedIndex() {
        PawPals pawPals = new PawPals();
        pawPals.addClient(new ClientBuilder(ALICE).withPets(MILO).build());
        pawPals.addClient(new ClientBuilder(BENSON).withPets(MILO).build());
        Model model = new ModelManager(pawPals, new UserPrefs());
        model.updateFilteredClientList(client -> client.isSameClient(BENSON));

        assertDoesNotThrow(() -> new DeletePetCommand(INDEX_FIRST_CLIENT, new PetName("Milo")).execute(model));

        assertTrue(model.getPawPals().getClientList().get(1).getPets().isEmpty());
        assertEquals(List.of(MILO), model.getPawPals().getClientList().get(0).getPets());
    }

    @Test
    public void execute_unknownPet_throwsCommandException() {
        Model model = modelWith(new ClientBuilder(ALICE).withPets(MILO).build());
        DeletePetCommand command = new DeletePetCommand(INDEX_FIRST_CLIENT, new PetName("Rex"));

        assertCommandFailure(command, model, String.format(Messages.MESSAGE_UNKNOWN_PET, "Rex"));
    }

    @Test
    public void execute_invalidClientIndex_throwsCommandException() {
        Model model = modelWith(new ClientBuilder(ALICE).withPets(MILO).build());
        DeletePetCommand command = new DeletePetCommand(Index.fromOneBased(2), new PetName("Milo"));

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeletePetCommand(null, new PetName("Milo")));
        assertThrows(NullPointerException.class, () -> new DeletePetCommand(INDEX_FIRST_CLIENT, null));
    }

    @Test
    public void equals() {
        DeletePetCommand command = new DeletePetCommand(INDEX_FIRST_CLIENT, new PetName("Milo"));

        assertTrue(command.equals(command));
        assertTrue(command.equals(new DeletePetCommand(INDEX_FIRST_CLIENT, new PetName("Milo"))));
        assertEquals(command.hashCode(), new DeletePetCommand(INDEX_FIRST_CLIENT, new PetName("Milo")).hashCode());
        assertFalse(command.equals(new DeletePetCommand(Index.fromOneBased(2), new PetName("Milo"))));
        assertFalse(command.equals(new DeletePetCommand(INDEX_FIRST_CLIENT, new PetName("Luna"))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new Object()));
    }

    @Test
    public void toStringMethod() {
        DeletePetCommand command = new DeletePetCommand(INDEX_FIRST_CLIENT, new PetName("Milo"));
        String expected = DeletePetCommand.class.getCanonicalName()
                + "{clientIndex=" + INDEX_FIRST_CLIENT + ", petName=Milo}";

        assertEquals(expected, command.toString());
    }

    private static Model modelWith(Client client) {
        PawPals pawPals = new PawPals();
        pawPals.addClient(client);
        return new ModelManager(pawPals, new UserPrefs());
    }
}
