package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;
import static seedu.address.testutil.TypicalClients.HOON;
import static seedu.address.testutil.TypicalClients.IDA;
import static seedu.address.testutil.TypicalClients.getTypicalPawPals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.PawPals;
import seedu.address.model.ReadOnlyPawPals;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.PetNote;
import seedu.address.model.pet.Species;

public class JsonPawPalsStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonPawPalsStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readPawPals_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readPawPals(null));
    }

    private java.util.Optional<ReadOnlyPawPals> readPawPals(String filePath) throws Exception {
        return new JsonPawPalsStorage(Paths.get(filePath)).readPawPals(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readPawPals("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readPawPals("notJsonFormatPawPals.json"));
    }

    @Test
    public void readPawPals_invalidClientPawPals_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readPawPals("invalidClientPawPals.json"));
    }

    @Test
    public void readPawPals_invalidAndValidClientPawPals_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readPawPals("invalidAndValidClientPawPals.json"));
    }

    @Test
    public void backupCorruptedData_createsUniqueCopy() throws Exception {
        Path filePath = testFolder.resolve("pawpals.json");
        String corruptedContents = "{\"clients\": [invalid]}";
        Files.writeString(filePath, corruptedContents);
        JsonPawPalsStorage storage = new JsonPawPalsStorage(filePath);

        Path firstBackup = storage.backupCorruptedData();
        Path secondBackup = storage.backupCorruptedData();

        assertNotEquals(firstBackup, secondBackup);
        assertEquals(corruptedContents, Files.readString(firstBackup));
        assertEquals(corruptedContents, Files.readString(secondBackup));
        assertEquals(corruptedContents, Files.readString(filePath));
    }

    @Test
    public void backupCorruptedData_copyFails_deletesTemporaryBackup() throws Exception {
        Path missingFile = testFolder.resolve("missing.json");
        JsonPawPalsStorage storage = new JsonPawPalsStorage(missingFile);

        assertThrows(IOException.class, storage::backupCorruptedData);
    }

    @Test
    public void readAndSavePawPals_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempPawPals.json");
        PawPals original = getTypicalPawPals();
        JsonPawPalsStorage jsonPawPalsStorage = new JsonPawPalsStorage(filePath);

        // Save in new file and read back
        jsonPawPalsStorage.savePawPals(original, filePath);
        ReadOnlyPawPals readBack = jsonPawPalsStorage.readPawPals(filePath).get();
        assertEquals(original, new PawPals(readBack));

        // Modify data, overwrite existing file, and read back
        original.addClient(HOON);
        original.removeClient(ALICE);
        jsonPawPalsStorage.savePawPals(original, filePath);
        readBack = jsonPawPalsStorage.readPawPals(filePath).get();
        assertEquals(original, new PawPals(readBack));

        // Save and read without specifying file path
        original.addClient(IDA);
        jsonPawPalsStorage.savePawPals(original); // file path not specified
        readBack = jsonPawPalsStorage.readPawPals().get(); // file path not specified
        assertEquals(original, new PawPals(readBack));

    }

    @Test
    public void readAndSavePawPals_appointmentsRemainWithTheirClients() throws Exception {
        Path filePath = testFolder.resolve("appointments.json");
        Appointment pastAppointment = new Appointment(new AppointmentDate("29-02-2024"),
                new AppointmentTime("09:05"), "Old visit");
        Appointment futureAppointment = new Appointment(new AppointmentDate("01-01-2028"),
                new AppointmentTime("12:30"));
        Client alice = ALICE.copyWithAppointment(pastAppointment).copyWithAppointment(futureAppointment);
        Client benson = BENSON.copyWithAppointment(pastAppointment);
        PawPals original = new PawPals();
        original.addClient(alice);
        original.addClient(benson);

        JsonPawPalsStorage storage = new JsonPawPalsStorage(filePath);
        storage.savePawPals(original);
        PawPals restored = new PawPals(storage.readPawPals().get());
        assertEquals(original, restored);
        assertEquals(alice.getAppointments(), restored.getClientList().get(0).getAppointments());
        assertEquals(benson.getAppointments(), restored.getClientList().get(1).getAppointments());
    }

    @Test
    public void readAndSavePawPals_petsRemainWithTheirClients() throws Exception {
        Path filePath = testFolder.resolve("pets.json");
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"))
                .withNote(NoteType.SENS, new PetNote("Sensitive paws"))
                .withNote(NoteType.TEMP, new PetNote("Anxious around dryers"));
        Pet luna = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));
        Client alice = ALICE.withPet(milo);
        Client benson = BENSON.withPet(milo).withPet(luna);
        PawPals original = new PawPals();
        original.addClient(alice);
        original.addClient(benson);

        JsonPawPalsStorage storage = new JsonPawPalsStorage(filePath);
        storage.savePawPals(original);
        PawPals restored = new PawPals(storage.readPawPals().get());

        assertEquals(original, restored);
        assertEquals(alice.getPets(), restored.getClientList().get(0).getPets());
        assertEquals(benson.getPets(), restored.getClientList().get(1).getPets());
        assertEquals(milo.getNotes(), restored.getClientList().get(0).getPets().get(0).getNotes());
    }

    @Test
    public void readPawPals_invalidStoredPet_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("invalidPets.json");
        List<JsonAdaptedPet> invalidPets = List.of(new JsonAdaptedPet("Milo", null, "Poodle"));
        JsonAdaptedClient client = new JsonAdaptedClient(ALICE.getName().toString(),
                ALICE.getPhone().toString(), ALICE.getEmail().toString(), ALICE.getAddress().toString(),
                List.of(), invalidPets, List.of());
        JsonUtil.saveJsonFile(new JsonSerializablePawPals(List.of(client)), filePath);

        JsonPawPalsStorage storage = new JsonPawPalsStorage(filePath);
        assertThrows(DataLoadingException.class, storage::readPawPals);
    }

    @Test
    public void readPawPals_invalidStoredAppointment_throwsDataLoadingException() throws Exception {
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
            JsonUtil.saveJsonFile(new JsonSerializablePawPals(List.of(client)), filePath);
            JsonPawPalsStorage storage = new JsonPawPalsStorage(filePath);
            assertThrows(DataLoadingException.class, storage::readPawPals);
        }
    }

    @Test
    public void savePawPals_nullPawPals_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> savePawPals(null, "SomeFile.json"));
    }

    /**
     * Saves {@code pawPals} at the specified {@code filePath}.
     */
    private void savePawPals(ReadOnlyPawPals pawPals, String filePath) {
        try {
            new JsonPawPalsStorage(Paths.get(filePath))
                    .savePawPals(pawPals, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void savePawPals_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> savePawPals(new PawPals(), null));
    }
}
