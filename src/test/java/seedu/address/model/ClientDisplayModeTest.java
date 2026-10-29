package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.appointment.AppointmentDate;

public class ClientDisplayModeTest {
    private static final AppointmentDate TARGET_DATE = new AppointmentDate("29-10-2026");

    @Test
    public void equalsAndHashCode_sameMode_equal() {
        ClientDisplayMode dateMode = ClientDisplayMode.scheduleDate(TARGET_DATE);
        ClientDisplayMode sameDateMode = ClientDisplayMode.scheduleDate(new AppointmentDate("29-10-2026"));

        assertEquals(dateMode, dateMode);
        assertEquals(dateMode, sameDateMode);
        assertEquals(dateMode.hashCode(), sameDateMode.hashCode());
        assertEquals(ClientDisplayMode.NORMAL.hashCode(), ClientDisplayMode.NORMAL.hashCode());
    }

    @Test
    public void equals_differentModeDateOrType_notEqual() {
        ClientDisplayMode dateMode = ClientDisplayMode.scheduleDate(TARGET_DATE);

        assertNotEquals(dateMode, ClientDisplayMode.NORMAL);
        assertNotEquals(dateMode, ClientDisplayMode.scheduleDate(new AppointmentDate("30-10-2026")));
        assertNotEquals(dateMode, null);
        assertNotEquals(dateMode, "schedule");
    }
}
