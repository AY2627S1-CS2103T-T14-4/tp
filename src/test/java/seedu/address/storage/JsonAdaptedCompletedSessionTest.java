package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.history.CompletedSession;
import seedu.address.model.history.SessionNotes;
import seedu.address.model.pet.PetName;

public class JsonAdaptedCompletedSessionTest {
    private static final String LOGGED_AT = "2026-10-21T06:30:00Z";
    private static final Appointment APPOINTMENT = new Appointment(new AppointmentDate("20-10-2026"),
            new AppointmentTime("14:00"), Optional.of(new AppointmentTime("15:00")), "Call on arrival",
            Set.of(new PetName("Deleted pet")));

    @Test
    public void toModelType_roundTrip_preservesAllFieldsAndHistoricalPetNames() throws Exception {
        CompletedSession session = new CompletedSession(APPOINTMENT, new SessionNotes("Teddy cut"),
                Instant.parse(LOGGED_AT));
        assertEquals(session, new JsonAdaptedCompletedSession(session).toModelType());
    }

    @Test
    public void toModelType_trimsNotesAndAcceptsSnapshotWithoutEndTimeOrPets() throws Exception {
        JsonAdaptedCompletedSession adapted = new JsonAdaptedCompletedSession(
                new JsonAdaptedAppointment("20-10-2026", "14:00", "Booking details"), "  Teddy cut  ", LOGGED_AT);
        Appointment appointment = new Appointment(new AppointmentDate("20-10-2026"),
                new AppointmentTime("14:00"), "Booking details");
        CompletedSession expected = new CompletedSession(appointment, new SessionNotes("Teddy cut"),
                Instant.parse(LOGGED_AT));
        assertEquals(expected, adapted.toModelType());
    }

    @Test
    public void toModelType_doesNotReapplyCurrentClockEligibility() throws Exception {
        JsonAdaptedCompletedSession adapted = new JsonAdaptedCompletedSession(
                new JsonAdaptedAppointment("31-12-9999", "23:59", "Stored history"), "Notes", LOGGED_AT);
        assertEquals(new AppointmentDate("31-12-9999"), adapted.toModelType().getAppointment().getDate());
    }

    @Test
    public void toModelType_missingRequiredFields_throwsIllegalValueException() {
        List<JsonAdaptedCompletedSession> invalid = List.of(
                new JsonAdaptedCompletedSession(null, "Notes", LOGGED_AT),
                new JsonAdaptedCompletedSession(new JsonAdaptedAppointment(APPOINTMENT), null, LOGGED_AT),
                new JsonAdaptedCompletedSession(new JsonAdaptedAppointment(APPOINTMENT), "Notes", null));
        String[] fields = {"appointment", "notes", "loggedAt"};
        for (int i = 0; i < invalid.size(); i++) {
            JsonAdaptedCompletedSession adapted = invalid.get(i);
            IllegalValueException exception = assertThrows(IllegalValueException.class, adapted::toModelType);
            assertEquals(String.format(JsonAdaptedCompletedSession.MISSING_FIELD_MESSAGE_FORMAT, fields[i]),
                    exception.getMessage());
        }
    }

    @Test
    public void toModelType_invalidNotes_throwsIllegalValueException() {
        for (String notes : new String[] {"", " \t ", "x".repeat(201)}) {
            JsonAdaptedCompletedSession adapted = new JsonAdaptedCompletedSession(
                    new JsonAdaptedAppointment(APPOINTMENT), notes, LOGGED_AT);
            IllegalValueException exception = assertThrows(IllegalValueException.class, adapted::toModelType);
            assertEquals(SessionNotes.MESSAGE_CONSTRAINTS, exception.getMessage());
        }
    }

    @Test
    public void toModelType_invalidLoggingInstant_throwsIllegalValueException() {
        for (String loggedAt : new String[] {"", "not-a-date", "2026-10-21T06:30:00", "2026-02-30T00:00:00Z"}) {
            JsonAdaptedCompletedSession adapted = new JsonAdaptedCompletedSession(
                    new JsonAdaptedAppointment(APPOINTMENT), "Notes", loggedAt);
            IllegalValueException exception = assertThrows(IllegalValueException.class, adapted::toModelType);
            assertEquals(JsonAdaptedCompletedSession.MESSAGE_INVALID_LOGGED_AT, exception.getMessage());
        }
    }

    @Test
    public void toModelType_invalidSnapshot_throwsIllegalValueException() {
        for (JsonAdaptedAppointment appointment : List.of(
                new JsonAdaptedAppointment("31-02-2026", "14:00", ""),
                new JsonAdaptedAppointment("20-10-2026", "24:00", ""),
                new JsonAdaptedAppointment("20-10-2026", "14:00", "13:00", "", List.of()))) {
            JsonAdaptedCompletedSession adapted = new JsonAdaptedCompletedSession(appointment, "Notes", LOGGED_AT);
            assertThrows(IllegalValueException.class, adapted::toModelType);
        }
    }
}
