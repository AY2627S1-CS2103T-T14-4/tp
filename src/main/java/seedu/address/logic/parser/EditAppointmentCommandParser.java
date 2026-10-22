package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLIENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NEW_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NEW_END_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NEW_START_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;

import java.util.Optional;
import java.util.stream.Stream;

import seedu.address.logic.commands.EditAppointmentCommand;
import seedu.address.logic.commands.EditAppointmentCommand.EditAppointmentDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;

/**
 * Parses the original appointment's client/date/start and optional replacement date/start/end values.
 */
public class EditAppointmentCommandParser implements Parser<EditAppointmentCommand> {
    /**
     * Parses exactly one client index, original date and start time, and at least one replacement value.
     * Prefixes may appear in any order; duplicates and non-empty preambles are rejected.
     * Eligibility and end-time ordering are checked by the command against the original booking at execution.
     *
     * @throws NullPointerException if {@code args} is null.
     * @throws ParseException if required prefixes are missing, syntax or values are invalid, or no edit is supplied.
     */
    @Override
    public EditAppointmentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME,
                PREFIX_NEW_DATE, PREFIX_NEW_START_TIME, PREFIX_NEW_END_TIME);
        if (!arguments.getPreamble().isEmpty() || Stream.of(PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME)
                .anyMatch(prefix -> arguments.getValue(prefix).isEmpty())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    EditAppointmentCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME,
                PREFIX_NEW_DATE, PREFIX_NEW_START_TIME, PREFIX_NEW_END_TIME);
        ParserUtil.AppointmentTarget target = ParserUtil.parseAppointmentTarget(arguments);
        try {
            Optional<AppointmentDate> newDate = arguments.getValue(PREFIX_NEW_DATE).map(AppointmentDate::new);
            Optional<AppointmentTime> newStart = arguments.getValue(PREFIX_NEW_START_TIME).map(AppointmentTime::new);
            Optional<AppointmentTime> newEnd = arguments.getValue(PREFIX_NEW_END_TIME).map(AppointmentTime::new);
            EditAppointmentDescriptor edits = new EditAppointmentDescriptor(newDate, newStart, newEnd);
            return new EditAppointmentCommand(target.clientIndex(), target.date(), target.time(), edits);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
