package seedu.address.model.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BOB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.appointment.exceptions.DuplicateAppointmentException;
import seedu.address.testutil.ClientBuilder;

public class ClientTest {

    private final Appointment appointment = new Appointment(new AppointmentDate("08-10-2026"),
            new AppointmentTime("09:05"), "First visit");

    @Test
    public void constructor_withoutAppointments_defaultsToEmptyList() {
        assertTrue(ALICE.getAppointments().isEmpty());
    }

    @Test
    public void constructor_appointments_defensivelyCopiesList() {
        List<Appointment> appointments = new ArrayList<>(List.of(appointment));
        Client client = new Client(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(),
                ALICE.getTags(), appointments);
        appointments.clear();
        assertEquals(List.of(appointment), client.getAppointments());
        assertThrows(UnsupportedOperationException.class, () -> client.getAppointments().clear());
    }

    @Test
    public void constructor_nullAppointments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Client(ALICE.getName(), ALICE.getPhone(),
                ALICE.getEmail(), ALICE.getAddress(), ALICE.getTags(), null));
        assertThrows(NullPointerException.class, () -> new Client(ALICE.getName(), ALICE.getPhone(),
                ALICE.getEmail(), ALICE.getAddress(), ALICE.getTags(), Arrays.asList(appointment, null)));
    }

    @Test
    public void constructor_duplicateDateTime_throwsDuplicateAppointmentException() {
        Appointment differentDetails = new Appointment(appointment.getDate(), appointment.getTime(), "Other visit");
        assertThrows(DuplicateAppointmentException.class, () -> new Client(ALICE.getName(), ALICE.getPhone(),
                ALICE.getEmail(), ALICE.getAddress(), ALICE.getTags(), List.of(appointment, differentDetails)));
    }

    @Test
    public void withAppointment_addsAppointmentsWithoutChangingOriginal() {
        Appointment differentTime = new Appointment(appointment.getDate(), new AppointmentTime("10:05"));
        Appointment differentDate = new Appointment(new AppointmentDate("09-10-2026"), appointment.getTime());
        Client updated = ALICE.withAppointment(appointment).withAppointment(differentTime)
                .withAppointment(differentDate);
        assertTrue(ALICE.getAppointments().isEmpty());
        assertEquals(List.of(appointment, differentTime, differentDate), updated.getAppointments());
        assertTrue(ALICE.isSameClient(updated));
        assertEquals(ALICE.getEmail(), updated.getEmail());
        assertEquals(ALICE.getAddress(), updated.getAddress());
        assertEquals(ALICE.getTags(), updated.getTags());
    }

    @Test
    public void withAppointment_duplicateDateTime_throwsWithoutChangingClient() {
        Client client = ALICE.withAppointment(appointment);
        Appointment differentDetails = new Appointment(appointment.getDate(), appointment.getTime(), "Other visit");
        assertThrows(DuplicateAppointmentException.class, () -> client.withAppointment(appointment));
        assertThrows(DuplicateAppointmentException.class, () -> client.withAppointment(differentDetails));
        assertEquals(List.of(appointment), client.getAppointments());
    }

    @Test
    public void withAppointment_sameDateTimeForDifferentClients_succeeds() {
        assertEquals(List.of(appointment), ALICE.withAppointment(appointment).getAppointments());
        assertEquals(List.of(appointment), BOB.withAppointment(appointment).getAppointments());
    }

    @Test
    public void appointmentOperations_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ALICE.withAppointment(null));
        assertThrows(NullPointerException.class, () -> ALICE.hasAppointment(null));
    }

    @Test
    public void hasAppointment_comparesDateTimeIgnoringDetails() {
        assertFalse(ALICE.hasAppointment(appointment));
        Client client = ALICE.withAppointment(appointment);
        assertTrue(client.hasAppointment(new Appointment(appointment.getDate(), appointment.getTime(), "Changed")));
        assertFalse(client.hasAppointment(new Appointment(appointment.getDate(), new AppointmentTime("10:05"))));
        assertFalse(client.hasAppointment(new Appointment(new AppointmentDate("09-10-2026"), appointment.getTime())));
    }

    @Test
    public void equalityAndBuilder_includeAppointments() {
        Client client = ALICE.withAppointment(appointment);
        Client copy = new ClientBuilder(client).build();
        assertEquals(client, copy);
        assertEquals(client.hashCode(), copy.hashCode());
        assertEquals(client, new ClientBuilder(ALICE).withAppointments(appointment).build());
        assertFalse(ALICE.equals(client));
        assertFalse(client.equals(ALICE.withAppointment(
                new Appointment(appointment.getDate(), appointment.getTime(), "Changed"))));
    }

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Client client = new ClientBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> client.getTags().remove(0));
    }

    @Test
    public void isSameClient() {
        // same object -> returns true
        assertTrue(ALICE.isSameClient(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSameClient(null));

        // same name, different phone -> returns false
        Client editedAlice = new ClientBuilder(ALICE).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.isSameClient(editedAlice));

        editedAlice = new ClientBuilder(ALICE).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSameClient(editedAlice));

        // different name, all other attributes same -> returns false
        editedAlice = new ClientBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSameClient(editedAlice));

        // name differs in case, same phone -> returns true
        Client editedBob = new ClientBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertTrue(BOB.isSameClient(editedBob));

        // name has repeated/trailing spaces, same phone -> returns true
        String nameWithTrailingSpaces = VALID_NAME_BOB.replace(" ", "  ") + " ";
        editedBob = new ClientBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertTrue(BOB.isSameClient(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Client aliceCopy = new ClientBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different client -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Client editedAlice = new ClientBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new ClientBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new ClientBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new ClientBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new ClientBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void toStringMethod() {
        String expected = Client.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", address=" + ALICE.getAddress() + ", tags=" + ALICE.getTags()
                + ", appointments=" + ALICE.getAppointments() + "}";
        assertEquals(expected, ALICE.toString());
    }
}
