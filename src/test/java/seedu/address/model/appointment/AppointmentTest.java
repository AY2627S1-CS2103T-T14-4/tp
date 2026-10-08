package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class AppointmentTest {

    private final AppointmentDate date = new AppointmentDate("08-10-2026");
    private final AppointmentTime time = new AppointmentTime("09:05");
    private final Appointment appointment = new Appointment(date, time, "First visit; call on arrival.");

    @Test
    public void constructor_nullDate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Appointment(null, time, "Details"));
    }

    @Test
    public void constructor_nullTime_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Appointment(date, null, "Details"));
    }

    @Test
    public void constructor_nullDetails_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Appointment(date, time, null));
    }

    @Test
    public void constructor_withoutDetails_rejectsNullFields() {
        assertThrows(NullPointerException.class, () -> new Appointment(null, time));
        assertThrows(NullPointerException.class, () -> new Appointment(date, null));
    }

    @Test
    public void constructor_withoutDetails_defaultsToEmptyString() {
        Appointment withoutDetails = new Appointment(date, time);
        assertEquals(date, withoutDetails.getDate());
        assertEquals(time, withoutDetails.getTime());
        assertEquals("", withoutDetails.getDetails());
        assertEquals(new Appointment(date, time, ""), withoutDetails);
    }

    @Test
    public void constructor_emptyDetails_acceptsEmptyString() {
        assertEquals("", new Appointment(date, time, "").getDetails());
    }

    @Test
    public void constructor_freeTextDetails_preservesText() {
        String details = "  Call +65 1234 5678 on arrival.\nUse entrance B; bring supplies!  ";
        assertEquals(details, new Appointment(date, time, details).getDetails());
    }

    @Test
    public void getters_returnCorrectValues() {
        assertEquals(date, appointment.getDate());
        assertEquals(time, appointment.getTime());
        assertEquals("First visit; call on arrival.", appointment.getDetails());
    }

    @Test
    public void equals() {
        Appointment copy = new Appointment(new AppointmentDate("08-10-2026"),
                new AppointmentTime("09:05"), "First visit; call on arrival.");
        assertTrue(appointment.equals(appointment));
        assertTrue(appointment.equals(copy));
        assertTrue(copy.equals(appointment));
        assertFalse(appointment.equals(null));
        assertFalse(appointment.equals("First visit; call on arrival."));
        assertFalse(appointment.equals(new Appointment(new AppointmentDate("09-10-2026"),
                time, appointment.getDetails())));
        assertFalse(appointment.equals(new Appointment(date, new AppointmentTime("10:05"),
                appointment.getDetails())));
        assertFalse(appointment.equals(new Appointment(date, time, "Follow-up visit")));
    }

    @Test
    public void hashCode_equalAppointments_returnsSameHashCode() {
        Appointment copy = new Appointment(new AppointmentDate("08-10-2026"),
                new AppointmentTime("09:05"), appointment.getDetails());
        assertEquals(appointment.hashCode(), copy.hashCode());
        Set<Appointment> appointments = new HashSet<>();
        appointments.add(appointment);
        assertTrue(appointments.contains(copy));
    }

    @Test
    public void toString_includesDateTimeAndDetails() {
        String expected = Appointment.class.getCanonicalName()
                + "{date=08-10-2026, time=09:05, details=First visit; call on arrival.}";
        assertEquals(expected, appointment.toString());
    }

    @Test
    public void toString_withoutDetails_includesDateAndTime() {
        String expected = Appointment.class.getCanonicalName()
                + "{date=08-10-2026, time=09:05, details=}";
        assertEquals(expected, new Appointment(date, time).toString());
    }
}
