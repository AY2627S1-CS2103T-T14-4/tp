package seedu.address.model.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.ClientBuilder;

public class ClientContainsKeywordsPredicateTest {

    @Test
    public void equals() {
        List<String> firstPredicateKeywordList = List.of("first");
        List<String> secondPredicateKeywordList = List.of("first", "second");

        ClientContainsKeywordsPredicate firstPredicate =
                new ClientContainsKeywordsPredicate(firstPredicateKeywordList);
        ClientContainsKeywordsPredicate secondPredicate =
                new ClientContainsKeywordsPredicate(secondPredicateKeywordList);

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        ClientContainsKeywordsPredicate firstPredicateCopy =
                new ClientContainsKeywordsPredicate(firstPredicateKeywordList);
        assertTrue(firstPredicate.equals(firstPredicateCopy));

        // different types -> returns false
        assertFalse(firstPredicate.equals(1));

        // null -> returns false
        assertFalse(firstPredicate.equals(null));

        // different client -> returns false
        assertFalse(firstPredicate.equals(secondPredicate));
    }

    @Test
    public void test_clientDetailsContainKeywords_returnsTrue() {
        // One keyword
        ClientContainsKeywordsPredicate predicate = new ClientContainsKeywordsPredicate(List.of("Alice"));
        assertTrue(predicate.test(new ClientBuilder().withName("Alice Bob").build()));

        // Multiple keywords
        predicate = new ClientContainsKeywordsPredicate(List.of("Alice", "Bob"));
        assertTrue(predicate.test(new ClientBuilder().withName("Alice Bob").build()));

        // Only one matching keyword
        predicate = new ClientContainsKeywordsPredicate(List.of("Bob", "Carol"));
        assertTrue(predicate.test(new ClientBuilder().withName("Alice Carol").build()));

        // Mixed-case keywords
        predicate = new ClientContainsKeywordsPredicate(List.of("aLIce", "bOB"));
        assertTrue(predicate.test(new ClientBuilder().withName("Alice Bob").build()));

        Client client = new ClientBuilder().withName("Alice Bob").withPhone("91234567")
                .withEmail("alice@example.com").build();
        assertTrue(new ClientContainsKeywordsPredicate(List.of("91234567")).test(client));
        assertTrue(new ClientContainsKeywordsPredicate(List.of("ALICE@EXAMPLE.COM")).test(client));
    }

    @Test
    public void test_clientDetailsDoNotContainKeywords_returnsFalse() {
        // Zero keywords
        ClientContainsKeywordsPredicate predicate = new ClientContainsKeywordsPredicate(List.of());
        assertFalse(predicate.test(new ClientBuilder().withName("Alice").build()));

        // Non-matching keyword
        predicate = new ClientContainsKeywordsPredicate(List.of("Carol"));
        assertFalse(predicate.test(new ClientBuilder().withName("Alice Bob").build()));

        Client client = new ClientBuilder().withName("Alice").withPhone("12345678")
                .withEmail("alice@email.com").withAddress("Main Street").build();

        // Partial phone, partial email, and address keywords do not match.
        predicate = new ClientContainsKeywordsPredicate(List.of("12345", "example.com", "Main", "Street"));
        assertFalse(predicate.test(client));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        ClientContainsKeywordsPredicate predicate = new ClientContainsKeywordsPredicate(keywords);

        String expected = ClientContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}
