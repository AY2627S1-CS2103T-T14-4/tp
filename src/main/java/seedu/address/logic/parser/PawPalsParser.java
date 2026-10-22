package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AddPetCommand;
import seedu.address.logic.commands.AppointmentCommand;
import seedu.address.logic.commands.CancelCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteClientCommand;
import seedu.address.logic.commands.DeleteNoteCommand;
import seedu.address.logic.commands.DoneCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditPetCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.FindPetCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class PawPalsParser {

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");
    private static final Logger logger = LogsCenter.getLogger(PawPalsParser.class);

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");

        // Note to developers: Change LOG_LEVEL in LogsCenter to enable lower level (i.e., FINE, FINER and lower)
        // log messages such as the one below.
        // Lower level log messages are used sparingly to minimize noise in the code.
        logger.fine("Command word: " + commandWord + "; Arguments: " + arguments);

        return switch (commandWord) {
            case AddCommand.COMMAND_WORD -> new AddCommandParser().parse(arguments);
            case AddPetCommand.COMMAND_WORD -> new AddPetCommandParser().parse(arguments);
            case AppointmentCommand.COMMAND_WORD -> new AppointmentCommandParser().parse(arguments);
            case CancelCommand.COMMAND_WORD -> new CancelCommandParser().parse(arguments);
            case DoneCommand.COMMAND_WORD -> new DoneCommandParser().parse(arguments);
            case EditCommand.COMMAND_WORD -> new EditCommandParser().parse(arguments);
            case EditPetCommand.COMMAND_WORD -> new EditPetCommandParser().parse(arguments);
            case DeleteClientCommand.COMMAND_WORD -> new DeleteClientCommandParser().parse(arguments);
            case ClearCommand.COMMAND_WORD -> new ClearCommand();
            case FindCommand.COMMAND_WORD -> new FindCommandParser().parse(arguments);
            case FindPetCommand.COMMAND_WORD -> new FindPetCommandParser().parse(arguments);
            case NoteCommand.COMMAND_WORD -> new NoteCommandParser().parse(arguments);
            case DeleteNoteCommand.COMMAND_WORD -> new DeleteNoteCommandParser().parse(arguments);
            case ListCommand.COMMAND_WORD -> new ListCommand();
            case ExitCommand.COMMAND_WORD -> new ExitCommand();
            case HelpCommand.COMMAND_WORD -> new HelpCommand();
            default -> {
                logger.finer("This user input caused a ParseException: " + userInput);
                throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
            }
        };
    }
}
