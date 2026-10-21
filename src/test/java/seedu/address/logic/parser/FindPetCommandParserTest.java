package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindPetCommand;
import seedu.address.model.pet.PetContainsKeywordsPredicate;

public class FindPetCommandParserTest {
    private final FindPetCommandParser parser = new FindPetCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "  ", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                FindPetCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validArgs_returnsFindPetCommand() {
        FindPetCommand expected = new FindPetCommand(
                new PetContainsKeywordsPredicate(List.of("Milo", "Luna")));
        assertParseSuccess(parser, " Milo  Luna ", expected);
    }
}
