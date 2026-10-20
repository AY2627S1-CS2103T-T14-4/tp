package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.pet.PetName;

public class JsonAdaptedAppointmentTest {
    @Test
    public void toModelType_validPastAppointment_preservesDetails() throws Exception {
        Appointment original = new Appointment(new AppointmentDate("29-02-2024"),
                new AppointmentTime("09:05"), "Call before arrival");
        assertEquals(original, new JsonAdaptedAppointment(original).toModelType());
    }

    @Test
    public void toModelType_missingOptionalDetails_defaultsToEmpty() throws Exception {
        Appointment expected = new Appointment(new AppointmentDate("01-01-2025"), new AppointmentTime("00:00"));
        assertEquals(expected, new JsonAdaptedAppointment("01-01-2025", "00:00", null).toModelType());
    }

    @Test
    public void toModelType_missingOrInvalidDate_throwsIllegalValueException() {
        JsonAdaptedAppointment missingDate = new JsonAdaptedAppointment(null, "09:05", "");
        JsonAdaptedAppointment invalidDate = new JsonAdaptedAppointment("29-02-2025", "09:05", "");
        assertThrows(IllegalValueException.class, String.format(JsonAdaptedAppointment.MISSING_FIELD_MESSAGE_FORMAT,
                        AppointmentDate.class.getSimpleName()), missingDate::toModelType);
        assertThrows(IllegalValueException.class, AppointmentDate.MESSAGE_CONSTRAINTS, invalidDate::toModelType);
    }

    @Test
    public void toModelType_missingOrInvalidTime_throwsIllegalValueException() {
        JsonAdaptedAppointment missingTime = new JsonAdaptedAppointment("01-01-2025", null, "");
        JsonAdaptedAppointment invalidTime = new JsonAdaptedAppointment("01-01-2025", "24:00", "");
        assertThrows(IllegalValueException.class, String.format(JsonAdaptedAppointment.MISSING_FIELD_MESSAGE_FORMAT,
                        AppointmentTime.class.getSimpleName()), missingTime::toModelType);
        assertThrows(IllegalValueException.class, AppointmentTime.MESSAGE_CONSTRAINTS, invalidTime::toModelType);
    }
    @Test
    public void toModelType_linkedPets_preservesPetsInOrder() throws Exception {
        Set<PetName> petNames = new LinkedHashSet<>(List.of(new PetName("Milo"), new PetName("Luna")));
        Appointment original = new Appointment(new AppointmentDate("29-02-2024"),
                new AppointmentTime("09:05"), "", petNames);
        assertEquals(original, new JsonAdaptedAppointment(original).toModelType());
    }

    @Test
    public void toModelType_missingPets_returnsAppointmentWithoutPets() throws Exception {
        // Appointments saved by v1.3 have no pets field at all
        JsonAdaptedAppointment legacy = new JsonAdaptedAppointment("01-01-2025", "00:00", "", null);
        assertTrue(legacy.toModelType().getPetNames().isEmpty());
    }

    @Test
    public void toModelType_invalidPetName_throwsIllegalValueException() {
        JsonAdaptedAppointment blankPet = new JsonAdaptedAppointment("01-01-2025", "09:05", "", List.of(" "));
        JsonAdaptedAppointment invalidPet = new JsonAdaptedAppointment("01-01-2025", "09:05", "", List.of("Milo!"));
        JsonAdaptedAppointment nullPet = new JsonAdaptedAppointment("01-01-2025", "09:05", "",
                Arrays.asList((String) null));
        assertThrows(IllegalValueException.class, PetName.MESSAGE_CONSTRAINTS, blankPet::toModelType);
        assertThrows(IllegalValueException.class, PetName.MESSAGE_CONSTRAINTS, nullPet::toModelType);
        assertThrows(IllegalValueException.class, PetName.MESSAGE_CONSTRAINTS, invalidPet::toModelType);
    }

    @Test
    public void toModelType_withEndTime_preservesEndTime() throws Exception {
        Appointment original = new Appointment(new AppointmentDate("12-12-2026"), new AppointmentTime("14:00"),
                Optional.of(new AppointmentTime("15:30")), "Bath", Set.of(new PetName("Milo")));
        assertEquals(original, new JsonAdaptedAppointment(original).toModelType());
    }

    @Test
    public void toModelType_missingEndTime_returnsAppointmentWithoutEndTime() throws Exception {
        // Appointments saved by v1.3 have no endTime field at all
        JsonAdaptedAppointment legacy = new JsonAdaptedAppointment("12-12-2026", "14:00", null, "", null);
        assertTrue(legacy.toModelType().getEndTime().isEmpty());
    }

    @Test
    public void toModelType_invalidEndTime_throwsIllegalValueException() {
        JsonAdaptedAppointment invalidEnd = new JsonAdaptedAppointment("12-12-2026", "14:00", "25:00", "", null);
        JsonAdaptedAppointment endBeforeStart = new JsonAdaptedAppointment("12-12-2026", "14:00", "13:00", "", null);
        JsonAdaptedAppointment endAtStart = new JsonAdaptedAppointment("12-12-2026", "14:00", "14:00", "", null);

        assertThrows(IllegalValueException.class, AppointmentTime.MESSAGE_CONSTRAINTS, invalidEnd::toModelType);
        assertThrows(IllegalValueException.class, Appointment.MESSAGE_END_NOT_AFTER_START,
                endBeforeStart::toModelType);
        assertThrows(IllegalValueException.class, Appointment.MESSAGE_END_NOT_AFTER_START, endAtStart::toModelType);
    }
}
