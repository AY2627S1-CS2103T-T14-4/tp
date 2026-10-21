package seedu.address.storage;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.history.CompletedSession;
import seedu.address.model.history.SessionNotes;

/**
 * Jackson-friendly representation of a completed appointment snapshot and session notes.
 */
class JsonAdaptedCompletedSession {
    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Completed session's %s field is missing!";
    public static final String MESSAGE_INVALID_LOGGED_AT = "Session loggedAt must be a valid ISO-8601 instant.";

    private final JsonAdaptedAppointment appointment;
    private final String notes;
    private final String loggedAt;

    /**
     * Creates a stored session. Fields are validated when converting to the model.
     */
    @JsonCreator
    public JsonAdaptedCompletedSession(@JsonProperty("appointment") JsonAdaptedAppointment appointment,
            @JsonProperty("notes") String notes, @JsonProperty("loggedAt") String loggedAt) {
        this.appointment = appointment;
        this.notes = notes;
        this.loggedAt = loggedAt;
    }

    public JsonAdaptedCompletedSession(CompletedSession source) {
        appointment = new JsonAdaptedAppointment(source.getAppointment());
        notes = source.getNotes().toString();
        loggedAt = source.getLoggedAt().toString();
    }

    /**
     * Reads a session without resolving pet names against current pets or applying current-clock validation.
     */
    public CompletedSession toModelType() throws IllegalValueException {
        if (appointment == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "appointment"));
        }
        if (notes == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "notes"));
        }
        if (loggedAt == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "loggedAt"));
        }
        if (!SessionNotes.isValidNotes(notes)) {
            throw new IllegalValueException(SessionNotes.MESSAGE_CONSTRAINTS);
        }
        try {
            return new CompletedSession(appointment.toModelType(), new SessionNotes(notes), Instant.parse(loggedAt));
        } catch (DateTimeParseException e) {
            throw new IllegalValueException(MESSAGE_INVALID_LOGGED_AT, e);
        }
    }
}
