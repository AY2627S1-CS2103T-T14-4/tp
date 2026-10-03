package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {

    private static final String NON_EMPTY_REMARK = "Some remark.";

    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_indexAndRemarkSpecified_success() {
        Index targetIndex = INDEX_FIRST_PERSON;

        String userInput =
                targetIndex.getOneBased()
                        + " "
                        + PREFIX_REMARK
                        + NON_EMPTY_REMARK;

        RemarkCommand expectedCommand =
                new RemarkCommand(
                        INDEX_FIRST_PERSON,
                        new Remark(NON_EMPTY_REMARK));

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_emptyRemark_success() {
        Index targetIndex = INDEX_FIRST_PERSON;

        String userInput =
                targetIndex.getOneBased()
                        + " "
                        + PREFIX_REMARK;

        RemarkCommand expectedCommand =
                new RemarkCommand(
                        INDEX_FIRST_PERSON,
                        new Remark(""));

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_invalidIndex_failure() {
        String expectedMessage =
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        RemarkCommand.MESSAGE_USAGE);

        assertParseFailure(
                parser,
                "abc " + PREFIX_REMARK + NON_EMPTY_REMARK,
                expectedMessage);
    }

    @Test
    public void parse_missingIndex_failure() {
        String expectedMessage =
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        RemarkCommand.MESSAGE_USAGE);

        assertParseFailure(
                parser,
                PREFIX_REMARK + NON_EMPTY_REMARK,
                expectedMessage);
    }
}
