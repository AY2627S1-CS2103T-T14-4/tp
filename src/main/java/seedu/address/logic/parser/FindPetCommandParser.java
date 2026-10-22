package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import seedu.address.logic.commands.FindPetCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.pet.PetContainsKeywordsPredicate;

/**
 * Parses arguments for the {@code findp} command.
 */
public class FindPetCommandParser implements Parser<FindPetCommand> {

    @Override
    public FindPetCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindPetCommand.MESSAGE_USAGE));
        }

        String[] keywords = trimmedArgs.split("\\s+");
        return new FindPetCommand(new PetContainsKeywordsPredicate(List.of(keywords)));
    }
}
