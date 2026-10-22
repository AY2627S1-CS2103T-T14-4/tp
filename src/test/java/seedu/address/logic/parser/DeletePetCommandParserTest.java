package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeletePetCommand;
import seedu.address.model.pet.PetName;

public class DeletePetCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeletePetCommand.MESSAGE_USAGE);

    private final DeletePetCommandParser parser = new DeletePetCommandParser();

    @Test
    public void parse_validArgs_returnsDeletePetCommand() {
        DeletePetCommand expected = new DeletePetCommand(INDEX_FIRST_CLIENT, new PetName("Mr Fluffy"));

        assertParseSuccess(parser, " c/1 pn/Mr Fluffy", expected);
        assertParseSuccess(parser, " pn/Mr    Fluffy c/1", expected);
    }

    @Test
    public void parse_missingPrefixOrPreamble_throwsParseException() {
        assertParseFailure(parser, " pn/Milo", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " c/1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " 1 c/1 pn/Milo", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_duplicatePrefix_throwsParseException() {
        assertParseFailure(parser, " c/1 pn/Milo pn/Luna",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_PET_NAME));
    }

    @Test
    public void parse_invalidValues_throwsParseException() {
        assertParseFailure(parser, " c/0 pn/Milo", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " c/1 pn/Milo!", PetName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " c/1 pn/", PetName.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
