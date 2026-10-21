package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.PawPals;
import seedu.address.model.appointment.AppointmentConflictChecker.Conflict;
import seedu.address.model.client.Client;
import seedu.address.model.history.CompletedSession;
import seedu.address.model.history.SessionNotes;
import seedu.address.testutil.ClientBuilder;

public class AppointmentConflictCheckerTest {
    private static final Appointment EXISTING = slot("14:00", "15:00");

    @Test
    public void findConflict_emptyClientsOrNoAppointments_returnsEmpty() {
        assertTrue(AppointmentConflictChecker.findConflict(new PawPals(), EXISTING).isEmpty());
        assertTrue(AppointmentConflictChecker.findConflict(data(ALICE, BENSON), EXISTING).isEmpty());
    }

    @Test
    public void findConflict_multipleBookings_returnsFirstConflictingOwnerAndFullAppointment() {
        Appointment earlier = slot("12:00", "13:00");
        Client alice = new ClientBuilder(ALICE).withAppointments(earlier, EXISTING).build();
        Client benson = new ClientBuilder(BENSON).withAppointments(EXISTING).build();
        PawPals data = data(alice, benson);
        PawPals original = new PawPals(data);
        assertEquals(Optional.of(new Conflict(alice, EXISTING)),
                AppointmentConflictChecker.findConflict(data, slot("14:30", "15:30")));
        assertEquals(original, data);
    }

    @Test
    public void findConflict_withoutEndTimes_usesStartingMinuteAndExclusiveEndBoundaries() {
        for (String start : List.of("14:00", "14:59")) {
            assertTrue(AppointmentConflictChecker.findConflict(
                    data(new ClientBuilder(ALICE).withAppointments(EXISTING).build()), slot(start, null)).isPresent());
        }
        PawPals pointBooking = data(new ClientBuilder(ALICE).withAppointments(slot("14:00", null)).build());
        for (Appointment candidate : List.of(slot("14:00", null), slot("13:59", "14:01"), EXISTING)) {
            assertTrue(AppointmentConflictChecker.findConflict(pointBooking, candidate).isPresent());
        }
        for (Appointment candidate : List.of(slot("14:01", null), slot("13:00", "14:00"),
                slot("14:01", "15:00"))) {
            assertTrue(AppointmentConflictChecker.findConflict(pointBooking, candidate).isEmpty());
        }
        PawPals lastMinute = data(new ClientBuilder(ALICE).withAppointments(slot("23:59", null)).build());
        assertTrue(AppointmentConflictChecker.findConflict(lastMinute, slot("23:59", null)).isPresent());
        assertTrue(AppointmentConflictChecker.findConflict(lastMinute,
                new Appointment(new AppointmentDate("14-12-2026"), new AppointmentTime("00:00"))).isEmpty());
    }

    @Test
    public void findConflict_historyOnly_doesNotBlockBookingButActiveAppointmentDoes() {
        CompletedSession session = new CompletedSession(EXISTING, new SessionNotes("Teddy cut"),
                Instant.parse("2026-12-13T08:00:00Z"));
        Client historical = new ClientBuilder(ALICE).withHistory(session).build();
        assertTrue(AppointmentConflictChecker.findConflict(data(historical, BENSON), EXISTING).isEmpty());
        Client active = new ClientBuilder(BENSON).withAppointments(EXISTING).build();
        assertEquals(Optional.of(new Conflict(active, EXISTING)),
                AppointmentConflictChecker.findConflict(data(historical, active), EXISTING));
    }

    @Test
    public void findConflict_exclusion_matchesOwnerAndSlotRatherThanObjectOrDetails() {
        Client owner = new ClientBuilder(ALICE).withAppointments(EXISTING).build();
        Client sameIdentity = new ClientBuilder(owner).withAddress("Updated address").build();
        Appointment sameSlot = new Appointment(EXISTING.getDate(), EXISTING.getTime(), "Different details");
        PawPals data = data(owner, BENSON);
        assertTrue(AppointmentConflictChecker.findConflict(data, EXISTING, sameIdentity, sameSlot).isEmpty());
        assertEquals(List.of(EXISTING), owner.getAppointments());
        assertEquals(Optional.of(new Conflict(owner, EXISTING)),
                AppointmentConflictChecker.findConflict(data, EXISTING, owner, slot("13:00", null)));
    }

    @Test
    public void findConflict_exclusion_stillChecksOtherAppointmentsOfSameClient() {
        Appointment other = slot("15:00", "16:00");
        Client owner = new ClientBuilder(ALICE).withAppointments(EXISTING, other).build();
        assertEquals(Optional.of(new Conflict(owner, other)), AppointmentConflictChecker.findConflict(
                data(owner), slot("14:30", "15:30"), owner, EXISTING));
    }

    @Test
    public void findConflict_exclusion_doesNotExcludeOtherClientsAtSameSlot() {
        Client alice = new ClientBuilder(ALICE).withAppointments(EXISTING).build();
        Client benson = new ClientBuilder(BENSON).withAppointments(EXISTING).build();
        assertEquals(Optional.of(new Conflict(benson, EXISTING)),
                AppointmentConflictChecker.findConflict(data(alice, benson), EXISTING, alice, EXISTING));
    }

    @Test
    public void findConflict_nullArguments_throwsNullPointerException() {
        PawPals data = data(ALICE);
        assertThrows(NullPointerException.class, () -> AppointmentConflictChecker.findConflict(null, EXISTING));
        assertThrows(NullPointerException.class, () -> AppointmentConflictChecker.findConflict(data, null));
        assertThrows(NullPointerException.class, ()
                -> AppointmentConflictChecker.findConflict(data, EXISTING, null, EXISTING));
        assertThrows(NullPointerException.class, ()
                -> AppointmentConflictChecker.findConflict(data, EXISTING, ALICE, null));
        assertThrows(NullPointerException.class, ()
                -> AppointmentConflictChecker.findConflict(null, EXISTING, ALICE, EXISTING));
        assertThrows(NullPointerException.class, ()
                -> AppointmentConflictChecker.findConflict(data, null, ALICE, EXISTING));
    }

    private static Appointment slot(String start, String end) {
        return new Appointment(new AppointmentDate("13-12-2026"), new AppointmentTime(start),
                Optional.ofNullable(end).map(AppointmentTime::new), "", Set.of());
    }

    private static PawPals data(Client... clients) {
        PawPals data = new PawPals();
        for (Client client : clients) {
            data.addClient(client);
        }
        return data;
    }
}
