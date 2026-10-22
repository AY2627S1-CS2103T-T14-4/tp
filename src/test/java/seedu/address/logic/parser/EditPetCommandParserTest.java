package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditPetCommand;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;

public class EditPetCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditPetCommand.MESSAGE_USAGE);

    private final EditPetCommandParser parser = new EditPetCommandParser();

    @Test
    public void parse_someFields_returnsEditPetCommand() {
        assertParseSuccess(parser, " c/1 pn/Milo b/Toy Poodle", new EditPetCommand(INDEX_FIRST_CLIENT,
                new PetName("Milo"), Optional.empty(), Optional.empty(), Optional.of(new Breed("Toy Poodle"))));
        assertParseSuccess(parser, " n/Max pn/Milo c/1", new EditPetCommand(INDEX_FIRST_CLIENT,
                new PetName("Milo"), Optional.of(new PetName("Max")), Optional.empty(), Optional.empty()));
    }

    @Test
    public void parse_allFields_returnsEditPetCommand() {
        EditPetCommand expected = new EditPetCommand(INDEX_FIRST_CLIENT, new PetName("Milo"),
                Optional.of(new PetName("Max")), Optional.of(new Species("Cat")), Optional.of(new Breed("Persian")));

        assertParseSuccess(parser, " c/1 pn/Milo n/Max s/Cat b/Persian", expected);
    }

    @Test
    public void parse_noFieldToEdit_throwsParseException() {
        assertParseFailure(parser, " c/1 pn/Milo", EditPetCommand.MESSAGE_NOT_EDITED);
    }

    @Test
    public void parse_missingPrefixOrPreamble_throwsParseException() {
        assertParseFailure(parser, " pn/Milo b/Beagle", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " c/1 b/Beagle", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " extra c/1 pn/Milo b/Beagle", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_duplicatePrefix_throwsParseException() {
        assertParseFailure(parser, " c/1 pn/Milo b/Beagle b/Pug",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_BREED));
    }

    @Test
    public void parse_invalidValues_throwsParseException() {
        assertParseFailure(parser, " c/0 pn/Milo b/Beagle", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " c/1 pn/Milo! b/Beagle", PetName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " c/1 pn/Milo n/Max!", PetName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " c/1 pn/Milo s/", Species.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " c/1 pn/Milo b/Poodle!", Breed.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
