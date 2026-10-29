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
 * Parses arguments and creates an {@code AddPetCommand}.
 */
public class AddPetCommandParser implements Parser<AddPetCommand> {

    /**
     * Parses a client index, a pet name, a species and a breed into an {@code AddPetCommand}.
     * Each prefix must appear exactly once, with no text before the first prefix.
     *
     * @throws ParseException if a prefix is missing or repeated, the preamble is not empty, or a value is invalid.
     */
    @Override
    public AddPetCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(
                args, PREFIX_CLIENT, PREFIX_NAME, PREFIX_SPECIES, PREFIX_BREED);

        if (!arePrefixesPresent(argMultimap, PREFIX_CLIENT, PREFIX_NAME, PREFIX_SPECIES, PREFIX_BREED)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddPetCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(
                PREFIX_CLIENT, PREFIX_NAME, PREFIX_SPECIES, PREFIX_BREED);

        Index clientIndex = ParserUtil.parseIndex(argMultimap.getValue(PREFIX_CLIENT).get());
        PetName name = ParserUtil.parsePetName(argMultimap.getValue(PREFIX_NAME).get());
        Species species = ParserUtil.parseSpecies(argMultimap.getValue(PREFIX_SPECIES).get());
        Breed breed = ParserUtil.parseBreed(argMultimap.getValue(PREFIX_BREED).get());

        return new AddPetCommand(clientIndex, new Pet(name, species, breed));
    }

    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}
