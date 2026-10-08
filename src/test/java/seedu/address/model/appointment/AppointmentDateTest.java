package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class AppointmentDateTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AppointmentDate(null));
    }

    @Test
    public void constructor_invalidDate_throwsIllegalArgumentException() {
        String[] invalidDates = {"", "31-04-2026", "29-02-2026", "8-10-2026", "01-01-0000"};
        for (String date : invalidDates) {
            assertThrows(IllegalArgumentException.class,
                    AppointmentDate.MESSAGE_CONSTRAINTS, () -> new AppointmentDate(date));
        }
    }

    @Test
    public void isValidDate_null_returnsFalse() {
        assertFalse(AppointmentDate.isValidDate(null));
    }

    @Test
    public void isValidDate_validCalendarDates_returnsTrue() {
        String[] validDates = {"08-10-2026", "30-04-2026", "31-12-2026", "28-02-2026",
            "01-01-0001", "31-12-9999", "01-01-2000"};
        for (String date : validDates) {
            assertTrue(AppointmentDate.isValidDate(date), date);
            assertEquals(date, new AppointmentDate(date).toString());
        }
    }

    @Test
    public void isValidDate_invalidCalendarDates_returnsFalse() {
        String[] invalidDates = {"00-10-2026", "32-10-2026", "31-04-2026", "31-06-2026",
            "30-02-2026", "01-00-2026", "01-13-2026", "01-01-0000"};
        for (String date : invalidDates) {
            assertFalse(AppointmentDate.isValidDate(date), date);
            assertThrows(IllegalArgumentException.class, () -> new AppointmentDate(date));
        }
    }

    @Test
    public void isValidDate_leapYears_checksCenturyRule() {
        assertTrue(AppointmentDate.isValidDate("29-02-2028"));
        assertTrue(AppointmentDate.isValidDate("29-02-2000"));
        assertFalse(AppointmentDate.isValidDate("29-02-2026"));
        assertFalse(AppointmentDate.isValidDate("29-02-1900"));
        assertFalse(AppointmentDate.isValidDate("29-02-2100"));
    }

    @Test
    public void isValidDate_incorrectFormat_returnsFalse() {
        String[] invalidDates = {"", " ", "8-10-2026", "08-1-2026", "08-10-26", "2026-10-08",
            "08/10/2026", "08-10-2026extra", " 08-10-2026", "08-10-2026 ", "08-10-2026\n",
            "08-10-2026 09:00", "01-01-10000", "01-01--001", "aa-bb-cccc"};
        for (String date : invalidDates) {
            assertFalse(AppointmentDate.isValidDate(date), date);
            assertThrows(IllegalArgumentException.class, () -> new AppointmentDate(date));
        }
    }

    @Test
    public void getValue_returnsParsedDate() {
        assertEquals(LocalDate.of(2026, 10, 8), new AppointmentDate("08-10-2026").getValue());
    }

    @Test
    public void toString_returnsRequiredFormat() {
        assertEquals("08-01-2026", new AppointmentDate("08-01-2026").toString());
    }

    @Test
    public void equals() {
        AppointmentDate date = new AppointmentDate("08-10-2026");
        assertTrue(date.equals(date));
        assertTrue(date.equals(new AppointmentDate("08-10-2026")));
        assertFalse(date.equals(null));
        assertFalse(date.equals("08-10-2026"));
        assertFalse(date.equals(new AppointmentDate("09-10-2026")));
        assertFalse(date.equals(new AppointmentDate("08-11-2026")));
        assertFalse(date.equals(new AppointmentDate("08-10-2027")));
    }

    @Test
    public void hashCode_equalDates_returnsSameHashCode() {
        assertEquals(new AppointmentDate("08-10-2026").hashCode(),
                new AppointmentDate("08-10-2026").hashCode());
    }
}
