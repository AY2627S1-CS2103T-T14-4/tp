package seedu.address.model.pet;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.model.client.Client;

/**
 * Tests whether a {@code Client} owns a pet whose name matches any keyword.
 */
public class PetContainsKeywordsPredicate implements Predicate<Client> {
    private final List<String> keywords;

    public PetContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean test(Client client) {
        return getMatchingPetCount(client) > 0;
    }

    /**
     * Returns the number of pets owned by {@code client} whose names contain a matching word.
     *
     * @param client the client whose pets are checked
     * @return the number of matching pets
     */
    public long getMatchingPetCount(Client client) {
        return client.getPets().stream()
                .filter(pet -> keywords.stream()
                        .anyMatch(keyword -> StringUtil.containsWordIgnoreCase(pet.getName().fullName, keyword)))
                .count();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof PetContainsKeywordsPredicate otherPredicate)) {
            return false;
        }

        return keywords.equals(otherPredicate.keywords);
    }

    @Override
    public String toString() {
        return "PetContainsKeywordsPredicate{" + "keywords=" + keywords + '}';
    }
}
