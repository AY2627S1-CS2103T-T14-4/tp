package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_CLIENT;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeleteClientCommand;

/**
 * Tests the required client prefix and index validation for client deletion.
 */
public class DeleteClientCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteClientCommand.MESSAGE_USAGE);

    private final DeleteClientCommandParser parser = new DeleteClientCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteClientCommand() {
        // Boundary Value Analysis: the first positive index.
        assertParseSuccess(parser, " c/1", new DeleteClientCommand(INDEX_FIRST_CLIENT));
        // Equivalence Partition: another positive index and whitespace around the value.
        assertParseSuccess(parser, " c/2", new DeleteClientCommand(INDEX_SECOND_CLIENT));
        assertParseSuccess(parser, "   c/ 1   ", new DeleteClientCommand(INDEX_FIRST_CLIENT));
    }

    @Test
    public void parse_missingPrefixOrPreamble_throwsParseException() {
        // Equivalence Partition: missing arguments, unprefixed arguments, and nonempty preambles.
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "   ", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " 1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " a", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " 1 c/1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " text c/1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_duplicatePrefix_throwsParseException() {
        // Equivalence Partition: repeated prefixes with identical and different values.
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_CLIENT);
        assertParseFailure(parser, " c/1 c/1", expectedMessage);
        assertParseFailure(parser, " c/1 c/2", expectedMessage);
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        // Invalid Input Isolation: missing or malformed index with the required prefix present.
        assertParseFailure(parser, " c/", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " c/a", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " c/1.5", ParserUtil.MESSAGE_INVALID_INDEX);
        // Boundary Value Analysis: zero, negative, and one beyond the maximum integer.
        assertParseFailure(parser, " c/0", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " c/-1", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " c/2147483648", ParserUtil.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_extraText_throwsParseException() {
        // Invalid Input Isolation: trailing text or unsupported fields after an otherwise valid index.
        assertParseFailure(parser, " c/1 extra", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " c/1 pn/Milo", ParserUtil.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        // Equivalence Partition: null arguments.
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
