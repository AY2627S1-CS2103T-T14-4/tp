package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;

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
}
