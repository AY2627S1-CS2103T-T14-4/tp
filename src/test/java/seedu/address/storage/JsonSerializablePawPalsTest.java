package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.PawPals;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.TypicalClients;

public class JsonSerializablePawPalsTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializablePawPalsTest");
    private static final Path TYPICAL_CLIENTS_FILE = TEST_DATA_FOLDER.resolve("typicalClientsPawPals.json");
    private static final Path INVALID_CLIENT_FILE = TEST_DATA_FOLDER.resolve("invalidClientPawPals.json");
    private static final Path DUPLICATE_CLIENT_FILE = TEST_DATA_FOLDER.resolve("duplicateClientPawPals.json");
    private static final Path V1_3_FORMAT_FILE = TEST_DATA_FOLDER.resolve("v1.3FormatPawPals.json");

    @Test
    public void toModelType_typicalClientsFile_success() throws Exception {
        JsonSerializablePawPals dataFromFile = JsonUtil.readJsonFile(TYPICAL_CLIENTS_FILE,
                JsonSerializablePawPals.class).get();
        PawPals pawPalsFromFile = dataFromFile.toModelType();
        PawPals typicalClientsPawPals = TypicalClients.getTypicalPawPals();
        assertEquals(pawPalsFromFile, typicalClientsPawPals);
        assertTrue(pawPalsFromFile.getClientList().stream()
                .allMatch(client -> client.getAppointments().isEmpty()));
        assertTrue(pawPalsFromFile.getClientList().stream()
                .allMatch(client -> client.getPets().isEmpty()));
    }

    @Test
    public void toModelType_invalidClientFile_throwsIllegalValueException() throws Exception {
        JsonSerializablePawPals dataFromFile = JsonUtil.readJsonFile(INVALID_CLIENT_FILE,
                JsonSerializablePawPals.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicateClients_throwsIllegalValueException() throws Exception {
        JsonSerializablePawPals dataFromFile = JsonUtil.readJsonFile(DUPLICATE_CLIENT_FILE,
                JsonSerializablePawPals.class).get();
        assertThrows(IllegalValueException.class, JsonSerializablePawPals.MESSAGE_DUPLICATE_CLIENT,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_v1Point3FormatFile_loadsWithoutNewFields() throws Exception {
        JsonSerializablePawPals dataFromFile = JsonUtil.readJsonFile(V1_3_FORMAT_FILE,
                JsonSerializablePawPals.class).get();
        PawPals pawPalsFromFile = dataFromFile.toModelType();

        // Pets and appointments built with the v1.3 constructors have no notes, end time or linked pets,
        // so this also checks that the missing fields load as empty.
        assertEquals(getV1Point3FormatPawPals(), pawPalsFromFile);
        assertTrue(pawPalsFromFile.getClientList().stream()
                .flatMap(client -> client.getAppointments().stream())
                .allMatch(appointment -> appointment.getPetNames().isEmpty()));
    }

    @Test
    public void toModelType_v1Point3FormatFileSavedAgain_loadsUnchanged() throws Exception {
        PawPals pawPalsFromFile = JsonUtil.readJsonFile(V1_3_FORMAT_FILE, JsonSerializablePawPals.class)
                .get().toModelType();

        PawPals reloaded = new JsonSerializablePawPals(pawPalsFromFile).toModelType();

        assertEquals(pawPalsFromFile, reloaded);
    }

    /**
     * Returns the data stored in {@code v1.3FormatPawPals.json}.
     */
    private static PawPals getV1Point3FormatPawPals() {
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Toy Poodle"));
        Pet luna = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));
        Appointment groom = new Appointment(new AppointmentDate("12-12-2026"), new AppointmentTime("14:00"),
                "Full groom for Milo");
        Appointment checkIn = new Appointment(new AppointmentDate("13-12-2026"), new AppointmentTime("09:30"));
        Client alice = new ClientBuilder().withName("Alice Tan").withPhone("91234567")
                .withEmail("alice@example.com").withAddress("Blk 123 Clementi Ave 3, #04-56")
                .withTags("regular").withPets(milo, luna).withAppointments(groom, checkIn).build();
        Client ben = new ClientBuilder().withName("Ben Lim").withPhone("98765432")
                .withEmail("ben@example.com").withAddress("10 Tampines Street 11, #02-01").build();

        PawPals pawPals = new PawPals();
        pawPals.addClient(alice);
        pawPals.addClient(ben);
        return pawPals;
    }
}
