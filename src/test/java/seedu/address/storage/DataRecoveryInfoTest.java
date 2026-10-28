package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class DataRecoveryInfoTest {

    @Test
    public void accessors_returnSuppliedRecoveryDetails() {
        Path dataFile = Path.of("data.json");
        Path backupFile = Path.of("data.json.bak");
        DataRecoveryInfo info = new DataRecoveryInfo(dataFile, Optional.of(backupFile), "Invalid phone");

        assertEquals(dataFile, info.dataFilePath());
        assertEquals(Optional.of(backupFile), info.backupFilePath());
        assertEquals("Invalid phone", info.loadingError());
    }
}
