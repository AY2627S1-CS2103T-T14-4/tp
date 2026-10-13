package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showClientAtIndex;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;
import static seedu.address.testutil.TypicalClients.getTypicalAddressBook;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_CLIENT;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ClientBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code DeleteClientCommand}.
 */
public class DeleteClientCommandIntegrationTest {

    @TempDir
    public Path testFolder;

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Client clientToDelete = model.getFilteredClientList().get(INDEX_FIRST_CLIENT.getZeroBased());
        DeleteClientCommand deleteClientCommand = new DeleteClientCommand(INDEX_FIRST_CLIENT);

        String expectedMessage = String.format(DeleteClientCommand.MESSAGE_DELETE_CLIENT_SUCCESS,
                Messages.format(clientToDelete));

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deleteClient(clientToDelete);

        assertCommandSuccess(deleteClientCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_clientWithAppointment_removesOnlySelectedClient() throws Exception {
        Client original = model.getFilteredClientList().get(0);
        Appointment appointment = new Appointment(new AppointmentDate("01-01-2028"),
                new AppointmentTime("09:05"));
        Client withAppointment = original.copyWithAppointment(appointment);
        model.setClient(original, withAppointment);
        int originalSize = model.getAddressBook().getClientList().size();

        new DeleteClientCommand(INDEX_FIRST_CLIENT).execute(model);

        assertEquals(originalSize - 1, model.getAddressBook().getClientList().size());
        assertFalse(model.getAddressBook().getClientList().contains(withAppointment));
    }

    @Test
    public void execute_clientWithPetsAndAppointments_removesAssociatedData() throws Exception {
        Pet pet = new Pet(new PetName("Milo"), new Species("Cat"), new Breed("Siamese"));
        Appointment appointment = new Appointment(new AppointmentDate("01-01-2028"),
                new AppointmentTime("09:05"));
        Client clientToDelete = new ClientBuilder(ALICE).withPets(pet).withAppointments(appointment).build();
        AddressBook addressBook = new AddressBook();
        addressBook.addClient(clientToDelete);
        addressBook.addClient(BENSON);
        model = new ModelManager(addressBook, new UserPrefs());

        new DeleteClientCommand(INDEX_FIRST_CLIENT).execute(model);

        assertEquals(1, model.getAddressBook().getClientList().size());
        assertFalse(model.getAddressBook().getClientList().contains(clientToDelete));
        assertEquals(BENSON, model.getAddressBook().getClientList().get(0));
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

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deleteClient(clientToDelete);
        showNoClient(expectedModel);

        assertCommandSuccess(deleteClientCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showClientAtIndex(model, INDEX_FIRST_CLIENT);

        Index outOfBoundIndex = INDEX_SECOND_CLIENT;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getClientList().size());

        DeleteClientCommand deleteClientCommand = new DeleteClientCommand(outOfBoundIndex);

        assertCommandFailure(deleteClientCommand, model, Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_afterFind_deletesClientAtFilteredIndex() throws Exception {
        AddressBook addressBook = new AddressBook();
        addressBook.addClient(ALICE);
        addressBook.addClient(BENSON);
        model = new ModelManager(addressBook, new UserPrefs());
        LogicManager logic = new LogicManager(model, createStorage());

        logic.execute("find Benson");
        assertEquals(List.of(BENSON), model.getFilteredClientList());

        logic.execute("delc 1");

        assertEquals(List.of(ALICE), model.getAddressBook().getClientList());
        assertTrue(model.getFilteredClientList().isEmpty());
    }

    private StorageManager createStorage() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(testFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(testFolder.resolve("userPrefs.json"));
        return new StorageManager(addressBookStorage, userPrefsStorage);
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoClient(Model model) {
        model.updateFilteredClientList(p -> false);

        assertTrue(model.getFilteredClientList().isEmpty());
    }
}
