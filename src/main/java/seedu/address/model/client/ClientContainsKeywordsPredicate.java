package seedu.address.model.client;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Client}'s name, phone number, or email matches any of the keywords given.
 */
public class ClientContainsKeywordsPredicate implements Predicate<Client> {
    private final List<String> keywords;

    public ClientContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean test(Client client) {
        return keywords.stream().anyMatch(keyword ->
                StringUtil.containsWordIgnoreCase(client.getName().fullName, keyword)
                        || client.getPhone().startsWith(keyword)
                        || client.getEmail().value.equalsIgnoreCase(keyword));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ClientContainsKeywordsPredicate otherClientContainsKeywordsPredicate)) {
            return false;
        }

        return keywords.equals(otherClientContainsKeywordsPredicate.keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
