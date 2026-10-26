package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.DoneCommand;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.history.SessionNotes;

public class DoneCommandParserTest {
    private final DoneCommandParser parser = new DoneCommandParser();

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_validInput_acceptsAnyOrderAndLeavesEligibilityToCommand() {
        DoneCommand expected = new DoneCommand(INDEX_FIRST_CLIENT, new AppointmentDate("29-02-2024"),
                new AppointmentTime("00:00"), new SessionNotes("Teddy cut,  short ears!"));
        assertParseSuccess(parser, " c/1 d/29-02-2024 t/00:00 nt/Teddy cut,  short ears!", expected);
        assertParseSuccess(parser, " nt/  Teddy cut,  short ears!  t/00:00 d/29-02-2024 c/1 ", expected);
        assertParseSuccess(parser, " c/1 d/31-12-9999 t/23:59 nt/Notes", new DoneCommand(INDEX_FIRST_CLIENT,
                new AppointmentDate("31-12-9999"), new AppointmentTime("23:59"), new SessionNotes("Notes")));
        assertParseSuccess(parser, " c/1 d/01-01-0001 t/00:00 nt/Notes", new DoneCommand(INDEX_FIRST_CLIENT,
                new AppointmentDate("01-01-0001"), new AppointmentTime("00:00"), new SessionNotes("Notes")));
    }

    @Test
    public void parse_notesBoundaryLengths_acceptsAfterTrimming() {
        for (String notes : new String[] {"x", "x".repeat(200), "修剪毛发"}) {
            DoneCommand expected = new DoneCommand(INDEX_FIRST_CLIENT, new AppointmentDate("12-12-2026"),
                    new AppointmentTime("14:00"), new SessionNotes(notes));
            assertParseSuccess(parser, " c/1 d/12-12-2026 t/14:00 nt/  " + notes + "  ", expected);
        }
    }

    @Test
    public void parse_missingFieldsOrPreamble_fails() {
        for (String input : new String[] {"", " c/1", " d/12-12-2026 t/14:00 nt/Notes",
            " c/1 t/14:00 nt/Notes", " c/1 d/12-12-2026 nt/Notes", " c/1 d/12-12-2026 t/14:00",
            " extra c/1 d/12-12-2026 t/14:00 nt/Notes"}) {
            assertParseFailure(parser, input, String.format(MESSAGE_INVALID_COMMAND_FORMAT, DoneCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_repeatedPrefixes_failsEvenWithIdenticalOrEmptyValues() {
        Prefix[] prefixes = {CliSyntax.PREFIX_CLIENT, CliSyntax.PREFIX_DATE,
            CliSyntax.PREFIX_TIME, CliSyntax.PREFIX_NOTE};
        String[] values = {"1", "12-12-2026", "14:00", "Notes"};
        for (int i = 0; i < prefixes.length; i++) {
            for (String value : new String[] {values[i], "different", ""}) {
                assertParseFailure(parser, " nt/Notes c/1 d/12-12-2026 t/14:00 " + prefixes[i] + value,
                        Messages.getErrorMessageForDuplicatePrefixes(prefixes[i]));
            }
        }
        assertParseFailure(parser, " c/1 c/2 d/12-12-2026 t/14:00 nt/Notes",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_CLIENT));
    }

    @Test
    public void parse_prefixLikeTextInTrailingNotes_keptInNotes() {
        String notes = "Teddy cut, d/c brushing, c/o owner, t/ trim, nt/ more";
        DoneCommand expected = new DoneCommand(INDEX_FIRST_CLIENT, new AppointmentDate("12-12-2026"),
                new AppointmentTime("14:00"), new SessionNotes(notes));
        assertParseSuccess(parser, " c/1 d/12-12-2026 t/14:00 nt/" + notes, expected);
        assertParseSuccess(parser, " t/14:00 c/1 d/12-12-2026 nt/" + notes, expected);
    }

    @Test
    public void parse_invalidTargetValues_preservesSharedParserMessages() {
        for (String index : new String[] {"", "0", "-1", "abc", "1.5", "2147483648"}) {
            assertParseFailure(parser, " c/" + index + " d/12-12-2026 t/14:00 nt/Notes",
                    ParserUtil.MESSAGE_INVALID_INDEX);
        }
        for (String date : new String[] {"", "29-02-2025", "31-04-2026", "1-12-2026", "2026-12-12", "01-01-0000"}) {
            assertParseFailure(parser, " c/1 d/" + date + " t/14:00 nt/Notes", AppointmentDate.MESSAGE_CONSTRAINTS);
        }
        for (String time : new String[] {"", "24:00", "12:60", "1:00", "14:00:00", "noon"}) {
            assertParseFailure(parser, " c/1 d/12-12-2026 t/" + time + " nt/Notes",
                    AppointmentTime.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidNotes_fails() {
        for (String notes : new String[] {"", " \t ", "x".repeat(201)}) {
            assertParseFailure(parser, " c/1 d/12-12-2026 t/14:00 nt/" + notes, SessionNotes.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_endTimeOrPetNameAsTarget_fails() {
        assertParseFailure(parser, " c/1 d/12-12-2026 t/14:00 et/15:00 nt/Notes", AppointmentTime.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " c/1 d/12-12-2026 t/14:00 pn/Milo nt/Notes", AppointmentTime.MESSAGE_CONSTRAINTS);
    }
}
