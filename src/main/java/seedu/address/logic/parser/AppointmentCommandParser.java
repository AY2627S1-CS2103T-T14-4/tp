package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLIENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_END_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AppointmentCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;

/**
 * Parses the client index, date, start time and optional end time for an appointment.
 */
public class AppointmentCommandParser implements Parser<AppointmentCommand> {
    /**
     * Parses exactly one client index, date and start time, and at most one end time,
     * in any order, with no preamble.
     * Validates date/time format here; the command checks whether it is in the future at execution.
     *
     * @param args The arguments following the {@code appt} command word.
     * @return A command targeting the supplied displayed client index.
     * @throws NullPointerException if {@code args} is null.
     * @throws ParseException if a required prefix is missing or repeated, the preamble is non-empty,
     *     or the client index, date, start time or end time is invalid.
     */
    @Override
    public AppointmentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args,
                PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME, PREFIX_END_TIME);
        if (!arguments.getPreamble().isEmpty() || Stream.of(PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME)
                .anyMatch(prefix -> arguments.getValue(prefix).isEmpty())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AppointmentCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME, PREFIX_END_TIME);
        Index index = ParserUtil.parseIndex(arguments.getValue(PREFIX_CLIENT).get());
        try {
            AppointmentDate date = new AppointmentDate(arguments.getValue(PREFIX_DATE).get());
            AppointmentTime time = new AppointmentTime(arguments.getValue(PREFIX_TIME).get());
            Optional<AppointmentTime> endTime = arguments.getValue(PREFIX_END_TIME).map(AppointmentTime::new);
            return new AppointmentCommand(index, new Appointment(date, time, endTime, "", Set.of()));
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
