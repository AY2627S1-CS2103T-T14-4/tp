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
import seedu.address.testutil.TypicalClients;

public class JsonSerializablePawPalsTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializablePawPalsTest");
    private static final Path TYPICAL_CLIENTS_FILE = TEST_DATA_FOLDER.resolve("typicalClientsPawPals.json");
    private static final Path INVALID_CLIENT_FILE = TEST_DATA_FOLDER.resolve("invalidClientPawPals.json");
    private static final Path DUPLICATE_CLIENT_FILE = TEST_DATA_FOLDER.resolve("duplicateClientPawPals.json");

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

}
