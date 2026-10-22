package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeleteNoteCommand;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.PetName;

public class DeleteNoteCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteNoteCommand.MESSAGE_USAGE);

    private final DeleteNoteCommandParser parser = new DeleteNoteCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteNoteCommand() {
        DeleteNoteCommand expected = new DeleteNoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"), NoteType.TEMP);

        assertParseSuccess(parser, " c/1 pn/Milo ty/temp", expected);
        assertParseSuccess(parser, " ty/TEMP c/1 pn/Milo", expected);
        assertParseSuccess(parser, " c/1 pn/  Milo  ty/ temp ", expected);
    }

    @Test
    public void parse_missingPrefixOrPreamble_throwsParseException() {
        assertParseFailure(parser, " pn/Milo ty/temp", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " c/1 ty/temp", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " c/1 pn/Milo", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " extra c/1 pn/Milo ty/temp", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_duplicatePrefix_throwsParseException() {
        assertParseFailure(parser, " c/1 pn/Milo ty/temp ty/pref",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NOTE_TYPE));
    }

    @Test
    public void parse_invalidValues_throwsParseException() {
        assertParseFailure(parser, " c/0 pn/Milo ty/temp", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " c/1 pn/Milo! ty/temp", PetName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " c/1 pn/Milo ty/mood", NoteType.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " c/1 pn/Milo ty/", NoteType.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
