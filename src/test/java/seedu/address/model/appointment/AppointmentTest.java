package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.pet.PetName;

public class AppointmentTest {

    private final AppointmentDate date = new AppointmentDate("08-10-2026");
    private final AppointmentTime time = new AppointmentTime("09:05");
    private final Appointment appointment = new Appointment(date, time, "First visit; call on arrival.");

    @Test
    public void hasSameDateTime_comparesDateAndTimeOnly() {
        Appointment original = new Appointment(new AppointmentDate("08-10-2026"),
                new AppointmentTime("09:05"), "First visit");
        assertTrue(original.hasSameDateTime(original));
        assertTrue(original.hasSameDateTime(new Appointment(original.getDate(), original.getTime(), "Changed")));
        assertFalse(original.hasSameDateTime(null));
        assertFalse(original.hasSameDateTime(new Appointment(new AppointmentDate("09-10-2026"),
                original.getTime())));
        assertFalse(original.hasSameDateTime(new Appointment(original.getDate(), new AppointmentTime("10:05"))));
    }

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
                + "{date=08-10-2026, time=09:05, details=First visit; call on arrival., pets=[]}";
        assertEquals(expected, appointment.toString());
    }

    @Test
    public void toString_withoutDetails_includesDateAndTime() {
        String expected = Appointment.class.getCanonicalName()
                + "{date=08-10-2026, time=09:05, details=, pets=[]}";
        assertEquals(expected, new Appointment(date, time).toString());
    }
    @Test
    public void constructor_withoutPets_hasNoLinkedPets() {
        assertTrue(appointment.getPetNames().isEmpty());
        assertTrue(new Appointment(date, time).getPetNames().isEmpty());
    }

    @Test
    public void constructor_nullPetNames_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Appointment(date, time, "", null));
    }

    @Test
    public void getPetNames_keepsGivenOrder() {
        Set<PetName> petNames = new LinkedHashSet<>(List.of(new PetName("Milo"), new PetName("Luna")));
        Appointment linked = new Appointment(date, time, "", petNames);

        assertEquals(List.of(new PetName("Milo"), new PetName("Luna")), List.copyOf(linked.getPetNames()));
    }

    @Test
    public void getPetNames_modifySet_throwsUnsupportedOperationException() {
        Appointment linked = new Appointment(date, time, "", Set.of(new PetName("Milo")));
        assertThrows(UnsupportedOperationException.class, () -> linked.getPetNames().add(new PetName("Luna")));
    }

    @Test
    public void constructor_laterChangesToGivenSet_doNotAffectAppointment() {
        Set<PetName> petNames = new HashSet<>(Set.of(new PetName("Milo")));
        Appointment linked = new Appointment(date, time, "", petNames);
        petNames.add(new PetName("Luna"));

        assertEquals(Set.of(new PetName("Milo")), linked.getPetNames());
    }

    @Test
    public void withPetNames_replacesPetsAndKeepsOtherFields() {
        Appointment linked = appointment.withPetNames(Set.of(new PetName("Milo")));

        assertEquals(Set.of(new PetName("Milo")), linked.getPetNames());
        assertTrue(linked.hasSameDateTime(appointment));
        assertEquals(appointment.getDetails(), linked.getDetails());
        assertTrue(appointment.getPetNames().isEmpty());
    }

    @Test
    public void equals_differentPets_returnsFalse() {
        Appointment withMilo = appointment.withPetNames(Set.of(new PetName("Milo")));
        Appointment withLuna = appointment.withPetNames(Set.of(new PetName("Luna")));

        assertFalse(withMilo.equals(withLuna));
        assertFalse(withMilo.equals(appointment));
        assertTrue(withMilo.hasSameDateTime(withLuna));
        assertEquals(withMilo, appointment.withPetNames(Set.of(new PetName("Milo"))));
        assertEquals(withMilo.hashCode(), appointment.withPetNames(Set.of(new PetName("Milo"))).hashCode());
    }
}
