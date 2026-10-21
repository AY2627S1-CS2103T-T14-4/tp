package seedu.address.model.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.pet.PetName;

public class CompletedSessionTest {
    private static final Appointment APPOINTMENT = new Appointment(new AppointmentDate("21-10-2026"),
            new AppointmentTime("14:00"), Optional.of(new AppointmentTime("15:00")), "Call on arrival",
            Set.of(new PetName("Milo")));
    private static final SessionNotes NOTES = new SessionNotes("Teddy cut");
    private static final Instant LOGGED_AT = Instant.parse("2026-10-21T06:30:00Z");

    @Test
    public void constructor_nullFields_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CompletedSession(null, NOTES, LOGGED_AT));
        assertThrows(NullPointerException.class, () -> new CompletedSession(APPOINTMENT, null, LOGGED_AT));
        assertThrows(NullPointerException.class, () -> new CompletedSession(APPOINTMENT, NOTES, null));
    }

    @Test
    public void constructor_retainsCompleteImmutableSnapshotAndSeparateNotes() {
        Set<PetName> names = new LinkedHashSet<>(List.of(new PetName("Milo"), new PetName("Luna")));
        Appointment appointment = APPOINTMENT.withPetNames(names);
        CompletedSession session = new CompletedSession(appointment, NOTES, LOGGED_AT);
        names.clear();

        assertEquals(appointment, session.getAppointment());
        assertEquals(List.of(new PetName("Milo"), new PetName("Luna")),
                List.copyOf(session.getAppointment().getPetNames()));
        assertEquals("Call on arrival", session.getAppointment().getDetails());
        assertEquals(NOTES, session.getNotes());
        assertEquals(LOGGED_AT, session.getLoggedAt());
        assertThrows(UnsupportedOperationException.class, () -> session.getAppointment().getPetNames().clear());
    }

    @Test
    public void equalsAndHashCode_includeSnapshotNotesAndLoggingInstant() {
        CompletedSession session = new CompletedSession(APPOINTMENT, NOTES, LOGGED_AT);
        CompletedSession same = new CompletedSession(APPOINTMENT, new SessionNotes("Teddy cut"),
                Instant.parse("2026-10-21T06:30:00Z"));
        assertEquals(session, session);
        assertEquals(session, same);
        assertEquals(session.hashCode(), same.hashCode());
        assertNotEquals(session, new CompletedSession(APPOINTMENT.withPetNames(Set.of()), NOTES, LOGGED_AT));
        assertNotEquals(session, new CompletedSession(APPOINTMENT, new SessionNotes("Short ears"), LOGGED_AT));
        assertNotEquals(session, new CompletedSession(APPOINTMENT, NOTES, LOGGED_AT.plusSeconds(1)));
        assertNotEquals(session, null);
        assertNotEquals(session, APPOINTMENT);
    }

    @Test
    public void toString_includesSnapshotNotesAndLoggingInstant() {
        CompletedSession session = new CompletedSession(APPOINTMENT, NOTES, LOGGED_AT);
        assertEquals(CompletedSession.class.getCanonicalName() + "{appointment=" + APPOINTMENT
                + ", notes=" + NOTES + ", loggedAt=" + LOGGED_AT + "}", session.toString());
    }
}
