package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.storage.DataRecoveryInfo;

public class UiManagerTest {

    @Test
    public void dataRecoveryWarningContent_withBackup_includesReasonAndBackupPath() {
        DataRecoveryInfo recoveryInfo = new DataRecoveryInfo(
                Path.of("pawpals.json"), Optional.of(Path.of("pawpals.json.bak")),
                "Phone numbers should only contain digits");

        String content = UiManager.getDataRecoveryWarningContent(recoveryInfo);

        assertTrue(content.contains("Reason: Phone numbers should only contain digits"));
        assertTrue(content.contains("A backup was created at:\n" + Path.of("pawpals.json.bak")));
    }

    @Test
    public void dataRecoveryWarningContent_withoutBackup_mentionsBackupFailure() {
        DataRecoveryInfo recoveryInfo = new DataRecoveryInfo(
                Path.of("pawpals.json"), Optional.empty(), "Invalid JSON");

        String content = UiManager.getDataRecoveryWarningContent(recoveryInfo);

        assertTrue(content.contains("Reason: Invalid JSON"));
        assertTrue(content.contains("PawPals could not create a backup of the rejected file."));
    }
}
