package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLIENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;

import java.util.stream.Stream;

import seedu.address.logic.commands.AppointmentCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.appointment.Appointment;

/**
 * Parses the client index, date and time for an appointment.
 */
public class AppointmentCommandParser implements Parser<AppointmentCommand> {
    /**
     * Parses exactly one client index, date and time in any order, with no preamble.
     * Validates date/time format here; the command checks whether it is in the future at execution.
     *
     * @param args The arguments following the {@code appt} command word.
     * @return A command targeting the supplied displayed client index.
     * @throws NullPointerException if {@code args} is null.
     * @throws ParseException if a required prefix is missing or repeated, the preamble is non-empty,
     *     or the client index, date or time is invalid.
     */
    @Override
    public AppointmentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME);
        if (!arguments.getPreamble().isEmpty() || Stream.of(PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME)
                .anyMatch(prefix -> arguments.getValue(prefix).isEmpty())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AppointmentCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME);
        ParserUtil.AppointmentTarget target = ParserUtil.parseAppointmentTarget(arguments);
        return new AppointmentCommand(target.clientIndex(), new Appointment(target.date(), target.time()));
    }
}
