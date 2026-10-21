package seedu.address.model.history;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.appointment.Appointment;

/**
 * An immutable appointment snapshot and the notes recorded when its grooming session was logged.
 * Historical pet names are preserved even when the client's current pets are renamed or deleted.
 */
public final class CompletedSession {
    public static final Comparator<CompletedSession> NEWEST_FIRST = Comparator
            .comparing((CompletedSession session) -> session.getAppointment().getDate().getValue())
            .thenComparing(session -> session.getAppointment().getTime().getValue())
            .thenComparing(CompletedSession::getLoggedAt)
            .reversed();

    private final Appointment appointment;
    private final SessionNotes notes;
    private final Instant loggedAt;

    /**
     * Creates a completed-session record using an immutable appointment snapshot.
     * Loading history does not recheck the current clock or require historical pets to still exist.
     */
    public CompletedSession(Appointment appointment, SessionNotes notes, Instant loggedAt) {
        requireAllNonNull(appointment, notes, loggedAt);
        this.appointment = appointment;
        this.notes = notes;
        this.loggedAt = loggedAt;
    }

    /**
     * Returns the immutable appointment as recorded at completion, including its historical pet names.
     */
    public Appointment getAppointment() {
        return appointment;
    }

    public SessionNotes getNotes() {
        return notes;
    }

    /**
     * Returns when the record was logged, rather than the scheduled or actual grooming finish time.
     */
    public Instant getLoggedAt() {
        return loggedAt;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof CompletedSession otherSession
                && appointment.equals(otherSession.appointment)
                && notes.equals(otherSession.notes)
                && loggedAt.equals(otherSession.loggedAt));
    }

    @Override
    public int hashCode() {
        return Objects.hash(appointment, notes, loggedAt);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("appointment", appointment).add("notes", notes)
                .add("loggedAt", loggedAt).toString();
    }
}
