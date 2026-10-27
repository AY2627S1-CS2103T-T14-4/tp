package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BREED;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLIENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SPECIES;

import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddPetCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;

/**
 * Parses arguments for the {@code addp} command.
 */
public class AddPetCommandParser implements Parser<AddPetCommand> {

    /**
     * Parses exactly one client index, name, species and breed, in any order, with no preamble.
     *
     * @throws ParseException if a prefix is missing or repeated, the preamble is not empty,
     *     or a value is invalid.
     */
    @Override
    public AddPetCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(
                args, PREFIX_CLIENT, PREFIX_NAME, PREFIX_SPECIES, PREFIX_BREED);
        if (!arguments.getPreamble().isEmpty() || Stream.of(PREFIX_CLIENT, PREFIX_NAME, PREFIX_SPECIES, PREFIX_BREED)
                .anyMatch(prefix -> arguments.getValue(prefix).isEmpty())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddPetCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CLIENT, PREFIX_NAME, PREFIX_SPECIES, PREFIX_BREED);

        Index clientIndex = ParserUtil.parseIndex(arguments.getValue(PREFIX_CLIENT).get());
        PetName name = ParserUtil.parsePetName(arguments.getValue(PREFIX_NAME).get());
        Species species = ParserUtil.parseSpecies(arguments.getValue(PREFIX_SPECIES).get());
        Breed breed = ParserUtil.parseBreed(arguments.getValue(PREFIX_BREED).get());
        return new AddPetCommand(clientIndex, new Pet(name, species, breed));
    }
}
