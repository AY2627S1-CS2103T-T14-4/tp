package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLIENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PET_NAME;

import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeletePetCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.pet.PetName;

/**
 * Parses arguments for the {@code delp} command.
 */
public class DeletePetCommandParser implements Parser<DeletePetCommand> {

    /**
     * Parses exactly one client index and one pet name, in any order, with no preamble.
     *
     * @throws ParseException if a prefix is missing or repeated, the preamble is not empty,
     *     or a value is invalid.
     */
    @Override
    public DeletePetCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_CLIENT, PREFIX_PET_NAME);
        if (!arguments.getPreamble().isEmpty() || Stream.of(PREFIX_CLIENT, PREFIX_PET_NAME)
                .anyMatch(prefix -> arguments.getValue(prefix).isEmpty())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeletePetCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CLIENT, PREFIX_PET_NAME);

        Index clientIndex = ParserUtil.parseIndex(arguments.getValue(PREFIX_CLIENT).get());
        PetName petName = ParserUtil.parsePetName(arguments.getValue(PREFIX_PET_NAME).get());
        return new DeletePetCommand(clientIndex, petName);
    }
}
