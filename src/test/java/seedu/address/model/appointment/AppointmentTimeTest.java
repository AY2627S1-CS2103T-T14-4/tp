package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

public class AppointmentTimeTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AppointmentTime(null));
    }

    @Test
    public void constructor_invalidTime_throwsIllegalArgumentException() {
        String[] invalidTimes = {"", "24:00", "12:60", "9:05", "09:05:00"};
        for (String time : invalidTimes) {
            assertThrows(IllegalArgumentException.class,
                    AppointmentTime.MESSAGE_CONSTRAINTS, () -> new AppointmentTime(time));
        }
    }

    @Test
    public void isValidTime_null_returnsFalse() {
        assertFalse(AppointmentTime.isValidTime(null));
    }

    @Test
    public void isValidTime_validTimes_returnsTrue() {
        String[] validTimes = {"00:00", "00:01", "09:05", "12:00", "13:30", "23:59"};
        for (String time : validTimes) {
            assertTrue(AppointmentTime.isValidTime(time), time);
            assertEquals(time, new AppointmentTime(time).toString());
        }
    }

    @Test
    public void isValidTime_outOfRange_returnsFalse() {
        String[] invalidTimes = {"24:00", "24:01", "25:00", "99:00", "00:60", "12:60", "23:99"};
        for (String time : invalidTimes) {
            assertFalse(AppointmentTime.isValidTime(time), time);
            assertThrows(IllegalArgumentException.class, () -> new AppointmentTime(time));
        }
    }

    @Test
    public void isValidTime_incorrectFormat_returnsFalse() {
        String[] invalidTimes = {"", " ", "9:05", "09:5", "9:5", "009:05", "09:005", "0905",
            "09-05", "09.05", "09:05:00", "09:05.000", "09:05 AM", "9:05 PM", "09:05Z",
            " 09:05", "09:05 ", "09:05\n", "09:05extra", "-1:00", "00:-1", "aa:bb"};
        for (String time : invalidTimes) {
            assertFalse(AppointmentTime.isValidTime(time), time);
            assertThrows(IllegalArgumentException.class, () -> new AppointmentTime(time));
        }
    }

    @Test
    public void getValue_returnsParsedTime() {
        assertEquals(LocalTime.of(9, 5), new AppointmentTime("09:05").getValue());
    }

    @Test
    public void toString_returnsRequiredFormat() {
        assertEquals("09:05", new AppointmentTime("09:05").toString());
        assertEquals("00:00", new AppointmentTime("00:00").toString());
    }

    @Test
    public void equals() {
        AppointmentTime time = new AppointmentTime("09:05");
        assertTrue(time.equals(time));
        assertTrue(time.equals(new AppointmentTime("09:05")));
        assertFalse(time.equals(null));
        assertFalse(time.equals("09:05"));
        assertFalse(time.equals(new AppointmentTime("10:05")));
        assertFalse(time.equals(new AppointmentTime("09:06")));
    }

    @Test
    public void hashCode_equalTimes_returnsSameHashCode() {
        assertEquals(new AppointmentTime("09:05").hashCode(),
                new AppointmentTime("09:05").hashCode());
    }
}
