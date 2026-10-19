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
import seedu.address.model.appointment.exceptions.AppointmentNotFoundException;
import seedu.address.model.appointment.exceptions.DuplicateAppointmentException;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;
import seedu.address.model.pet.exceptions.DuplicatePetException;
import seedu.address.model.pet.exceptions.PetNotFoundException;
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
    public void copyWithAppointment_addsAppointmentsWithoutChangingOriginal() {
        Appointment differentTime = new Appointment(appointment.getDate(), new AppointmentTime("10:05"));
        Appointment differentDate = new Appointment(new AppointmentDate("09-10-2026"), appointment.getTime());
        Client updated = ALICE.copyWithAppointment(appointment).copyWithAppointment(differentTime)
                .copyWithAppointment(differentDate);
        assertTrue(ALICE.getAppointments().isEmpty());
        assertEquals(List.of(appointment, differentTime, differentDate), updated.getAppointments());
        assertTrue(ALICE.isSameClient(updated));
        assertEquals(ALICE.getEmail(), updated.getEmail());
        assertEquals(ALICE.getAddress(), updated.getAddress());
        assertEquals(ALICE.getTags(), updated.getTags());
    }

    @Test
    public void copyWithAppointment_duplicateDateTime_throwsWithoutChangingClient() {
        Client client = ALICE.copyWithAppointment(appointment);
        Appointment differentDetails = new Appointment(appointment.getDate(), appointment.getTime(), "Other visit");
        assertThrows(DuplicateAppointmentException.class, () -> client.copyWithAppointment(appointment));
        assertThrows(DuplicateAppointmentException.class, () -> client.copyWithAppointment(differentDetails));
        assertEquals(List.of(appointment), client.getAppointments());
    }

    @Test
    public void copyWithAppointment_sameDateTimeForDifferentClients_succeeds() {
        assertEquals(List.of(appointment), ALICE.copyWithAppointment(appointment).getAppointments());
        assertEquals(List.of(appointment), BOB.copyWithAppointment(appointment).getAppointments());
    }

    @Test
    public void appointmentOperations_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ALICE.copyWithAppointment(null));
        assertThrows(NullPointerException.class, () -> ALICE.hasAppointment(null));
    }

    @Test
    public void hasAppointment_comparesDateTimeIgnoringDetails() {
        assertFalse(ALICE.hasAppointment(appointment));
        Client client = ALICE.copyWithAppointment(appointment);
        assertTrue(client.hasAppointment(new Appointment(appointment.getDate(), appointment.getTime(), "Changed")));
        assertFalse(client.hasAppointment(new Appointment(appointment.getDate(), new AppointmentTime("10:05"))));
        assertFalse(client.hasAppointment(new Appointment(new AppointmentDate("09-10-2026"), appointment.getTime())));
    }

    @Test
    public void equalityAndBuilder_includeAppointments() {
        Client client = ALICE.copyWithAppointment(appointment);
        Client copy = new ClientBuilder(client).build();
        assertEquals(client, copy);
        assertEquals(client.hashCode(), copy.hashCode());
        assertEquals(client, new ClientBuilder(ALICE).withAppointments(appointment).build());
        assertFalse(ALICE.equals(client));
        assertFalse(client.equals(ALICE.copyWithAppointment(
                new Appointment(appointment.getDate(), appointment.getTime(), "Changed"))));
    }

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Client client = new ClientBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> client.getTags().remove(0));
    }

    @Test
    public void withPet_addsPetWithoutChangingOriginal() {
        Client original = new ClientBuilder().build();
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));

        Client updated = original.withPet(milo);

        assertTrue(original.getPets().isEmpty());
        assertEquals(List.of(milo), updated.getPets());
    }

    @Test
    public void getPets_modifyList_throwsUnsupportedOperationException() {
        Client client = new ClientBuilder().build().withPet(
                new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle")));

        assertThrows(UnsupportedOperationException.class, () -> client.getPets().clear());
    }

    @Test
    public void withPet_duplicateNameForSameClient_throwsDuplicatePetException() {
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
        Pet sameName = new Pet(new PetName("mILO"), new Species("Cat"), new Breed("Persian"));
        Client client = new ClientBuilder().build().withPet(milo);

        assertThrows(DuplicatePetException.class, () -> client.withPet(sameName));
    }

    @Test
    public void withPet_sameNameForDifferentClients_succeeds() {
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));

        assertEquals(List.of(milo), new ClientBuilder(ALICE).build().withPet(milo).getPets());
        assertEquals(List.of(milo), new ClientBuilder(BOB).build().withPet(milo).getPets());
    }

    @Test
    public void withPet_addsMultipleDistinctPets() {
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
        Pet luna = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));

        Client client = new ClientBuilder(ALICE).build()
                .withPet(milo)
                .withPet(luna);

        assertEquals(List.of(milo, luna), client.getPets());
    }

    @Test
    public void constructor_duplicatePets_throwsDuplicatePetException() {
        Pet milo = new Pet(
                new PetName("Milo"),
                new Species("Dog"),
                new Breed("Poodle"));

        Pet sameName = new Pet(
                new PetName("mILO"),
                new Species("Cat"),
                new Breed("Persian"));

        assertThrows(DuplicatePetException.class, () ->
                new Client(
                        ALICE.getName(),
                        ALICE.getPhone(),
                        ALICE.getEmail(),
                        ALICE.getAddress(),
                        ALICE.getTags(),
                        List.of(milo, sameName),
                        List.of()));
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

        // name has repeated spaces, same phone -> returns true
        String nameWithRepeatedSpaces = VALID_NAME_BOB.replace(" ", "  ");
        editedBob = new ClientBuilder(BOB).withName(nameWithRepeatedSpaces).build();
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

        // different pets -> returns false
        editedAlice = ALICE.withPet(new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle")));
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void hashCode_equalClientsWithSamePets_returnsSameHashCode() {
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
        Client clientWithPet = new ClientBuilder(ALICE).build().withPet(milo);
        Client equalClientWithPet = new ClientBuilder(clientWithPet).build();

        assertEquals(clientWithPet, equalClientWithPet);
        assertEquals(clientWithPet.hashCode(), equalClientWithPet.hashCode());
    }

    @Test
    public void toStringMethod() {
        String expected = Client.class.getCanonicalName()
                + "{name=" + ALICE.getName()
                + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail()
                + ", address=" + ALICE.getAddress()
                + ", tags=" + ALICE.getTags()
                + ", pets=" + ALICE.getPets()
                + ", appointments=" + ALICE.getAppointments()
                + "}";

        assertEquals(expected, ALICE.toString());
    }

    @Test
    public void withPet_preservesAppointments() {
        Pet milo = new Pet(
                new PetName("Milo"),
                new Species("Dog"),
                new Breed("Poodle"));

        Client original = ALICE.copyWithAppointment(appointment);
        Client updated = original.withPet(milo);

        assertEquals(List.of(milo), updated.getPets());
        assertEquals(List.of(appointment), updated.getAppointments());
        assertTrue(original.getPets().isEmpty());
    }

    @Test
    public void copyWithAppointment_preservesPets() {
        Pet milo = new Pet(
                new PetName("Milo"),
                new Species("Dog"),
                new Breed("Poodle"));

        Client original = ALICE.withPet(milo);
        Client updated = original.copyWithAppointment(appointment);

        assertEquals(List.of(milo), updated.getPets());
        assertEquals(List.of(appointment), updated.getAppointments());
        assertTrue(original.getAppointments().isEmpty());
    }

    @Test
    public void builder_copyPreservesPetsAndAppointments() {
        Pet milo = new Pet(
                new PetName("Milo"),
                new Species("Dog"),
                new Breed("Poodle"));

        Client original = ALICE.withPet(milo)
                .copyWithAppointment(appointment);

        Client copied = new ClientBuilder(original).build();

        assertEquals(original, copied);
        assertEquals(original.hashCode(), copied.hashCode());
        assertEquals(List.of(milo), copied.getPets());
        assertEquals(List.of(appointment), copied.getAppointments());
    }
    @Test
    public void getPet_matchesNameIgnoringCase() {
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
        Client client = new ClientBuilder().build().withPet(milo);

        assertEquals(milo, client.getPet(new PetName("mILO")).get());
        assertTrue(client.getPet(new PetName("Luna")).isEmpty());
    }

    @Test
    public void withPetReplaced_replacesInPlaceWithoutChangingOriginal() {
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
        Pet luna = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));
        Pet editedMilo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Toy Poodle"));
        Client original = new ClientBuilder().build().withPet(milo).withPet(luna);

        Client updated = original.withPetReplaced(new PetName("milo"), editedMilo);

        assertEquals(List.of(editedMilo, luna), updated.getPets());
        assertEquals(List.of(milo, luna), original.getPets());
    }

    @Test
    public void withPetReplaced_renameToOtherPetsName_throwsDuplicatePetException() {
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
        Pet luna = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));
        Pet renamedToLuna = new Pet(new PetName("LUNA"), new Species("Dog"), new Breed("Poodle"));
        Client client = new ClientBuilder().build().withPet(milo).withPet(luna);

        assertThrows(DuplicatePetException.class, () -> client.withPetReplaced(new PetName("Milo"), renamedToLuna));
    }

    @Test
    public void withPetReplaced_missingPet_throwsPetNotFoundException() {
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
        Client client = new ClientBuilder().build();

        assertThrows(PetNotFoundException.class, () -> client.withPetReplaced(new PetName("Milo"), milo));
    }

    @Test
    public void withoutPet_removesOnlyThatPet() {
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
        Pet luna = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));
        Client original = new ClientBuilder().build().withPet(milo).withPet(luna);

        assertEquals(List.of(luna), original.withoutPet(new PetName("MILO")).getPets());
        assertEquals(List.of(milo, luna), original.getPets());
        assertThrows(PetNotFoundException.class, () -> original.withoutPet(new PetName("Rex")));
    }

    @Test
    public void getAppointment_matchesDateAndTime() {
        Client client = new ClientBuilder().build().copyWithAppointment(appointment);

        assertEquals(appointment, client.getAppointment(appointment.getDate(), appointment.getTime()).get());
        assertTrue(client.getAppointment(appointment.getDate(), new AppointmentTime("10:00")).isEmpty());
    }

    @Test
    public void withAppointmentReplaced_movesAppointmentWithoutChangingOriginal() {
        Client original = new ClientBuilder().build().copyWithAppointment(appointment);
        Appointment moved = new Appointment(appointment.getDate(), new AppointmentTime("16:00"),
                appointment.getDetails());

        Client updated = original.withAppointmentReplaced(appointment.getDate(), appointment.getTime(), moved);

        assertEquals(List.of(moved), updated.getAppointments());
        assertEquals(List.of(appointment), original.getAppointments());
    }

    @Test
    public void withAppointmentReplaced_intoOccupiedSlot_throwsDuplicateAppointmentException() {
        Appointment other = new Appointment(appointment.getDate(), new AppointmentTime("16:00"));
        Client client = new ClientBuilder().build().copyWithAppointment(appointment).copyWithAppointment(other);
        Appointment movedOntoOther = new Appointment(other.getDate(), other.getTime(), "Moved");

        assertThrows(DuplicateAppointmentException.class, () ->
                client.withAppointmentReplaced(appointment.getDate(), appointment.getTime(), movedOntoOther));
    }

    @Test
    public void withAppointmentReplaced_missingAppointment_throwsAppointmentNotFoundException() {
        Client client = new ClientBuilder().build();

        assertThrows(AppointmentNotFoundException.class, () ->
                client.withAppointmentReplaced(appointment.getDate(), appointment.getTime(), appointment));
    }

    @Test
    public void withoutAppointment_removesOnlyThatAppointment() {
        Appointment other = new Appointment(appointment.getDate(), new AppointmentTime("16:00"));
        Client original = new ClientBuilder().build().copyWithAppointment(appointment).copyWithAppointment(other);

        Client updated = original.withoutAppointment(appointment.getDate(), appointment.getTime());

        assertEquals(List.of(other), updated.getAppointments());
        assertEquals(List.of(appointment, other), original.getAppointments());
        assertThrows(AppointmentNotFoundException.class, () ->
                updated.withoutAppointment(appointment.getDate(), appointment.getTime()));
    }
}
