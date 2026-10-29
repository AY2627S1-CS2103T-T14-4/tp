package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedClient.MESSAGE_MISSING_SESSION;
import static seedu.address.storage.JsonAdaptedClient.MESSAGE_UNKNOWN_LINKED_PET;
import static seedu.address.storage.JsonAdaptedClient.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.BENSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Address;
import seedu.address.model.client.Email;
import seedu.address.model.client.Name;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;

public class JsonAdaptedClientTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "12-34";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());
    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));

    @Test
    public void toModelType_validClientDetails_returnsClient() throws Exception {
        JsonAdaptedClient client = new JsonAdaptedClient(BENSON);
        assertEquals(BENSON, client.toModelType());
    }

    @Test
    public void toModelType_appointmentsBelongToClient() throws Exception {
        Appointment appointment = new Appointment(new AppointmentDate("01-01-2025"),
                new AppointmentTime("09:05"), "Grooming");
        JsonAdaptedClient client = new JsonAdaptedClient(BENSON.copyWithAppointment(appointment));
        assertEquals(BENSON.copyWithAppointment(appointment), client.toModelType());
    }

    @Test
    public void toModelType_petsBelongToClient() throws Exception {
        JsonAdaptedClient client = new JsonAdaptedClient(BENSON.withPet(MILO));
        assertEquals(BENSON.withPet(MILO), client.toModelType());
    }

    @Test
    public void toModelType_missingPets_defaultsToEmpty() throws Exception {
        JsonAdaptedClient client = new JsonAdaptedClient(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_ADDRESS, VALID_TAGS, List.of());
        assertEquals(BENSON, client.toModelType());
    }

    @Test
    public void toModelType_nullTagEntry_throwsIllegalValueException() {
        List<JsonAdaptedTag> tags = new ArrayList<>(VALID_TAGS);
        tags.add(null);
        JsonAdaptedClient client = new JsonAdaptedClient(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_ADDRESS, tags, List.of(), List.of());
        assertThrows(IllegalValueException.class, "Tag entry is missing!", client::toModelType);
    }

    @Test
    public void toModelType_nullPetEntry_throwsIllegalValueException() {
        List<JsonAdaptedPet> pets = new ArrayList<>();
        pets.add(null);
        JsonAdaptedClient client = new JsonAdaptedClient(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_ADDRESS, VALID_TAGS, pets, List.of());
        assertThrows(IllegalValueException.class, "Pet entry is missing!", client::toModelType);
    }

    @Test
    public void toModelType_duplicatePetNames_throwsIllegalValueException() {
        List<JsonAdaptedPet> pets = List.of(
                new JsonAdaptedPet("Milo", "Dog", "Poodle"),
                new JsonAdaptedPet("mILO", "Cat", "Persian"));
        JsonAdaptedClient client = new JsonAdaptedClient(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_ADDRESS, VALID_TAGS, pets, List.of());
        assertThrows(IllegalValueException.class, "Pets list contains duplicate pet names.", client::toModelType);
    }

    @Test
    public void toModelType_nullHistoryEntry_throwsIllegalValueException() {
        List<JsonAdaptedCompletedSession> history = new ArrayList<>();
        history.add(null);
        JsonAdaptedClient client = new JsonAdaptedClient(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_TAGS, List.of(), List.of(), history);
        assertThrows(IllegalValueException.class, MESSAGE_MISSING_SESSION, client::toModelType);
    }

    @Test
    public void toModelType_appointmentLinkedToUnknownPet_throwsIllegalValueException() {
        JsonAdaptedClient client = new JsonAdaptedClient(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_TAGS, List.of(new JsonAdaptedPet("Milo", "Dog", "Poodle")),
                List.of(new JsonAdaptedAppointment("01-01-2025", "09:05", "", List.of("Ghost"))));
        assertThrows(IllegalValueException.class, String.format(MESSAGE_UNKNOWN_LINKED_PET, "Ghost"),
                client::toModelType);
    }

    @Test
    public void toModelType_appointmentLinksInOtherCase_useRecordedPetNamesOnce() throws Exception {
        JsonAdaptedClient client = new JsonAdaptedClient(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_TAGS, List.of(new JsonAdaptedPet("Sir Fluffy", "Cat", "Persian")),
                List.of(new JsonAdaptedAppointment("01-01-2025", "09:05", "",
                        List.of("sir fluffy", "SIR   FLUFFY"))));
        Appointment appointment = client.toModelType().getAppointments().get(0);
        assertEquals(Set.of(new PetName("Sir Fluffy")), appointment.getPetNames());
        assertEquals("Sir Fluffy", appointment.getPetNames().iterator().next().fullName);
    }

    @Test
    public void toModelType_duplicateAppointmentSlots_throwsIllegalValueException() {
        List<JsonAdaptedAppointment> appointments = List.of(
                new JsonAdaptedAppointment("01-01-2025", "09:05", "First"),
                new JsonAdaptedAppointment("01-01-2025", "09:05", "Different details"));
        JsonAdaptedClient client = new JsonAdaptedClient(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_ADDRESS, VALID_TAGS, appointments);
        assertThrows(IllegalValueException.class, "Appointments list contains duplicate date/time slots.",
                client::toModelType);
    }

    @Test
    public void toModelType_nullAppointmentEntry_throwsIllegalValueException() {
        List<JsonAdaptedAppointment> appointments = new ArrayList<>();
        appointments.add(null);
        JsonAdaptedClient client = new JsonAdaptedClient(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_ADDRESS, VALID_TAGS, appointments);
        assertThrows(IllegalValueException.class, "Appointment entry is missing!", client::toModelType);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedClient client =
                new JsonAdaptedClient(INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, client::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedClient client = new JsonAdaptedClient(null, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, client::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedClient client =
                new JsonAdaptedClient(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, client::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedClient client = new JsonAdaptedClient(VALID_NAME, null, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, client::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedClient client =
                new JsonAdaptedClient(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, client::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedClient client = new JsonAdaptedClient(VALID_NAME, VALID_PHONE, null, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, client::toModelType);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedClient client =
                new JsonAdaptedClient(VALID_NAME, VALID_PHONE, VALID_EMAIL, INVALID_ADDRESS, VALID_TAGS);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, client::toModelType);
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        JsonAdaptedClient client = new JsonAdaptedClient(VALID_NAME, VALID_PHONE, VALID_EMAIL, null, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, client::toModelType);
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedClient client =
                new JsonAdaptedClient(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, invalidTags);
        assertThrows(IllegalValueException.class, client::toModelType);
    }

}
