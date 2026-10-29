package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.logging.Logger;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ConfirmableCommand;
import seedu.address.logic.commands.ConfirmationRequest;
import seedu.address.logic.commands.FeedbackType;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.PawPalsParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ClientDisplayMode;
import seedu.address.model.Model;
import seedu.address.model.client.Client;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final PawPalsParser pawPalsParser;
    private ConfirmationRequest pendingConfirmation;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        pawPalsParser = new PawPalsParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        if (pendingConfirmation != null) {
            return handleConfirmation(commandText);
        }

        Command command = pawPalsParser.parseCommand(commandText);
        if (command instanceof ConfirmableCommand confirmableCommand) {
            pendingConfirmation = confirmableCommand.prepareConfirmation(model);
            return new CommandResult(pendingConfirmation.getPrompt(), FeedbackType.NEUTRAL);
        }
        return executeAndSave(command);
    }

    /**
     * Handles a response to the pending request without parsing it as another command.
     */
    private CommandResult handleConfirmation(String commandText) throws CommandException, ParseException {
        String response = commandText.trim();
        if (response.equalsIgnoreCase("n")) {
            pendingConfirmation = null;
            return new CommandResult(ConfirmationRequest.MESSAGE_CANCELLED, FeedbackType.NEUTRAL);
        }
        if (!response.equalsIgnoreCase("y")) {
            throw new ParseException(ConfirmationRequest.MESSAGE_INVALID_RESPONSE + pendingConfirmation.getPrompt());
        }

        Command confirmedCommand = pendingConfirmation.getCommand();
        pendingConfirmation = null;
        return executeAndSave(confirmedCommand);
    }

    /**
     * Executes a command and saves its result through the normal storage error handling.
     */
    private CommandResult executeAndSave(Command command) throws CommandException {
        CommandResult commandResult = command.execute(model);

        try {
            storage.savePawPals(model.getPawPals());
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        return commandResult;
    }

    @Override
    public ObservableList<Client> getFilteredClientList() {
        return model.getFilteredClientList();
    }

    @Override
    public ObservableList<Client> getClientList() {
        return model.getPawPals().getClientList();
    }

    @Override
    public ObservableValue<ClientDisplayMode> clientDisplayModeProperty() {
        return model.clientDisplayModeProperty();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
