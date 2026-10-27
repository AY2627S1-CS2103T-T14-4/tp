package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLIENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;

import java.util.stream.Stream;

import seedu.address.logic.commands.DoneCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.history.SessionNotes;

/**
 * Parses a completion target and required session notes, leaving completion eligibility to the command.
 */
public class DoneCommandParser implements Parser<DoneCommand> {
    /**
     * Parses exactly one client index, date, start time and session-notes value, with no preamble.
     * When the notes follow all target fields, prefix-like text such as "d/c" remains part of the notes.
     *
     * @throws NullPointerException if {@code args} is null.
     * @throws ParseException if required prefixes are missing or repeated, the preamble is non-empty,
     *     or a target value or session notes are invalid.
     */
    @Override
    public DoneCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenizeWithTrailingText(args,
                PREFIX_NOTE, PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME);
        if (!arguments.getPreamble().isEmpty() || Stream.of(PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME, PREFIX_NOTE)
                .anyMatch(prefix -> arguments.getValue(prefix).isEmpty())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DoneCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME, PREFIX_NOTE);
        ParserUtil.AppointmentTarget target = ParserUtil.parseAppointmentTarget(arguments);
        try {
            SessionNotes notes = new SessionNotes(arguments.getValue(PREFIX_NOTE).get());
            return new DoneCommand(target.clientIndex(), target.date(), target.time(), notes);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
