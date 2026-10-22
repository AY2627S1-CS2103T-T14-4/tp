package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_CLIENTS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentConflictChecker.Conflict;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.model.client.ClientContainsKeywordsPredicate;
import seedu.address.model.history.CompletedSession;
import seedu.address.model.history.SessionNotes;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.PawPalsBuilder;

public class ModelManagerTest {

    private static final Appointment APPOINTMENT = new Appointment(new AppointmentDate("13-12-2026"),
            new AppointmentTime("14:00")).withEndTime(new AppointmentTime("15:00"));

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new PawPals(), new PawPals(modelManager.getPawPals()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new PawPals(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasClient_nullClient_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasClient(null));
    }

    @Test
    public void hasClient_clientNotInPawPals_returnsFalse() {
        assertFalse(modelManager.hasClient(ALICE));
    }

    @Test
    public void hasClient_clientInPawPals_returnsTrue() {
        modelManager.addClient(ALICE);
        assertTrue(modelManager.hasClient(ALICE));
    }

    @Test
    public void findAppointmentConflict_hiddenClient_returnsConflictWithoutChangingDataOrFilter() {
        Client owner = new ClientBuilder(ALICE).withAppointments(APPOINTMENT).build();
        Model model = modelManager;
        model.addClient(owner);
        model.addClient(BENSON);
        model.updateFilteredClientList(client -> client.isSameClient(BENSON));
        PawPals original = new PawPals(model.getPawPals());

        assertEquals(Optional.of(new Conflict(owner, APPOINTMENT)), model.findAppointmentConflict(APPOINTMENT));
        Appointment adjacent = new Appointment(APPOINTMENT.getDate(), new AppointmentTime("15:00"));
        assertTrue(model.findAppointmentConflict(adjacent).isEmpty());
        assertEquals(original, model.getPawPals());
        assertEquals(List.of(BENSON), model.getFilteredClientList());
    }

    @Test
    public void findAppointmentConflict_noActiveBookings_ignoresHistory() {
        CompletedSession session = new CompletedSession(APPOINTMENT, new SessionNotes("Teddy cut"),
                Instant.parse("2026-12-13T08:00:00Z"));
        Model model = modelManager;
        assertTrue(model.findAppointmentConflict(APPOINTMENT).isEmpty());
        model.addClient(new ClientBuilder(ALICE).withHistory(session).build());
        assertTrue(model.findAppointmentConflict(APPOINTMENT).isEmpty());
    }

    @Test
    public void findAppointmentConflict_excludingOriginal_stillChecksOtherBookingsAndPreservesFilter() {
        Appointment later = new Appointment(APPOINTMENT.getDate(), new AppointmentTime("15:00"))
                .withEndTime(new AppointmentTime("16:00"));
        Client owner = new ClientBuilder(ALICE).withAppointments(APPOINTMENT, later).build();
        Client otherOwner = new ClientBuilder(BENSON).withAppointments(APPOINTMENT).build();
        Model model = modelManager;
        model.addClient(owner);
        model.updateFilteredClientList(client -> client.isSameClient(owner));
        assertTrue(model.findAppointmentConflict(APPOINTMENT, owner, APPOINTMENT).isEmpty());

        Appointment proposed = new Appointment(APPOINTMENT.getDate(), new AppointmentTime("14:30"))
                .withEndTime(new AppointmentTime("15:30"));
        assertEquals(Optional.of(new Conflict(owner, later)),
                model.findAppointmentConflict(proposed, owner, APPOINTMENT));

        model.addClient(otherOwner);
        model.updateFilteredClientList(client -> client.isSameClient(owner));
        PawPals original = new PawPals(model.getPawPals());
        assertEquals(Optional.of(new Conflict(otherOwner, APPOINTMENT)),
                model.findAppointmentConflict(APPOINTMENT, owner, APPOINTMENT));
        assertEquals(original, model.getPawPals());
        assertEquals(List.of(owner), model.getFilteredClientList());
    }

    @Test
    public void findAppointmentConflict_afterClientUpdate_usesCurrentBookings() {
        Model model = modelManager;
        Client owner = new ClientBuilder(ALICE).withAppointments(APPOINTMENT).build();
        model.addClient(owner);
        assertTrue(model.findAppointmentConflict(APPOINTMENT).isPresent());
        model.setClient(owner, owner.withoutAppointment(APPOINTMENT.getDate(), APPOINTMENT.getTime()));
        assertTrue(model.findAppointmentConflict(APPOINTMENT).isEmpty());
    }

    @Test
    public void findAppointmentConflict_nullArguments_throwsNullPointerException() {
        Model model = modelManager;
        assertThrows(NullPointerException.class, () -> model.findAppointmentConflict(null));
        assertThrows(NullPointerException.class, () -> model.findAppointmentConflict(null, ALICE, APPOINTMENT));
        assertThrows(NullPointerException.class, () -> model.findAppointmentConflict(APPOINTMENT, null, APPOINTMENT));
        assertThrows(NullPointerException.class, () -> model.findAppointmentConflict(APPOINTMENT, ALICE, null));
    }

    @Test
    public void getFilteredClientList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredClientList().remove(0));
    }

    @Test
    public void equals() {
        PawPals pawPals = new PawPalsBuilder().withClient(ALICE).withClient(BENSON).build();
        PawPals differentPawPals = new PawPals();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(pawPals, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(pawPals, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different pawPals -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentPawPals, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredClientList(new ClientContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(pawPals, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredClientList(PREDICATE_SHOW_ALL_CLIENTS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(pawPals, differentUserPrefs)));
    }
}
