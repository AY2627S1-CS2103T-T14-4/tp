package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AppointmentCommand;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;

public class AppointmentCommandParserTest {
    private final AppointmentCommandParser parser = new AppointmentCommandParser();

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_validInput_acceptsAnyOrderAndLeavesFutureCheckToCommand() {
        AppointmentCommand expected = new AppointmentCommand(INDEX_FIRST_CLIENT,
                new Appointment(new AppointmentDate("29-02-2024"), new AppointmentTime("00:00")));
        assertParseSuccess(parser, " c/1 d/29-02-2024 t/00:00", expected);
        assertParseSuccess(parser, " t/00:00   c/1   d/29-02-2024 ", expected);
    }

    @Test
    public void parse_missingFieldsOrPreamble_fails() {
        for (String input : new String[] {"", " c/1", " d/12-12-2026 t/12:00",
            " c/1 t/12:00", " c/1 d/12-12-2026", " extra c/1 d/12-12-2026 t/12:00"}) {
            assertParseFailure(parser, input,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AppointmentCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_repeatedPrefixes_failsEvenWithIdenticalValues() {
        Prefix[] prefixes = {CliSyntax.PREFIX_CLIENT, CliSyntax.PREFIX_DATE, CliSyntax.PREFIX_TIME};
        String[] values = {"1", "12-12-2026", "12:00"};
        for (int i = 0; i < prefixes.length; i++) {
            Prefix prefix = prefixes[i];
            assertParseFailure(parser, " c/1 d/12-12-2026 t/12:00 " + prefix + values[i],
                    Messages.getErrorMessageForDuplicatePrefixes(prefix));
            assertParseFailure(parser, " c/1 d/12-12-2026 t/12:00 " + prefix + "different",
                    Messages.getErrorMessageForDuplicatePrefixes(prefix));
        }
    }

    @Test
    public void parse_invalidIndex_fails() {
        for (String index : new String[] {"", "0", "-1", "abc", "1.5", "2147483648"}) {
            assertParseFailure(parser, " c/" + index + " d/12-12-2026 t/12:00", ParserUtil.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_invalidDate_fails() {
        for (String date : new String[] {"", "29-02-2025", "31-04-2026", "1-12-2026", "2026-12-12", "01-01-0000"}) {
            assertParseFailure(parser, " c/1 d/" + date + " t/12:00", AppointmentDate.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidTime_fails() {
        for (String time : new String[] {"", "24:00", "12:60", "1:00", "12:00:00", "noon"}) {
            assertParseFailure(parser, " c/1 d/12-12-2026 t/" + time, AppointmentTime.MESSAGE_CONSTRAINTS);
        }
    }
}
