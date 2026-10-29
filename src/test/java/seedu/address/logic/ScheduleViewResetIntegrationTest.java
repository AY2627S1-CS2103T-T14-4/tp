package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ClientDisplayMode;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.storage.JsonPawPalsStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ClientBuilder;

/** Verifies schedule-view resets through parsing, command execution, model filtering and storage. */
public class ScheduleViewResetIntegrationTest {
    private static final String DATE = "29-10-2027";

    @TempDir
    public Path testFolder;

    @Test
    public void execute_addClientFromEitherScheduleMode_restoresNormalFullList() throws Exception {
        for (String scheduleCommand : List.of("schedule d/" + DATE, "schedule c/1")) {
            Fixture fixture = createFixture(scheduleCommand);

            fixture.logic.execute("addc n/New Client p/95551234 e/new@example.com a/New Address");

            assertNormalFullList(fixture.model, 3);
        }
    }

    @Test
    public void execute_editClientFromEitherScheduleMode_restoresNormalFullList() throws Exception {
        for (String scheduleCommand : List.of("schedule d/" + DATE, "schedule c/1")) {
            Fixture fixture = createFixture(scheduleCommand);

            fixture.logic.execute("edit c/1 a/New Address");

            assertNormalFullList(fixture.model, 2);
        }
    }

    @Test
    public void execute_failedAddFromEitherScheduleMode_preservesView() throws Exception {
        for (String scheduleCommand : List.of("schedule d/" + DATE, "schedule c/1")) {
            Fixture fixture = createFixture(scheduleCommand);
            ClientDisplayMode expectedMode = fixture.model.clientDisplayModeProperty().getValue();
            List<Client> expectedClients = List.copyOf(fixture.model.getFilteredClientList());

            assertThrows(CommandException.class, () -> fixture.logic.execute(
                    "addc n/Alice Test p/91234567 e/other@example.com a/Other Address"));

            assertEquals(expectedMode, fixture.model.clientDisplayModeProperty().getValue());
            assertEquals(expectedClients, fixture.model.getFilteredClientList());
        }
    }

    @Test
    public void execute_failedEditFromEitherScheduleMode_preservesView() throws Exception {
        for (String scheduleCommand : List.of("schedule d/" + DATE, "schedule c/1")) {
            Fixture fixture = createFixture(scheduleCommand);
            ClientDisplayMode expectedMode = fixture.model.clientDisplayModeProperty().getValue();
            List<Client> expectedClients = List.copyOf(fixture.model.getFilteredClientList());

            assertThrows(CommandException.class, () -> fixture.logic.execute("edit c/1 n/Bob Test p/92345678"));

            assertEquals(expectedMode, fixture.model.clientDisplayModeProperty().getValue());
            assertEquals(expectedClients, fixture.model.getFilteredClientList());
        }
    }

    private Fixture createFixture(String scheduleCommand) throws Exception {
        Appointment appointment = new Appointment(new AppointmentDate(DATE), new AppointmentTime("10:00"));
        Client alice = new ClientBuilder().withName("Alice Test").withPhone("91234567")
                .withEmail("alice@example.com").withAppointments(appointment).build();
        Client bob = new ClientBuilder().withName("Bob Test").withPhone("92345678")
                .withEmail("bob@example.com").build();
        PawPals pawPals = new PawPals();
        pawPals.setClients(List.of(alice, bob));
        Model model = new ModelManager(pawPals, new UserPrefs());
        JsonPawPalsStorage dataStorage = new JsonPawPalsStorage(testFolder.resolve("pawpals.json"));
        Logic logic = new LogicManager(model, new StorageManager(dataStorage,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
        logic.execute(scheduleCommand);
        return new Fixture(model, logic);
    }

    private static void assertNormalFullList(Model model, int expectedSize) {
        assertEquals(ClientDisplayMode.NORMAL, model.clientDisplayModeProperty().getValue());
        assertEquals(expectedSize, model.getFilteredClientList().size());
        assertEquals(model.getPawPals().getClientList(), model.getFilteredClientList());
    }

    private record Fixture(Model model, Logic logic) {
    }
}
