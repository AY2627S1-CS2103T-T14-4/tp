package seedu.address.model.appointment;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents an appointment's date, time, and free-text details.
 * Guarantees: immutable; all fields are non-null and date and time values are validated.
 * An empty details string represents an appointment with no additional details.
 */
public final class Appointment {

    private final AppointmentDate date;
    private final AppointmentTime time;
    private final String details;

    /**
     * Constructs an {@code Appointment} without additional details.
     *
     * @throws NullPointerException if either field is null.
     */
    public Appointment(AppointmentDate date, AppointmentTime time) {
        this(date, time, "");
    }

    /**
     * Constructs an {@code Appointment} with the given details, preserving the text as entered.
     *
     * @throws NullPointerException if any field is null.
     */
    public Appointment(AppointmentDate date, AppointmentTime time, String details) {
        requireAllNonNull(date, time, details);
        this.date = date;
        this.time = time;
        this.details = details;
    }

    public AppointmentDate getDate() {
        return date;
    }

    public AppointmentTime getTime() {
        return time;
    }

    public String getDetails() {
        return details;
    }

    /**
     * Returns true if both appointments have the same date, time, and details.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Appointment otherAppointment)) {
            return false;
        }
        return date.equals(otherAppointment.date)
                && time.equals(otherAppointment.time)
                && details.equals(otherAppointment.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, details);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("date", date)
                .add("time", time)
                .add("details", details)
                .toString();
    }
}
