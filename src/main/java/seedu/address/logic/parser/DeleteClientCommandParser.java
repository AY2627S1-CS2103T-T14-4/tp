package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLIENT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteClientCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteClientCommand object.
 */
public class DeleteClientCommandParser implements Parser<DeleteClientCommand> {

    /**
     * Parses exactly one client index with the {@code c/} prefix and no preamble.
     *
     * @throws ParseException if the prefix is missing or repeated, the preamble is not empty,
     *     or the index is invalid.
     */
    @Override
    public DeleteClientCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_CLIENT);
        if (!arguments.getPreamble().isEmpty() || arguments.getValue(PREFIX_CLIENT).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteClientCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CLIENT);

        Index index = ParserUtil.parseIndex(arguments.getValue(PREFIX_CLIENT).get());
        return new DeleteClientCommand(index);
    }
}
