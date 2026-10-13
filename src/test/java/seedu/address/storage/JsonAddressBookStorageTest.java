package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;
import static seedu.address.testutil.TypicalClients.HOON;
import static seedu.address.testutil.TypicalClients.IDA;
import static seedu.address.testutil.TypicalClients.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidClientAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidClientAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidClientAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidClientAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addClient(HOON);
        original.removeClient(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addClient(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void readAndSaveAddressBook_appointmentsRemainWithTheirClients() throws Exception {
        Path filePath = testFolder.resolve("appointments.json");
        Appointment pastAppointment = new Appointment(new AppointmentDate("29-02-2024"),
                new AppointmentTime("09:05"), "Old visit");
        Appointment futureAppointment = new Appointment(new AppointmentDate("01-01-2028"),
                new AppointmentTime("12:30"));
        Client alice = ALICE.copyWithAppointment(pastAppointment).copyWithAppointment(futureAppointment);
        Client benson = BENSON.copyWithAppointment(pastAppointment);
        AddressBook original = new AddressBook();
        original.addClient(alice);
        original.addClient(benson);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(original);
        AddressBook restored = new AddressBook(storage.readAddressBook().get());
        assertEquals(original, restored);
        assertEquals(alice.getAppointments(), restored.getClientList().get(0).getAppointments());
        assertEquals(benson.getAppointments(), restored.getClientList().get(1).getAppointments());
    }

    @Test
    public void readAndSaveAddressBook_petsRemainWithTheirClients() throws Exception {
        Path filePath = testFolder.resolve("pets.json");
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
        Pet luna = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));
        Client alice = ALICE.withPet(milo);
        Client benson = BENSON.withPet(milo).withPet(luna);
        AddressBook original = new AddressBook();
        original.addClient(alice);
        original.addClient(benson);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(original);
        AddressBook restored = new AddressBook(storage.readAddressBook().get());

        assertEquals(original, restored);
        assertEquals(alice.getPets(), restored.getClientList().get(0).getPets());
        assertEquals(benson.getPets(), restored.getClientList().get(1).getPets());
    }

    @Test
    public void readAddressBook_invalidStoredPet_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("invalidPets.json");
        List<JsonAdaptedPet> invalidPets = List.of(new JsonAdaptedPet("Milo", null, "Poodle"));
        JsonAdaptedClient client = new JsonAdaptedClient(ALICE.getName().toString(),
                ALICE.getPhone().toString(), ALICE.getEmail().toString(), ALICE.getAddress().toString(),
                List.of(), invalidPets, List.of());
        JsonUtil.saveJsonFile(new JsonSerializableAddressBook(List.of(client)), filePath);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
    }

    @Test
    public void readAddressBook_invalidStoredAppointment_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("invalidAppointments.json");
        List<List<JsonAdaptedAppointment>> invalidAppointments = List.of(
                List.of(new JsonAdaptedAppointment("31-02-2026", "09:05", "")),
                List.of(new JsonAdaptedAppointment("01-01-2028", "24:00", "")),
                List.of(new JsonAdaptedAppointment("01-01-2028", "09:05", "First"),
                        new JsonAdaptedAppointment("01-01-2028", "09:05", "Second")));

        for (List<JsonAdaptedAppointment> appointments : invalidAppointments) {
            JsonAdaptedClient client = new JsonAdaptedClient(ALICE.getName().toString(),
                    ALICE.getPhone().toString(), ALICE.getEmail().toString(), ALICE.getAddress().toString(),
                    List.of(), appointments);
            JsonUtil.saveJsonFile(new JsonSerializableAddressBook(List.of(client)), filePath);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
            assertThrows(DataLoadingException.class, storage::readAddressBook);
        }
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
