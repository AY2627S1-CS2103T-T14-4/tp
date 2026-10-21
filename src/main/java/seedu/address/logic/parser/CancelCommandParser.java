package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLIENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;

import java.util.stream.Stream;

import seedu.address.logic.commands.CancelCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the client index, date and start time identifying an appointment to cancel.
 */
public class CancelCommandParser implements Parser<CancelCommand> {
    /**
     * Parses exactly one client index, date and start time in any order, with no preamble.
     * Dates are validated without requiring the appointment to be in the future.
     *
     * @throws NullPointerException if {@code args} is null.
     * @throws ParseException if a required prefix is missing or repeated, the preamble is non-empty,
     *     or the index, date or time is invalid.
     */
    @Override
    public CancelCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME);
        if (!arguments.getPreamble().isEmpty() || Stream.of(PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME)
                .anyMatch(prefix -> arguments.getValue(prefix).isEmpty())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, CancelCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME);
        ParserUtil.AppointmentTarget target = ParserUtil.parseAppointmentTarget(arguments);
        return new CancelCommand(target.clientIndex(), target.date(), target.time());
    }
}
