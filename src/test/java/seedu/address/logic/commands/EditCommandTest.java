package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showClientAtIndex;
import static seedu.address.testutil.TypicalClients.getTypicalPawPals;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_CLIENT;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditClientDescriptor;
import seedu.address.model.ClientDisplayMode;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.EditClientDescriptorBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalPawPals(), new UserPrefs());

    @Test
    public void execute_editContactDetails_preservesAppointments() {
        Appointment appointment = new Appointment(new AppointmentDate("08-10-2026"),
                new AppointmentTime("09:05"), "Call on arrival");
        Client original = model.getFilteredClientList().get(0);
        Client withAppointment = original.copyWithAppointment(appointment);
        model.setClient(original, withAppointment);
        showClientAtIndex(model, INDEX_FIRST_CLIENT);

        Client editedClient = new ClientBuilder(withAppointment).withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).withEmail("updated@example.com").withAddress("Updated address")
                .withTags(VALID_TAG_HUSBAND).build();
        EditClientDescriptor descriptor = new EditClientDescriptorBuilder(editedClient).build();
        Model expectedModel = new ModelManager(model.getPawPals(), new UserPrefs());
        expectedModel.setClient(withAppointment, editedClient);

        assertCommandSuccess(new EditCommand(INDEX_FIRST_CLIENT, descriptor), model,
                String.format(EditCommand.MESSAGE_EDIT_CLIENT_SUCCESS, Messages.format(editedClient)), expectedModel);
        assertEquals(withAppointment.getAppointments(),
                model.getPawPals().getClientList().get(0).getAppointments());
    }

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Client editedClient = new ClientBuilder().build();
        EditClientDescriptor descriptor = new EditClientDescriptorBuilder(editedClient).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_CLIENT, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_CLIENT_SUCCESS, Messages.format(editedClient));

        Model expectedModel = new ModelManager(new PawPals(model.getPawPals()), new UserPrefs());
        expectedModel.setClient(model.getFilteredClientList().get(0), editedClient);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastClient = Index.fromOneBased(model.getFilteredClientList().size());
        Client lastClient = model.getFilteredClientList().get(indexLastClient.getZeroBased());

        ClientBuilder clientInList = new ClientBuilder(lastClient);
        Client editedClient = clientInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withTags(VALID_TAG_HUSBAND).build();

        EditClientDescriptor descriptor = new EditClientDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).withTags(VALID_TAG_HUSBAND).build();
        EditCommand editCommand = new EditCommand(indexLastClient, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_CLIENT_SUCCESS, Messages.format(editedClient));

        Model expectedModel = new ModelManager(new PawPals(model.getPawPals()), new UserPrefs());
        expectedModel.setClient(lastClient, editedClient);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_CLIENT, new EditClientDescriptor());
        Client editedClient = model.getFilteredClientList().get(INDEX_FIRST_CLIENT.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_CLIENT_SUCCESS, Messages.format(editedClient));

        Model expectedModel = new ModelManager(new PawPals(model.getPawPals()), new UserPrefs());

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        showClientAtIndex(model, INDEX_FIRST_CLIENT);

        Client clientInFilteredList = model.getFilteredClientList().get(INDEX_FIRST_CLIENT.getZeroBased());
        Client editedClient = new ClientBuilder(clientInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_CLIENT,
                new EditClientDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_CLIENT_SUCCESS, Messages.format(editedClient));

        Model expectedModel = new ModelManager(new PawPals(model.getPawPals()), new UserPrefs());
        expectedModel.setClient(model.getFilteredClientList().get(0), editedClient);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_successFromScheduleModes_resetsView() throws Exception {
        for (ClientDisplayMode mode : List.of(ClientDisplayMode.SCHEDULE_CLIENT,
                ClientDisplayMode.scheduleDate(new AppointmentDate("29-10-2026")))) {
            Model currentModel = new ModelManager(getTypicalPawPals(), new UserPrefs());
            currentModel.setClientDisplayMode(mode);
            showClientAtIndex(currentModel, INDEX_FIRST_CLIENT);
            EditCommand command = new EditCommand(INDEX_FIRST_CLIENT,
                    new EditClientDescriptorBuilder().withAddress("Updated address").build());

            command.execute(currentModel);

            assertEquals(ClientDisplayMode.NORMAL, currentModel.clientDisplayModeProperty().getValue());
            assertEquals(currentModel.getPawPals().getClientList(), currentModel.getFilteredClientList());
        }
    }

    @Test
    public void execute_failureFromScheduleModes_preservesView() {
        for (ClientDisplayMode mode : List.of(ClientDisplayMode.SCHEDULE_CLIENT,
                ClientDisplayMode.scheduleDate(new AppointmentDate("29-10-2026")))) {
            Model currentModel = new ModelManager(getTypicalPawPals(), new UserPrefs());
            currentModel.setClientDisplayMode(mode);
            showClientAtIndex(currentModel, INDEX_FIRST_CLIENT);
            Client duplicate = currentModel.getPawPals().getClientList().get(INDEX_SECOND_CLIENT.getZeroBased());
            EditCommand command = new EditCommand(INDEX_FIRST_CLIENT,
                    new EditClientDescriptorBuilder(duplicate).build());

            assertCommandFailure(command, currentModel, EditCommand.MESSAGE_DUPLICATE_CLIENT);

            assertEquals(mode, currentModel.clientDisplayModeProperty().getValue());
            assertEquals(1, currentModel.getFilteredClientList().size());
        }
    }

    @Test
    public void execute_duplicateClientUnfilteredList_failure() {
        Client firstClient = model.getFilteredClientList().get(INDEX_FIRST_CLIENT.getZeroBased());
        EditClientDescriptor descriptor = new EditClientDescriptorBuilder(firstClient).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_CLIENT, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_CLIENT);
    }

    @Test
    public void execute_duplicateClientFilteredList_failure() {
        showClientAtIndex(model, INDEX_FIRST_CLIENT);

        // edit client in filtered list into a duplicate in PawPals data
        Client clientInList = model.getPawPals().getClientList().get(INDEX_SECOND_CLIENT.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_CLIENT,
                new EditClientDescriptorBuilder(clientInList).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_CLIENT);
    }

    @Test
    public void execute_invalidClientIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredClientList().size() + 1);
        EditClientDescriptor descriptor = new EditClientDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of PawPals data
     */
    @Test
    public void execute_invalidClientIndexFilteredList_failure() {
        showClientAtIndex(model, INDEX_FIRST_CLIENT);
        Index outOfBoundIndex = INDEX_SECOND_CLIENT;
        // ensures that outOfBoundIndex is still in bounds of PawPals data list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getPawPals().getClientList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditClientDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_CLIENT, DESC_AMY);

        // same values -> returns true
        EditClientDescriptor copyDescriptor = new EditClientDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_CLIENT, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_CLIENT, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_CLIENT, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditClientDescriptor editClientDescriptor = new EditClientDescriptor();
        EditCommand editCommand = new EditCommand(index, editClientDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editClientDescriptor="
                + editClientDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }

}
