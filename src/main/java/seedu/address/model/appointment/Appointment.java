package seedu.address.model.appointment;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.pet.PetName;

/**
 * Represents an appointment's date, time, free-text details and the pets it covers.
 * Guarantees: immutable; all fields are non-null and date and time values are validated.
 * An empty details string represents an appointment with no additional details.
 * An empty set of pet names means the appointment is not linked to specific pets.
 */
public final class Appointment {

    private final AppointmentDate date;
    private final AppointmentTime time;
    private final String details;
    private final Set<PetName> petNames;

    /**
     * Constructs an {@code Appointment} without additional details.
     *
     * @param date The validated calendar date.
     * @param time The validated time of day.
     * @throws NullPointerException if either field is null.
     */
    public Appointment(AppointmentDate date, AppointmentTime time) {
        this(date, time, "");
    }

    /**
     * Constructs an {@code Appointment} with the given details, preserving the text as entered.
     *
     * @param date The validated calendar date.
     * @param time The validated time of day.
     * @param details The optional notes; an empty string means no additional details.
     * @throws NullPointerException if any field is null.
     */
    public Appointment(AppointmentDate date, AppointmentTime time, String details) {
        this(date, time, details, Set.of());
    }

    /**
     * Constructs an {@code Appointment} linked to the given pets, keeping their order.
     *
     * @param date The validated calendar date.
     * @param time The validated time of day.
     * @param details The optional notes; an empty string means no additional details.
     * @param petNames The names of the pets this appointment covers; may be empty.
     * @throws NullPointerException if any field or pet name is null.
     */
    public Appointment(AppointmentDate date, AppointmentTime time, String details, Set<PetName> petNames) {
        requireAllNonNull(date, time, details, petNames);
        petNames.forEach(Objects::requireNonNull);
        this.date = date;
        this.time = time;
        this.details = details;
        this.petNames = Collections.unmodifiableSet(new LinkedHashSet<>(petNames));
    }

    /**
     * Returns the appointment's calendar date.
     *
     * @return The immutable date value.
     */
    public AppointmentDate getDate() {
        return date;
    }

    /**
     * Returns the appointment's time of day.
     *
     * @return The immutable time value.
     */
    public AppointmentTime getTime() {
        return time;
    }

    /**
     * Returns the appointment's notes as entered.
     *
     * @return The non-null details string, which may be empty.
     */
    public String getDetails() {
        return details;
    }

    /**
     * Returns the names of the pets this appointment covers, in the order they were given.
     *
     * @return An unmodifiable set, which is empty if the appointment is not linked to specific pets.
     */
    public Set<PetName> getPetNames() {
        return petNames;
    }

    /**
     * Returns a copy of this appointment linked to the given pets instead of its current ones.
     *
     * @param newPetNames The names of the pets the copy covers; may be empty.
     * @return A new appointment with the same date, time and details.
     */
    public Appointment withPetNames(Set<PetName> newPetNames) {
        return new Appointment(date, time, details, newPetNames);
    }

    /**
     * Returns true if both appointments have the same date and time, regardless of details.
     * This identity comparison is intended for appointments belonging to the same client.
     *
     * @param otherAppointment The appointment to compare, or null.
     * @return Whether the date and time match; false for null. Details are ignored.
     */
    public boolean hasSameDateTime(Appointment otherAppointment) {
        return otherAppointment != null
                && date.equals(otherAppointment.date)
                && time.equals(otherAppointment.time);
    }

    /**
     * Returns true if both appointments have the same date, time, details and linked pets.
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
                && details.equals(otherAppointment.details)
                && petNames.equals(otherAppointment.petNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, details, petNames);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("date", date)
                .add("time", time)
                .add("details", details)
                .add("pets", petNames)
                .toString();
    }
}
