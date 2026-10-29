package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ScheduleCommand;
import seedu.address.model.appointment.AppointmentDate;

public class ScheduleCommandParserTest {
    private final ScheduleCommandParser parser = new ScheduleCommandParser();

    @Test
    public void parse_validDate_success() {
        assertParseSuccess(parser, " d/29-10-2026",
                new ScheduleCommand(new AppointmentDate("29-10-2026")));
    }

    @Test
    public void parse_validClientIndex_success() {
        assertParseSuccess(parser, " c/1", new ScheduleCommand(INDEX_FIRST_CLIENT));
    }

    @Test
    public void parse_missingOrMultipleModes_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ScheduleCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", expected);
        assertParseFailure(parser, " c/1 d/29-10-2026", expected);
        assertParseFailure(parser, " extra c/1", expected);
    }

    @Test
    public void parse_duplicatePrefix_failure() {
        assertParseFailure(parser, " c/1 c/2",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_CLIENT));
        assertParseFailure(parser, " d/29-10-2026 d/30-10-2026",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_DATE));
    }

    @Test
    public void parse_invalidValues_failure() {
        assertParseFailure(parser, " c/0", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " d/31-02-2026", AppointmentDate.MESSAGE_CONSTRAINTS);
    }
}
