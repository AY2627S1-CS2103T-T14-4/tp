package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;

/**
 * Jackson-friendly representation of an appointment.
 */
class JsonAdaptedAppointment {
    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Appointment's %s field is missing!";

    private final String date;
    private final String time;
    private final String details;

    /**
     * Creates an appointment from stored data. Details may be absent for appointments without notes.
     */
    @JsonCreator
    public JsonAdaptedAppointment(@JsonProperty("date") String date, @JsonProperty("time") String time,
            @JsonProperty("details") String details) {
        this.date = date;
        this.time = time;
        this.details = details;
    }

    /**
     * Converts a model appointment to its stored representation.
     */
    public JsonAdaptedAppointment(Appointment source) {
        date = source.getDate().toString();
        time = source.getTime().toString();
        details = source.getDetails();
    }

    /**
     * Converts stored values to an appointment without requiring the date/time to remain in the future.
     *
     * @throws IllegalValueException if a required field is missing or invalid.
     */
    public Appointment toModelType() throws IllegalValueException {
        if (date == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    AppointmentDate.class.getSimpleName()));
        }
        if (!AppointmentDate.isValidDate(date)) {
            throw new IllegalValueException(AppointmentDate.MESSAGE_CONSTRAINTS);
        }
        if (time == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    AppointmentTime.class.getSimpleName()));
        }
        if (!AppointmentTime.isValidTime(time)) {
            throw new IllegalValueException(AppointmentTime.MESSAGE_CONSTRAINTS);
        }
        return new Appointment(new AppointmentDate(date), new AppointmentTime(time),
                details == null ? "" : details);
    }
}
