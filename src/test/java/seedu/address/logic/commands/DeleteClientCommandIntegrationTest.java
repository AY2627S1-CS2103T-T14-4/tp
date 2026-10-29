package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showClientAtIndex;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;
import static seedu.address.testutil.TypicalClients.getTypicalPawPals;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_CLIENT;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.LogicManager;
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
import seedu.address.storage.JsonPawPalsStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ClientBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code DeleteClientCommand}.
 */
public class DeleteClientCommandIntegrationTest {

    @TempDir
    public Path testFolder;

    private Model model = new ModelManager(getTypicalPawPals(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Client clientToDelete = model.getFilteredClientList().get(INDEX_FIRST_CLIENT.getZeroBased());
        DeleteClientCommand deleteClientCommand = new DeleteClientCommand(INDEX_FIRST_CLIENT);

        String expectedMessage = String.format(DeleteClientCommand.MESSAGE_DELETE_CLIENT_SUCCESS,
                Messages.format(clientToDelete));

        ModelManager expectedModel = new ModelManager(model.getPawPals(), new UserPrefs());
        expectedModel.deleteClient(clientToDelete);

        assertCommandSuccess(deleteClientCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_clientWithAppointment_removesOnlySelectedClient() throws Exception {
        Client original = model.getFilteredClientList().getFirst();
        Appointment appointment = new Appointment(new AppointmentDate("01-01-2028"),
                new AppointmentTime("09:05"));
        Client withAppointment = original.copyWithAppointment(appointment);
        model.setClient(original, withAppointment);
        int originalSize = model.getPawPals().getClientList().size();

        new DeleteClientCommand(INDEX_FIRST_CLIENT).execute(model);

        assertEquals(originalSize - 1, model.getPawPals().getClientList().size());
        assertFalse(model.getPawPals().getClientList().contains(withAppointment));
    }

    @Test
    public void execute_clientWithPetsAndAppointments_removesAssociatedData() throws Exception {
        Pet pet = new Pet(new PetName("Milo"), new Species("Cat"), new Breed("Siamese"));
        Appointment appointment = new Appointment(new AppointmentDate("01-01-2028"),
                new AppointmentTime("09:05"));
        Client clientToDelete = new ClientBuilder(ALICE).withPets(pet).withAppointments(appointment).build();
        PawPals pawPals = new PawPals();
        pawPals.addClient(clientToDelete);
        pawPals.addClient(BENSON);
        model = new ModelManager(pawPals, new UserPrefs());

        new DeleteClientCommand(INDEX_FIRST_CLIENT).execute(model);

        assertEquals(1, model.getPawPals().getClientList().size());
        assertFalse(model.getPawPals().getClientList().contains(clientToDelete));
        assertEquals(BENSON, model.getPawPals().getClientList().getFirst());
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredClientList().size() + 1);
        DeleteClientCommand deleteClientCommand = new DeleteClientCommand(outOfBoundIndex);

        assertCommandFailure(deleteClientCommand, model, Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showClientAtIndex(model, INDEX_FIRST_CLIENT);

        Client clientToDelete = model.getFilteredClientList().get(INDEX_FIRST_CLIENT.getZeroBased());
        DeleteClientCommand deleteClientCommand = new DeleteClientCommand(INDEX_FIRST_CLIENT);

        String expectedMessage = String.format(DeleteClientCommand.MESSAGE_DELETE_CLIENT_SUCCESS,
                Messages.format(clientToDelete));

        Model expectedModel = new ModelManager(model.getPawPals(), new UserPrefs());
        expectedModel.deleteClient(clientToDelete);
        showNoClient(expectedModel);

        assertCommandSuccess(deleteClientCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showClientAtIndex(model, INDEX_FIRST_CLIENT);

        Index outOfBoundIndex = INDEX_SECOND_CLIENT;
        // ensures that outOfBoundIndex is still in bounds of PawPals data list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getPawPals().getClientList().size());

        DeleteClientCommand deleteClientCommand = new DeleteClientCommand(outOfBoundIndex);

        assertCommandFailure(deleteClientCommand, model, Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_afterFind_deletesClientAtFilteredIndex() throws Exception {
        PawPals pawPals = new PawPals();
        pawPals.addClient(ALICE);
        pawPals.addClient(BENSON);
        model = new ModelManager(pawPals, new UserPrefs());
        LogicManager logic = new LogicManager(model, createStorage());

        logic.execute("find Benson");
        assertEquals(List.of(BENSON), model.getFilteredClientList());

        logic.execute("delc c/1");

        assertEquals(List.of(ALICE), model.getPawPals().getClientList());
        assertTrue(model.getFilteredClientList().isEmpty());
    }

    @Test
    public void execute_afterReload_deletedClientRemainsAbsent() throws Exception {
        PawPals pawPals = new PawPals();
        pawPals.addClient(ALICE);
        pawPals.addClient(BENSON);
        model = new ModelManager(pawPals, new UserPrefs());
        LogicManager logic = new LogicManager(model, createStorage());

        logic.execute("delc c/1");
        assertEquals(List.of(BENSON), model.getPawPals().getClientList());

        StorageManager restartedStorage = createStorage();
        Model restartedModel = new ModelManager(restartedStorage.readPawPals().orElseThrow(), new UserPrefs());
        LogicManager restartedLogic = new LogicManager(restartedModel, restartedStorage);
        restartedLogic.execute("list");

        assertEquals(List.of(BENSON), restartedModel.getPawPals().getClientList());
        assertFalse(restartedModel.getPawPals().getClientList().contains(ALICE));
    }

    @Test
    public void execute_clientsWithNotesAndLinkedAppointments_deletesOnlySelectedClientAfterReload()
            throws Exception {
        Client alice = withNotedPetAndLinkedAppointment(ALICE, "Milo", "09:00");
        Client benson = withNotedPetAndLinkedAppointment(BENSON, "Luna", "14:00");
        PawPals pawPals = new PawPals();
        pawPals.addClient(alice);
        pawPals.addClient(benson);
        model = new ModelManager(pawPals, new UserPrefs());
        LogicManager logic = new LogicManager(model, createStorage());

        logic.execute("delc c/1");

        StorageManager restartedStorage = createStorage();
        Model restartedModel = new ModelManager(restartedStorage.readPawPals().orElseThrow(), new UserPrefs());
        // The other client keeps its pet note, appointment end time and linked pet
        assertEquals(List.of(benson), restartedModel.getPawPals().getClientList());
    }

    /**
     * Returns a copy of {@code client} with a pet that has a temperament note,
     * and an appointment with an end time that is linked to that pet.
     */
    private static Client withNotedPetAndLinkedAppointment(Client client, String petName, String startTime) {
        Pet pet = new Pet(new PetName(petName), new Species("Dog"), new Breed("Poodle"))
                .withNote(NoteType.TEMP, new PetNote("Nervous around dryers"));
        Appointment appointment = new Appointment(new AppointmentDate("01-01-2028"), new AppointmentTime(startTime),
                Optional.of(new AppointmentTime("16:30")), "Full groom", Set.of(new PetName(petName)));
        return new ClientBuilder(client).withPets(pet).withAppointments(appointment).build();
    }

    private StorageManager createStorage() {
        JsonPawPalsStorage pawPalsStorage =
                new JsonPawPalsStorage(testFolder.resolve("pawPals.json"));
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(testFolder.resolve("userPrefs.json"));
        return new StorageManager(pawPalsStorage, userPrefsStorage);
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoClient(Model model) {
        model.updateFilteredClientList(p -> false);

        assertTrue(model.getFilteredClientList().isEmpty());
    }
}
