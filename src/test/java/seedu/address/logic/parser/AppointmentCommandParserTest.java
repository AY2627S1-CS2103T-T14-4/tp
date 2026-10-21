package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AppointmentCommand;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.pet.PetName;

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
    public void parse_validEndTime_acceptsAnyOrderAndBoundaryTimes() {
        AppointmentCommand expected = new AppointmentCommand(INDEX_FIRST_CLIENT,
                new Appointment(new AppointmentDate("29-02-2024"), new AppointmentTime("00:00"))
                        .withEndTime(new AppointmentTime("23:59")));
        assertParseSuccess(parser, " c/1 d/29-02-2024 t/00:00 et/23:59", expected);
        assertParseSuccess(parser, " et/23:59   t/00:00 c/1 d/29-02-2024 ", expected);
        assertParseSuccess(parser, " c/1 et/23:59 d/29-02-2024 t/00:00", expected);

        AppointmentCommand oneMinute = new AppointmentCommand(INDEX_FIRST_CLIENT,
                new Appointment(new AppointmentDate("12-12-2026"), new AppointmentTime("23:58"))
                        .withEndTime(new AppointmentTime("23:59")));
        assertParseSuccess(parser, " c/1 d/12-12-2026 t/23:58 et/23:59", oneMinute);
    }

    @Test
    public void parse_multiplePetNames_acceptsAnyOrderAndPreservesEndTime() {
        Appointment appointment = new Appointment(new AppointmentDate("12-12-2026"), new AppointmentTime("14:00"))
                .withEndTime(new AppointmentTime("15:00"))
                .withPetNames(new LinkedHashSet<>(List.of(new PetName("Milo"), new PetName("Sir Fluffy"))));
        AppointmentCommand expected = new AppointmentCommand(INDEX_FIRST_CLIENT, appointment);
        assertParseSuccess(parser, " c/1 d/12-12-2026 t/14:00 et/15:00 pn/Milo pn/Sir Fluffy", expected);
        assertParseSuccess(parser, " pn/Milo et/15:00 c/1 pn/  Sir   Fluffy  d/12-12-2026 t/14:00", expected);
    }

    @Test
    public void parse_petNameWithoutEndTime_acceptsBoundaryNamesAndNormalizesSpaces() {
        for (String name : List.of("A", "x".repeat(40), "小白", "O'Malley", "Milo-2")) {
            Appointment appointment = new Appointment(new AppointmentDate("12-12-2026"), new AppointmentTime("14:00"))
                    .withPetNames(Set.of(new PetName(name)));
            assertParseSuccess(parser, " c/1 d/12-12-2026 t/14:00 pn/ " + name + " ",
                    new AppointmentCommand(INDEX_FIRST_CLIENT, appointment));
        }
    }

    @Test
    public void parse_repeatedPetNames_ignoresIdenticalNormalizedReferences() {
        Appointment appointment = new Appointment(new AppointmentDate("12-12-2026"), new AppointmentTime("14:00"))
                .withPetNames(Set.of(new PetName("Sir Fluffy")));
        assertParseSuccess(parser, " c/1 d/12-12-2026 t/14:00 pn/Sir Fluffy pn/ Sir   Fluffy ",
                new AppointmentCommand(INDEX_FIRST_CLIENT, appointment));
    }

    @Test
    public void parse_invalidPetName_failsEvenAfterAValidReference() {
        for (String name : new String[] {"", " ", "M!lo", "x".repeat(41), "Milo nt/notes"}) {
            assertParseFailure(parser, " c/1 d/12-12-2026 t/14:00 pn/" + name, PetName.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, " c/1 d/12-12-2026 t/14:00 pn/Milo pn/" + name,
                    PetName.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_petNamesPresent_preservesRequiredAndSingleValuedPrefixValidation() {
        assertParseFailure(parser, " c/1 d/12-12-2026 pn/Milo",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AppointmentCommand.MESSAGE_USAGE));
        for (Prefix prefix : List.of(CliSyntax.PREFIX_CLIENT, CliSyntax.PREFIX_DATE,
                CliSyntax.PREFIX_TIME, CliSyntax.PREFIX_END_TIME)) {
            assertParseFailure(parser, " c/1 d/12-12-2026 t/14:00 et/15:00 pn/Milo " + prefix + "invalid",
                    Messages.getErrorMessageForDuplicatePrefixes(prefix));
        }
        assertParseFailure(parser, " c/0 d/12-12-2026 t/14:00 pn/Milo", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " c/1 d/31-02-2026 t/14:00 pn/Milo", AppointmentDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " c/1 d/12-12-2026 t/24:00 pn/Milo", AppointmentTime.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " c/1 d/12-12-2026 t/14:00 et/14:00 pn/Milo",
                Appointment.MESSAGE_END_NOT_AFTER_START);
    }

    @Test
    public void parse_repeatedEndTime_failsEvenWithIdenticalOrEmptyValues() {
        for (String endTime : new String[] {"13:00", "14:00", ""}) {
            assertParseFailure(parser, " c/1 d/12-12-2026 t/12:00 et/13:00 et/" + endTime,
                    Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_END_TIME));
        }
    }

    @Test
    public void parse_invalidEndTime_fails() {
        for (String endTime : new String[] {"", "24:00", "12:60", "1:00", "13:00:00", "noon", "13:00 PM"}) {
            assertParseFailure(parser, " c/1 d/12-12-2026 t/12:00 et/" + endTime,
                    AppointmentTime.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_endNotAfterStart_fails() {
        for (String endTime : new String[] {"12:00", "11:59", "00:00"}) {
            assertParseFailure(parser, " c/1 d/12-12-2026 t/12:00 et/" + endTime,
                    Appointment.MESSAGE_END_NOT_AFTER_START);
        }
        assertParseFailure(parser, " c/1 d/12-12-2026 t/23:00 et/01:00",
                Appointment.MESSAGE_END_NOT_AFTER_START);
    }

    @Test
    public void parse_endTimePresent_preservesRequiredFieldValidation() {
        for (String input : new String[] {" et/13:00", " c/1 t/12:00 et/13:00",
            " c/1 d/12-12-2026 et/13:00", " d/12-12-2026 t/12:00 et/13:00",
            " extra c/1 d/12-12-2026 t/12:00 et/13:00"}) {
            assertParseFailure(parser, input,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AppointmentCommand.MESSAGE_USAGE));
        }
        assertParseFailure(parser, " c/0 d/12-12-2026 t/12:00 et/13:00", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " c/1 d/31-02-2026 t/12:00 et/13:00", AppointmentDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " c/1 d/12-12-2026 t/24:00 et/13:00", AppointmentTime.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " c/1 d/12-12-2026 t/12:00 t/12:00 et/13:00",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_TIME));
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
