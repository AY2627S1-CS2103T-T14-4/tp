package seedu.address.storage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.pet.PetName;

/**
 * Jackson-friendly representation of an appointment.
 * The end time and linked pets fields are optional, so data files saved before they existed still load.
 */
class JsonAdaptedAppointment {
    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Appointment's %s field is missing!";

    private final String date;
    private final String time;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String endTime;
    private final String details;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<String> pets = new ArrayList<>();

    /**
     * Creates an appointment from stored data.
     * The end time, details and pets may be absent for appointments without them.
     */
    @JsonCreator
    public JsonAdaptedAppointment(@JsonProperty("date") String date, @JsonProperty("time") String time,
            @JsonProperty("endTime") String endTime, @JsonProperty("details") String details,
            @JsonProperty("pets") List<String> pets) {
        this.date = date;
        this.time = time;
        this.endTime = endTime;
        this.details = details;
        if (pets != null) {
            this.pets.addAll(pets);
        }
    }

    /**
     * Creates an appointment from stored data with no end time.
     */
    public JsonAdaptedAppointment(String date, String time, String details, List<String> pets) {
        this(date, time, null, details, pets);
    }

    /**
     * Creates an appointment from stored data with no end time or linked pets.
     */
    public JsonAdaptedAppointment(String date, String time, String details) {
        this(date, time, null, details, null);
    }

    /**
     * Converts a model appointment to its stored representation.
     */
    public JsonAdaptedAppointment(Appointment source) {
        date = source.getDate().toString();
        time = source.getTime().toString();
        endTime = source.getEndTime().map(AppointmentTime::toString).orElse(null);
        details = source.getDetails();
        source.getPetNames().forEach(petName -> pets.add(petName.toString()));
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
        AppointmentTime modelTime = new AppointmentTime(time);
        Optional<AppointmentTime> modelEndTime = toModelEndTime(modelTime);
        Set<PetName> petNames = new LinkedHashSet<>();
        for (String pet : pets) {
            if (pet == null) {
                throw new IllegalValueException(PetName.MESSAGE_CONSTRAINTS);
            }
            try {
                petNames.add(new PetName(pet));
            } catch (IllegalArgumentException e) {
                throw new IllegalValueException(PetName.MESSAGE_CONSTRAINTS, e);
            }
        }
        return new Appointment(new AppointmentDate(date), modelTime, modelEndTime,
                details == null ? "" : details, petNames);
    }

    /**
     * Converts the stored end time, which is optional, to a model end time.
     *
     * @throws IllegalValueException if the end time is present but invalid or not after {@code startTime}.
     */
    private Optional<AppointmentTime> toModelEndTime(AppointmentTime startTime) throws IllegalValueException {
        if (endTime == null) {
            return Optional.empty();
        }
        if (!AppointmentTime.isValidTime(endTime)) {
            throw new IllegalValueException(AppointmentTime.MESSAGE_CONSTRAINTS);
        }
        AppointmentTime modelEndTime = new AppointmentTime(endTime);
        if (!Appointment.isValidEndTime(startTime, modelEndTime)) {
            throw new IllegalValueException(Appointment.MESSAGE_END_NOT_AFTER_START);
        }
        return Optional.of(modelEndTime);
    }
}
