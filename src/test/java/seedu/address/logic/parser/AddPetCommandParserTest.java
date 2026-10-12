package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BREED;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLIENT_INDEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SPECIES;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddPetCommand;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;

public class AddPetCommandParserTest {

    private final AddPetCommandParser parser = new AddPetCommandParser();

    @Test
    public void parse_validArgs_returnsAddPetCommand() {
        Pet pet = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));

        assertParseSuccess(parser, "c/1 n/Milo s/Dog b/Poodle",
                new AddPetCommand(INDEX_FIRST_CLIENT, pet));
    }

    @Test
    public void parse_missingField_throwsParseException() {
        assertParseFailure(parser, "c/1 n/Milo s/Dog",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddPetCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertParseFailure(parser, "c/zero n/Milo s/Dog b/Poodle",
                ParserUtil.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_invalidPetName_throwsParseException() {
        assertParseFailure(parser, "c/1 n/Milo! s/Dog b/Poodle",
                PetName.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidSpecies_throwsParseException() {
        assertParseFailure(parser, "c/1 n/Milo s/D@g b/Poodle",
                Species.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidBreed_throwsParseException() {
        assertParseFailure(parser, "c/1 n/Milo s/Dog b/Poodle!",
                Breed.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedPrefix_throwsParseException() {
        assertParseFailure(parser, "c/1 c/2 n/Milo s/Dog b/Poodle",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CLIENT_INDEX));

        assertParseFailure(parser, "c/1 n/Milo n/Luna s/Dog b/Poodle",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        assertParseFailure(parser, "c/1 n/Milo s/Dog s/Cat b/Poodle",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SPECIES));

        assertParseFailure(parser, "c/1 n/Milo s/Dog b/Poodle b/Persian",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_BREED));
    }
}