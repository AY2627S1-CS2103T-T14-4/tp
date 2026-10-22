package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BREED;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLIENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PET_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SPECIES;

import java.util.Optional;
import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditPetCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;

/**
 * Parses arguments for the {@code editp} command.
 */
public class EditPetCommandParser implements Parser<EditPetCommand> {

    /**
     * Parses a client index, the pet's current name and at least one of a new name, species or breed.
     * Prefixes may appear in any order, each at most once, with no preamble.
     *
     * @throws ParseException if a required prefix is missing, a prefix is repeated, the preamble is not empty,
     *     no field to edit is given, or a value is invalid.
     */
    @Override
    public EditPetCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(
                args, PREFIX_CLIENT, PREFIX_PET_NAME, PREFIX_NAME, PREFIX_SPECIES, PREFIX_BREED);
        if (!arguments.getPreamble().isEmpty() || Stream.of(PREFIX_CLIENT, PREFIX_PET_NAME)
                .anyMatch(prefix -> arguments.getValue(prefix).isEmpty())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditPetCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CLIENT, PREFIX_PET_NAME, PREFIX_NAME, PREFIX_SPECIES,
                PREFIX_BREED);

        Index clientIndex = ParserUtil.parseIndex(arguments.getValue(PREFIX_CLIENT).get());
        PetName petName = ParserUtil.parsePetName(arguments.getValue(PREFIX_PET_NAME).get());
        Optional<PetName> newName = parseOptional(arguments, PREFIX_NAME, ParserUtil::parsePetName);
        Optional<Species> newSpecies = parseOptional(arguments, PREFIX_SPECIES, ParserUtil::parseSpecies);
        Optional<Breed> newBreed = parseOptional(arguments, PREFIX_BREED, ParserUtil::parseBreed);
        if (newName.isEmpty() && newSpecies.isEmpty() && newBreed.isEmpty()) {
            throw new ParseException(EditPetCommand.MESSAGE_NOT_EDITED);
        }
        return new EditPetCommand(clientIndex, petName, newName, newSpecies, newBreed);
    }

    /**
     * Parses the value of {@code prefix} with {@code parser} if the prefix is present.
     */
    private static <T> Optional<T> parseOptional(ArgumentMultimap arguments, Prefix prefix,
            ValueParser<T> parser) throws ParseException {
        Optional<String> value = arguments.getValue(prefix);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(parser.parse(value.get()));
    }

    /**
     * Parses one field value, possibly throwing a {@code ParseException}.
     */
    @FunctionalInterface
    private interface ValueParser<T> {
        T parse(String value) throws ParseException;
    }
}
