package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditAppointmentCommand;
import seedu.address.logic.commands.EditAppointmentCommand.EditAppointmentDescriptor;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;

/**
 * Tests rescheduling syntax independently of the model and current clock.
 */
public class EditAppointmentCommandParserTest {
    private static final String TARGET_ARGUMENTS = " c/1 d/29-02-2024 t/14:00";
    private static final AppointmentDate ORIGINAL_DATE = new AppointmentDate("29-02-2024");
    private static final AppointmentTime ORIGINAL_START = new AppointmentTime("14:00");

    private final EditAppointmentCommandParser parser = new EditAppointmentCommandParser();

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_eachEditCombination_succeeds() {
        assertParseSuccess(parser, TARGET_ARGUMENTS + " nd/13-12-2026", command("13-12-2026", null, null));
        assertParseSuccess(parser, TARGET_ARGUMENTS + " ns/16:00", command(null, "16:00", null));
        assertParseSuccess(parser, TARGET_ARGUMENTS + " ne/17:00", command(null, null, "17:00"));
        assertParseSuccess(parser, TARGET_ARGUMENTS + " nd/13-12-2026 ns/16:00",
                command("13-12-2026", "16:00", null));
        assertParseSuccess(parser, TARGET_ARGUMENTS + " nd/13-12-2026 ne/17:00",
                command("13-12-2026", null, "17:00"));
        assertParseSuccess(parser, TARGET_ARGUMENTS + " ns/16:00 ne/17:00", command(null, "16:00", "17:00"));
        assertParseSuccess(parser, TARGET_ARGUMENTS + " nd/13-12-2026 ns/16:00 ne/17:00",
                command("13-12-2026", "16:00", "17:00"));
    }

    @Test
    public void parse_reorderedPrefixesAndWhitespace_succeeds() {
        assertParseSuccess(parser, " ne/17:00  ns/16:00 t/14:00 nd/13-12-2026 d/29-02-2024 c/1 ",
                command("13-12-2026", "16:00", "17:00"));
        assertParseSuccess(parser, " nd/ 13-12-2026  t/ 14:00  c/ 1 d/ 29-02-2024 ",
                command("13-12-2026", null, null));
    }

    @Test
    public void parse_missingRequiredPrefixesOrPreamble_fails() {
        for (String input : new String[] {"", " ns/16:00", " d/29-02-2024 t/14:00 ns/16:00",
            " c/1 t/14:00 ns/16:00", " c/1 d/29-02-2024 ns/16:00", " extra" + TARGET_ARGUMENTS + " ns/16:00"}) {
            assertParseFailure(parser, input,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditAppointmentCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_noEdits_fails() {
        assertParseFailure(parser, TARGET_ARGUMENTS, EditAppointmentCommand.MESSAGE_NOT_EDITED);
    }

    @Test
    public void parse_duplicatePrefixes_failsEvenWithIdenticalOrEmptyValues() {
        Prefix[] prefixes = {CliSyntax.PREFIX_CLIENT, CliSyntax.PREFIX_DATE, CliSyntax.PREFIX_TIME,
            CliSyntax.PREFIX_NEW_DATE, CliSyntax.PREFIX_NEW_START_TIME, CliSyntax.PREFIX_NEW_END_TIME};
        String[] values = {"1", "29-02-2024", "14:00", "13-12-2026", "16:00", "17:00"};
        String valid = TARGET_ARGUMENTS + " nd/13-12-2026 ns/16:00 ne/17:00";
        for (int i = 0; i < prefixes.length; i++) {
            for (String value : new String[] {values[i], "invalid", ""}) {
                assertParseFailure(parser, valid + " " + prefixes[i] + value,
                        Messages.getErrorMessageForDuplicatePrefixes(prefixes[i]));
            }
        }
    }

    @Test
    public void parse_invalidIndex_fails() {
        for (String index : new String[] {"", "0", "-1", "abc", "1.5", "2147483648"}) {
            assertParseFailure(parser, " c/" + index + " d/29-02-2024 t/14:00 ns/16:00",
                    ParserUtil.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_invalidOriginalOrNewDate_fails() {
        for (String date : new String[] {"", "29-02-2025", "31-04-2026", "1-12-2026", "2026-12-12",
            "01-01-0000", "01-01-10000"}) {
            assertParseFailure(parser, " c/1 d/" + date + " t/14:00 ns/16:00", AppointmentDate.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, TARGET_ARGUMENTS + " nd/" + date, AppointmentDate.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidOriginalOrNewTimes_fails() {
        for (String time : new String[] {"", "24:00", "12:60", "1:00", "14:00:00", "noon"}) {
            assertParseFailure(parser, " c/1 d/29-02-2024 t/" + time + " ns/16:00",
                    AppointmentTime.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, TARGET_ARGUMENTS + " ns/" + time, AppointmentTime.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, TARGET_ARGUMENTS + " ne/" + time, AppointmentTime.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_validDateAndTimeBoundaries_succeeds() {
        for (String date : new String[] {"01-01-0001", "29-02-2024", "31-12-9999"}) {
            assertParseSuccess(parser, TARGET_ARGUMENTS + " nd/" + date, command(date, null, null));
            assertParseSuccess(parser, " c/1 d/" + date + " t/00:00 ne/23:59",
                    new EditAppointmentCommand(INDEX_FIRST_CLIENT, new AppointmentDate(date),
                            new AppointmentTime("00:00"), edits(null, null, "23:59")));
        }
        assertParseSuccess(parser, TARGET_ARGUMENTS + " ns/00:00 ne/23:59", command(null, "00:00", "23:59"));
        assertParseSuccess(parser, " c/1 d/29-02-2024 t/23:59 ns/00:00",
                new EditAppointmentCommand(INDEX_FIRST_CLIENT, ORIGINAL_DATE,
                        new AppointmentTime("23:59"), edits(null, "00:00", null)));
    }

    @Test
    public void parse_slotEligibility_isDeferredToExecution() {
        assertParseSuccess(parser, TARGET_ARGUMENTS + " nd/01-01-2000 ns/17:00 ne/16:00",
                command("01-01-2000", "17:00", "16:00"));
    }

    @Test
    public void parse_extraOrUnsupportedArguments_fails() {
        for (String extra : new String[] {" et/17:00", " pn/Milo", " unknown/value", " extra"}) {
            assertParseFailure(parser, TARGET_ARGUMENTS + " ns/16:00" + extra, AppointmentTime.MESSAGE_CONSTRAINTS);
        }
    }

    private static EditAppointmentCommand command(String date, String start, String end) {
        return new EditAppointmentCommand(INDEX_FIRST_CLIENT, ORIGINAL_DATE, ORIGINAL_START, edits(date, start, end));
    }

    private static EditAppointmentDescriptor edits(String date, String start, String end) {
        return new EditAppointmentDescriptor(Optional.ofNullable(date).map(AppointmentDate::new),
                Optional.ofNullable(start).map(AppointmentTime::new),
                Optional.ofNullable(end).map(AppointmentTime::new));
    }
}
