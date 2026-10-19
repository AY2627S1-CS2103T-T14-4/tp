package seedu.address.storage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
 * The linked pets field is optional, so data files saved before pets could be linked still load.
 */
class JsonAdaptedAppointment {
    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Appointment's %s field is missing!";

    private final String date;
    private final String time;
    private final String details;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<String> pets = new ArrayList<>();

    /**
     * Creates an appointment from stored data.
     * Details and pets may be absent for appointments without notes or linked pets.
     */
    @JsonCreator
    public JsonAdaptedAppointment(@JsonProperty("date") String date, @JsonProperty("time") String time,
            @JsonProperty("details") String details, @JsonProperty("pets") List<String> pets) {
        this.date = date;
        this.time = time;
        this.details = details;
        if (pets != null) {
            this.pets.addAll(pets);
        }
    }

    /**
     * Creates an appointment from stored data with no linked pets.
     */
    public JsonAdaptedAppointment(String date, String time, String details) {
        this(date, time, details, null);
    }

    /**
     * Converts a model appointment to its stored representation.
     */
    public JsonAdaptedAppointment(Appointment source) {
        date = source.getDate().toString();
        time = source.getTime().toString();
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
        return new Appointment(new AppointmentDate(date), new AppointmentTime(time),
                details == null ? "" : details, petNames);
    }
}
