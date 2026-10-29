package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteClientCommand;
import seedu.address.logic.commands.FeedbackType;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.ReadOnlyPawPals;
import seedu.address.model.UserPrefs;
import seedu.address.model.client.Client;
import seedu.address.storage.JsonPawPalsStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ClientBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;
    private CountingPawPalsStorage pawPalsStorage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        pawPalsStorage = new CountingPawPalsStorage(temporaryFolder.resolve("pawPals.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(pawPalsStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteClientCommand = DeleteClientCommand.COMMAND_WORD + " c/9";
        assertCommandException(deleteClientCommand, MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_clear_requestsConfirmationWithoutSaving() throws Exception {
        // Requesting clear preserves both empty and populated databases without saving.
        for (boolean populated : List.of(false, true)) {
            setUp();
            if (populated) {
                model.addClient(AMY);
            }
            Model expectedModel = new ModelManager(model.getPawPals(), new UserPrefs());
            assertEquals(new CommandResult(ClearCommand.MESSAGE_CONFIRMATION, FeedbackType.NEUTRAL),
                    logic.execute("clear extra text"));
            assertEquals(expectedModel, model);
            assertEquals(0, pawPalsStorage.saveCount);
        }
    }

    @Test
    public void execute_clearConfirmed_clearsAndSavesThenResumesParsing() throws Exception {
        // Equivalence partitioning: affirmative replies while confirmation is pending (trimmed y, ignoring case).
        // Case and whitespace variants additionally check normalization within this partition.
        for (String response : List.of("y", "Y", " y ", " Y ")) {
            setUp();
            model.addClient(AMY);
            logic.execute("clear");
            assertEquals(new CommandResult(ClearCommand.MESSAGE_SUCCESS), logic.execute(response));
            assertEquals(new PawPals(), model.getPawPals());
            assertEquals(new PawPals(), pawPalsStorage.readPawPals().orElseThrow());
            assertEquals(1, pawPalsStorage.saveCount);
            assertParseException("y", MESSAGE_UNKNOWN_COMMAND);
            assertEquals(ListCommand.MESSAGE_SUCCESS, logic.execute("list").getFeedbackToUser());
            assertEquals(ClearCommand.MESSAGE_CONFIRMATION, logic.execute("clear").getFeedbackToUser());
        }
    }

    @Test
    public void execute_emptyClearConfirmed_savesEmptyDatabase() throws Exception {
        // Confirmed clear also succeeds when there are no records to remove.
        logic.execute("clear");
        assertEquals(new CommandResult(ClearCommand.MESSAGE_SUCCESS), logic.execute("y"));
        assertEquals(new PawPals(), model.getPawPals());
        assertEquals(new PawPals(), pawPalsStorage.readPawPals().orElseThrow());
        assertEquals(1, pawPalsStorage.saveCount);
    }

    @Test
    public void execute_clearCancelled_preservesDataWithoutSavingThenResumesParsing() throws Exception {
        // Equivalence partitioning: negative replies while confirmation is pending (trimmed n, ignoring case).
        // Case and whitespace variants additionally check normalization within this partition.
        for (String response : List.of("n", "N", " n ", " N ")) {
            setUp();
            model.addClient(AMY);
            logic.execute("list");
            Model expectedModel = new ModelManager(model.getPawPals(), new UserPrefs());
            logic.execute("clear");
            assertEquals(new CommandResult(ClearCommand.MESSAGE_CANCELLED, FeedbackType.NEUTRAL),
                    logic.execute(response));
            assertEquals(expectedModel, model);
            assertEquals(expectedModel.getPawPals(), pawPalsStorage.readPawPals().orElseThrow());
            assertEquals(1, pawPalsStorage.saveCount);
            assertParseException("n", MESSAGE_UNKNOWN_COMMAND);
            assertEquals(ListCommand.MESSAGE_SUCCESS, logic.execute("list").getFeedbackToUser());
            assertEquals(ClearCommand.MESSAGE_CONFIRMATION, logic.execute("clear").getFeedbackToUser());
        }
    }

    @Test
    public void execute_invalidClearResponse_preservesDataAndPendingConfirmation() throws Exception {
        model.addClient(AMY);
        logic.execute("list");
        logic.execute("clear");
        // Equivalence partitioning: replies outside the affirmative and negative partitions while pending.
        // Each invalid reply is tested with a valid model and functioning storage.
        for (String response : List.of("yes", "no", "y extra", "list", "exit", "clear", "", "   ")) {
            assertParseException(response, ClearCommand.MESSAGE_INVALID_CONFIRMATION);
            assertEquals(List.of(AMY), model.getPawPals().getClientList());
            assertEquals(model.getPawPals(), pawPalsStorage.readPawPals().orElseThrow());
            assertEquals(1, pawPalsStorage.saveCount);
        }
        assertEquals(ClearCommand.MESSAGE_SUCCESS, logic.execute("y").getFeedbackToUser());
        assertEquals(2, pawPalsStorage.saveCount);
    }

    @Test
    public void execute_confirmedClearSaveFailure_reportsErrorAndResetsConfirmation() throws Exception {
        // Equivalence partitioning of storage failures: access denied versus other I/O errors.
        for (IOException failure : List.of(DUMMY_IO_EXCEPTION, DUMMY_AD_EXCEPTION)) {
            setUp();
            model.addClient(AMY);
            logic.execute("list");
            pawPalsStorage.saveFailure = failure;
            logic.execute("clear");
            String errorFormat = failure instanceof AccessDeniedException
                    ? LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT : LogicManager.FILE_OPS_ERROR_FORMAT;
            String expectedMessage = String.format(errorFormat, failure.getMessage());
            assertThrows(CommandException.class, expectedMessage, () -> logic.execute("y"));
            assertEquals(new PawPals(), model.getPawPals());
            assertEquals(List.of(AMY), pawPalsStorage.readPawPals().orElseThrow().getClientList());
            assertParseException("y", MESSAGE_UNKNOWN_COMMAND);
            pawPalsStorage.saveFailure = null;
            assertEquals(ListCommand.MESSAGE_SUCCESS, logic.execute("list").getFeedbackToUser());
        }
    }

    /** Counts save attempts while retaining real temporary-file persistence. */
    private static class CountingPawPalsStorage extends JsonPawPalsStorage {
        private int saveCount;
        private IOException saveFailure;

        CountingPawPalsStorage(Path filePath) {
            super(filePath);
        }

        @Override
        public void savePawPals(ReadOnlyPawPals pawPals) throws IOException {
            saveCount++;
            if (saveFailure != null) {
                throw saveFailure;
            }
            super.savePawPals(pawPals);
        }
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredClientList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredClientList().remove(0));
    }

    @Test
    public void getClientList_modifyPopulatedList_throwsUnsupportedOperationException() {
        model.addClient(AMY);
        ObservableList<Client> clients = logic.getClientList();
        assertThrows(UnsupportedOperationException.class, () -> clients.add(new ClientBuilder().build()));
        assertThrows(UnsupportedOperationException.class, () -> clients.remove(0));
        assertThrows(UnsupportedOperationException.class, clients::clear);
        assertEquals(List.of(AMY), model.getPawPals().getClientList());
    }

    @Test
    public void getClientList_modelChangesAndFiltering_observesAllStoredClients() {
        ObservableList<Client> clients = logic.getClientList();
        List<List<Client>> changes = new ArrayList<>();
        clients.addListener((ListChangeListener<Client>) change -> changes.add(List.copyOf(clients)));

        model.addClient(AMY);
        assertEquals(List.of(AMY), clients);
        model.updateFilteredClientList(unused -> false);
        assertEquals(List.of(), logic.getFilteredClientList());
        assertEquals(List.of(AMY), clients);
        model.setPawPals(new PawPals());
        assertEquals(List.of(), clients);
        assertEquals(List.of(List.of(AMY), List.of()), changes);
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getPawPals(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonPawPalsStorage that throws the IOException e when saving
        JsonPawPalsStorage pawPalsStorage = new JsonPawPalsStorage(prefPath) {
            @Override
            public void savePawPals(ReadOnlyPawPals pawPals) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(pawPalsStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the savePawPals method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        Client expectedClient = new ClientBuilder(AMY).withTags().build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addClient(expectedClient);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
