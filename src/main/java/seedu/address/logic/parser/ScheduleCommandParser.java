package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLIENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ScheduleCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.appointment.AppointmentDate;

/** Parses input arguments and creates a {@link ScheduleCommand}. */
public class ScheduleCommandParser implements Parser<ScheduleCommand> {
    @Override
    public ScheduleCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_CLIENT, PREFIX_DATE);
        boolean hasClient = arguments.getValue(PREFIX_CLIENT).isPresent();
        boolean hasDate = arguments.getValue(PREFIX_DATE).isPresent();
        if (!arguments.getPreamble().isEmpty() || hasClient == hasDate) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ScheduleCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CLIENT, PREFIX_DATE);
        if (hasClient) {
            Index index = ParserUtil.parseIndex(arguments.getValue(PREFIX_CLIENT).get());
            return new ScheduleCommand(index);
        }
        try {
            return new ScheduleCommand(new AppointmentDate(arguments.getValue(PREFIX_DATE).get()));
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
