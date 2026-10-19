package seedu.address.model.appointment;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.pet.PetName;

/**
 * Represents an appointment's date, start time, optional end time, free-text details and the pets it covers.
 * Guarantees: immutable; all fields except the end time are non-null, date and time values are validated,
 * and the end time, if present, is after the start time on the same date.
 * An empty details string represents an appointment with no additional details.
 * An empty set of pet names means the appointment is not linked to specific pets.
 */
public final class Appointment {

    public static final String MESSAGE_END_NOT_AFTER_START = "End time must be after the start time.";

    private final AppointmentDate date;
    private final AppointmentTime time;
    private final AppointmentTime endTime; // null if the appointment has no end time
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
        this(date, time, Optional.empty(), details, petNames);
    }

    /**
     * Constructs an {@code Appointment} with an optional end time, linked to the given pets.
     *
     * @param date The validated calendar date.
     * @param time The validated start time.
     * @param endTime The end time, or empty if the appointment has no end time.
     * @param details The optional notes; an empty string means no additional details.
     * @param petNames The names of the pets this appointment covers; may be empty.
     * @throws NullPointerException if any field or pet name is null.
     * @throws IllegalArgumentException if the end time is not after the start time.
     */
    public Appointment(AppointmentDate date, AppointmentTime time, Optional<AppointmentTime> endTime,
            String details, Set<PetName> petNames) {
        requireAllNonNull(date, time, endTime, details, petNames);
        petNames.forEach(Objects::requireNonNull);
        if (endTime.isPresent() && !isValidEndTime(time, endTime.get())) {
            throw new IllegalArgumentException(MESSAGE_END_NOT_AFTER_START);
        }
        this.date = date;
        this.time = time;
        this.endTime = endTime.orElse(null);
        this.details = details;
        this.petNames = Collections.unmodifiableSet(new LinkedHashSet<>(petNames));
    }

    /**
     * Returns true if {@code endTime} is strictly after {@code startTime}.
     *
     * @param startTime The appointment's start time.
     * @param endTime The proposed end time.
     * @return Whether the end time is valid for an appointment starting at {@code startTime}.
     */
    public static boolean isValidEndTime(AppointmentTime startTime, AppointmentTime endTime) {
        requireAllNonNull(startTime, endTime);
        return endTime.getValue().isAfter(startTime.getValue());
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
     * Returns the appointment's end time, if it has one.
     *
     * @return The end time, or an empty optional for an appointment recorded with a start time only.
     */
    public Optional<AppointmentTime> getEndTime() {
        return Optional.ofNullable(endTime);
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
     * @return A new appointment with the same date, times and details.
     */
    public Appointment withPetNames(Set<PetName> newPetNames) {
        return new Appointment(date, time, getEndTime(), details, newPetNames);
    }

    /**
     * Returns a copy of this appointment with the given end time.
     *
     * @param newEndTime The end time of the copy.
     * @return A new appointment with the same date, start time, details and pets.
     * @throws IllegalArgumentException if {@code newEndTime} is not after the start time.
     */
    public Appointment withEndTime(AppointmentTime newEndTime) {
        requireNonNull(newEndTime);
        return new Appointment(date, time, Optional.of(newEndTime), details, petNames);
    }

    /**
     * Returns true if this appointment and {@code otherAppointment} overlap in time on the same date.
     * Each appointment covers the time from its start up to, but not including, its end, so an appointment
     * ending at 15:00 does not overlap one starting at 15:00. An appointment without an end time is treated
     * as a single moment at its start time.
     *
     * @param otherAppointment The appointment to compare with.
     * @return Whether the two appointments overlap.
     * @throws NullPointerException if {@code otherAppointment} is null.
     */
    public boolean overlaps(Appointment otherAppointment) {
        requireNonNull(otherAppointment);
        return date.equals(otherAppointment.date)
                && startMinute() < otherAppointment.endMinute()
                && otherAppointment.startMinute() < endMinute();
    }

    /**
     * Returns the start time as minutes after midnight.
     */
    private int startMinute() {
        return toMinuteOfDay(time);
    }

    /**
     * Returns the minute after midnight at which this appointment stops, exclusive.
     * Without an end time the appointment covers only the minute it starts in.
     */
    private int endMinute() {
        return endTime == null ? startMinute() + 1 : toMinuteOfDay(endTime);
    }

    /**
     * Returns the given time as minutes after midnight.
     */
    private static int toMinuteOfDay(AppointmentTime appointmentTime) {
        return appointmentTime.getValue().toSecondOfDay() / 60;
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
     * Returns true if both appointments have the same date, start and end time, details and linked pets.
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
                && Objects.equals(endTime, otherAppointment.endTime)
                && details.equals(otherAppointment.details)
                && petNames.equals(otherAppointment.petNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, endTime, details, petNames);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("date", date)
                .add("time", time)
                .add("endTime", getEndTime().map(AppointmentTime::toString).orElse(""))
                .add("details", details)
                .add("pets", petNames)
                .toString();
    }
}
