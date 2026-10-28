package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.CancelCommand;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;

public class CancelCommandParserTest {
    private final CancelCommandParser parser = new CancelCommandParser();

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_validInput_acceptsPastFutureAndAnyOrder() {
        CancelCommand past = new CancelCommand(INDEX_FIRST_CLIENT,
                new AppointmentDate("29-02-2024"), new AppointmentTime("00:00"));
        assertParseSuccess(parser, " c/1 d/29-02-2024 t/00:00", past);
        assertParseSuccess(parser, " t/00:00   c/1   d/29-02-2024 ", past);
        assertParseSuccess(parser, " d/29-02-2024 t/00:00 c/1", past);
        assertParseSuccess(parser, " c/1 d/31-12-9999 t/23:59", new CancelCommand(INDEX_FIRST_CLIENT,
                new AppointmentDate("31-12-9999"), new AppointmentTime("23:59")));
        assertParseSuccess(parser, " c/1 d/01-01-0001 t/00:00", new CancelCommand(INDEX_FIRST_CLIENT,
                new AppointmentDate("01-01-0001"), new AppointmentTime("00:00")));
    }

    @Test
    public void parse_missingFieldsOrPreamble_fails() {
        for (String input : new String[] {"", " c/1", " d/12-12-2026 t/14:00", " c/1 t/14:00",
            " c/1 d/12-12-2026", " extra c/1 d/12-12-2026 t/14:00", " 1 d/12-12-2026 t/14:00"}) {
            assertParseFailure(parser, input,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, CancelCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_repeatedPrefixes_failsEvenWithIdenticalOrEmptyValues() {
        Prefix[] prefixes = {CliSyntax.PREFIX_CLIENT, CliSyntax.PREFIX_DATE, CliSyntax.PREFIX_TIME};
        String[] values = {"1", "12-12-2026", "14:00"};
        for (int i = 0; i < prefixes.length; i++) {
            for (String value : new String[] {values[i], "different", ""}) {
                assertParseFailure(parser, " c/1 d/12-12-2026 t/14:00 " + prefixes[i] + value,
                        Messages.getErrorMessageForDuplicatePrefixes(prefixes[i]));
            }
        }
    }

    @Test
    public void parse_invalidIndex_fails() {
        for (String index : new String[] {"", "0", "-1", "abc", "1.5", "2147483648"}) {
            assertParseFailure(parser, " c/" + index + " d/12-12-2026 t/14:00", ParserUtil.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_invalidDate_fails() {
        for (String date : new String[] {"", "29-02-2025", "31-04-2026", "1-12-2026", "2026-12-12", "01-01-0000"}) {
            assertParseFailure(parser, " c/1 d/" + date + " t/14:00", AppointmentDate.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidTime_fails() {
        for (String time : new String[] {"", "24:00", "12:60", "1:00", "14:00:00", "noon"}) {
            assertParseFailure(parser, " c/1 d/12-12-2026 t/" + time, AppointmentTime.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_endTimeInAnyPosition_givesUnsupportedEndTimeMessage() {
        String expectedMessage = CancelCommand.MESSAGE_END_TIME_NOT_SUPPORTED + "\n" + CancelCommand.MESSAGE_USAGE;
        for (String input : new String[] {
            " c/1 d/05-12-2026 t/10:00 et/11:30",
            " et/11:30 c/1 d/05-12-2026 t/10:00",
            " c/1 et/11:30 d/05-12-2026 t/10:00",
            " c/1 d/05-12-2026 et/11:30 t/10:00"}) {
            assertParseFailure(parser, input, expectedMessage);
        }
    }

    @Test
    public void parse_emptyInvalidOrRepeatedEndTime_givesUnsupportedEndTimeMessage() {
        String expectedMessage = CancelCommand.MESSAGE_END_TIME_NOT_SUPPORTED + "\n" + CancelCommand.MESSAGE_USAGE;
        for (String endTime : new String[] {"", "11:30.", "25:00", "noon", "11:30 et/12:00"}) {
            assertParseFailure(parser, " c/1 d/05-12-2026 t/10:00 et/" + endTime, expectedMessage);
        }
        assertParseFailure(parser, " c/1 d/05-12-2026 et/11:30", expectedMessage);
    }

    @Test
    public void parse_extraPetName_failsRatherThanCancellingAnotherSlot() {
        assertParseFailure(parser, " c/1 d/12-12-2026 t/14:00 pn/Milo", AppointmentTime.MESSAGE_CONSTRAINTS);
    }
}
