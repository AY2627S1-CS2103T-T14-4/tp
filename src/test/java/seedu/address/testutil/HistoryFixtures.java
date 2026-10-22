package seedu.address.testutil;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;

import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.model.history.CompletedSession;
import seedu.address.model.history.SessionNotes;
import seedu.address.model.pet.PetName;

/** Representative immutable history records, supplied out of order to exercise model ordering. */
public final class HistoryFixtures {
    public static final CompletedSession OLDEST = session("17-10-2026", "09:00", "Oldest visit");
    public static final CompletedSession SECOND = session("18-10-2026", "10:00", "Gentle shampoo", "Milo");
    public static final CompletedSession THIRD = session("19-10-2026", "11:00", "Short trim", "Luna");
    public static final CompletedSession NEWEST = new CompletedSession(
            session("20-10-2026", "14:00", "Teddy cut, short ears", "Milo", "Luna").getAppointment()
                    .withEndTime(new AppointmentTime("15:30")),
            new SessionNotes("Teddy cut, short ears"), Instant.parse("2026-10-20T08:00:00Z"));
    public static final List<CompletedSession> SESSIONS = List.of(OLDEST, THIRD, SECOND, NEWEST);

    private HistoryFixtures() {
    }

    /** Returns a client with zero through four sessions, retaining all supplied records. */
    public static Client clientWithHistory(int count) {
        return new ClientBuilder().withHistory(SESSIONS.subList(0, count).toArray(CompletedSession[]::new)).build();
    }

    /** Creates a historical snapshot whose pet names need not exist in the current client. */
    public static CompletedSession session(String date, String time, String notes, String... pets) {
        Appointment appointment = new Appointment(new AppointmentDate(date), new AppointmentTime(time),
                "Original booking details", new LinkedHashSet<>(List.of(pets).stream().map(PetName::new).toList()));
        return new CompletedSession(appointment, new SessionNotes(notes), Instant.parse("2026-10-21T08:00:00Z"));
    }
}
