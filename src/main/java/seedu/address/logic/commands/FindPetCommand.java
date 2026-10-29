package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.ClientDisplayMode;
import seedu.address.model.Model;
import seedu.address.model.pet.PetContainsKeywordsPredicate;

/**
 * Finds and lists all clients who own pets matching any of the argument keywords.
 */
public class FindPetCommand extends Command {

    public static final String COMMAND_WORD = "findp";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds all clients with pets whose names match any "
            + "of the specified keywords (case-insensitive) and displays them as a list with index numbers.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]...\n"
            + "Example: " + COMMAND_WORD + " Milo";

    private final PetContainsKeywordsPredicate predicate;

    public FindPetCommand(PetContainsKeywordsPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        model.setClientDisplayMode(ClientDisplayMode.NORMAL);
        requireNonNull(model);
        model.updateFilteredClientList(predicate);
        long matchingPetCount = model.getPawPals().getClientList().stream()
                .mapToLong(predicate::getMatchingPetCount)
                .sum();
        return new CommandResult(String.format(Messages.MESSAGE_PETS_FOUND, matchingPetCount), FeedbackType.NEUTRAL);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof FindPetCommand otherFindPetCommand)) {
            return false;
        }

        return predicate.equals(otherFindPetCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("predicate", predicate).toString();
    }
}
