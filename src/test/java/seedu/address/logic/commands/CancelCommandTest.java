package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showClientAtIndex;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_CLIENT;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
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

public class CancelCommandTest {
    private static final AppointmentDate DATE = new AppointmentDate("29-02-2024");
    private static final AppointmentTime TIME = new AppointmentTime("14:00");
    private static final Appointment TARGET = new Appointment(DATE, TIME, "Full groom", Set.of(new PetName("Milo")))
            .withEndTime(new AppointmentTime("15:30"));
    private static final Appointment SAME_DATE = new Appointment(DATE, new AppointmentTime("16:00"));
    private static final Appointment SAME_TIME = new Appointment(new AppointmentDate("01-03-2024"), TIME);

    private Model model;
    private Client selectedClient;
    private Client otherClient;

    @BeforeEach
    public void setUp() {
        Pet pet = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"))
                .withNote(NoteType.PREF, new PetNote("Short ears"))
                .withNote(NoteType.SENS, new PetNote("Sensitive paws"))
                .withNote(NoteType.TEMP, new PetNote("Nervous around dryers"));
        selectedClient = new ClientBuilder(BENSON).withTags("regular").withPets(pet)
                .withAppointments(TARGET, SAME_DATE, SAME_TIME).build();
        otherClient = new ClientBuilder(ALICE).withPets(pet).withAppointments(TARGET).build();
        PawPals pawPals = new PawPals();
        pawPals.addClient(otherClient);
        pawPals.addClient(selectedClient);
        model = new ModelManager(pawPals, new UserPrefs());
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CancelCommand(null, DATE, TIME));
        assertThrows(NullPointerException.class, () -> new CancelCommand(INDEX_FIRST_CLIENT, null, TIME));
        assertThrows(NullPointerException.class, () -> new CancelCommand(INDEX_FIRST_CLIENT, DATE, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CancelCommand(INDEX_FIRST_CLIENT, DATE, TIME).execute(null));
    }

    @Test
    public void execute_pastAppointment_removesOnlySelectedSlotAndPreservesOtherData() {
        Client expectedClient = new ClientBuilder(selectedClient).withAppointments(SAME_DATE, SAME_TIME).build();
        Model expected = new ModelManager(model.getPawPals(), new UserPrefs());
        expected.setClient(selectedClient, expectedClient);

        assertCommandSuccess(new CancelCommand(INDEX_SECOND_CLIENT, DATE, TIME), model,
                "Cancelled appointment: " + selectedClient.getName() + " d/29-02-2024 t/14:00", expected);
        assertEquals(List.of(TARGET, SAME_DATE, SAME_TIME), selectedClient.getAppointments());
        assertEquals(otherClient, model.getPawPals().getClientList().getFirst());
    }

    @Test
    public void execute_futureAppointment_succeeds() throws Exception {
        Appointment future = new Appointment(new AppointmentDate("31-12-9999"), new AppointmentTime("23:59"));
        Client withFuture = selectedClient.copyWithAppointment(future);
        model.setClient(selectedClient, withFuture);

        new CancelCommand(INDEX_SECOND_CLIENT, future.getDate(), future.getTime()).execute(model);

        assertEquals(List.of(otherClient, selectedClient), model.getPawPals().getClientList());
    }

    @Test
    public void execute_filteredList_resolvesDisplayedIndexAndPreservesFilter() throws Exception {
        showClientAtIndex(model, INDEX_SECOND_CLIENT);
        Client expected = new ClientBuilder(selectedClient).withAppointments(SAME_DATE, SAME_TIME).build();

        new CancelCommand(INDEX_FIRST_CLIENT, DATE, TIME).execute(model);

        assertEquals(List.of(expected), model.getFilteredClientList());
        assertEquals(List.of(otherClient, expected), model.getPawPals().getClientList());
    }

    @Test
    public void execute_lastAppointment_keepsClientAndPets() throws Exception {
        Client expected = new ClientBuilder(otherClient).withAppointments().build();

        new CancelCommand(INDEX_FIRST_CLIENT, DATE, TIME).execute(model);

        assertEquals(List.of(expected, selectedClient), model.getPawPals().getClientList());
    }

    @Test
    public void execute_invalidDisplayedIndex_doesNotChangeData() {
        assertCommandFailure(new CancelCommand(Index.fromOneBased(3), DATE, TIME), model,
                Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        showClientAtIndex(model, INDEX_SECOND_CLIENT);
        assertCommandFailure(new CancelCommand(INDEX_SECOND_CLIENT, DATE, TIME), model,
                Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyDisplayedList_doesNotChangeData() {
        assertCommandFailure(new CancelCommand(INDEX_FIRST_CLIENT, DATE, TIME), new ModelManager(),
                Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        model.updateFilteredClientList(client -> false);
        assertCommandFailure(new CancelCommand(INDEX_FIRST_CLIENT, DATE, TIME), model,
                Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_missingAppointment_doesNotChangeData() {
        // This slot exists for Benson but not for the selected client Alice.
        assertCommandFailure(new CancelCommand(INDEX_FIRST_CLIENT, SAME_DATE.getDate(), SAME_DATE.getTime()), model,
                "Alice Pauline has no appointment at 29-02-2024 16:00.");
        // An end time does not identify the appointment.
        assertCommandFailure(new CancelCommand(INDEX_SECOND_CLIENT, DATE, new AppointmentTime("15:30")), model,
                "Benson Meier has no appointment at 29-02-2024 15:30.");
    }

    @Test
    public void execute_repeatedCancellation_doesNotChangeData() throws Exception {
        CancelCommand command = new CancelCommand(INDEX_SECOND_CLIENT, DATE, TIME);
        command.execute(model);

        assertCommandFailure(command, model, "Benson Meier has no appointment at 29-02-2024 14:00.");
    }

    @Test
    public void equalsAndHashCode_compareIndexDateAndTime() {
        CancelCommand command = new CancelCommand(INDEX_FIRST_CLIENT, DATE, TIME);
        CancelCommand equalCommand = new CancelCommand(Index.fromOneBased(1),
                new AppointmentDate("29-02-2024"), new AppointmentTime("14:00"));
        assertEquals(command, command);
        assertEquals(command, equalCommand);
        assertEquals(command.hashCode(), equalCommand.hashCode());
        assertNotEquals(command, new CancelCommand(INDEX_SECOND_CLIENT, DATE, TIME));
        assertNotEquals(command, new CancelCommand(INDEX_FIRST_CLIENT, new AppointmentDate("01-03-2024"), TIME));
        assertNotEquals(command, new CancelCommand(INDEX_FIRST_CLIENT, DATE, new AppointmentTime("14:01")));
        assertNotEquals(command, null);
        assertNotEquals(command, "cancel");
    }

    @Test
    public void toStringMethod() {
        CancelCommand command = new CancelCommand(INDEX_FIRST_CLIENT, DATE, TIME);
        assertEquals(CancelCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_CLIENT
                + ", date=" + DATE + ", time=" + TIME + "}", command.toString());
    }
}
