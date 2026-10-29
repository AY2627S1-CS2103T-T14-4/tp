package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.ClientBuilder;

public class PetContainsKeywordsPredicateTest {

    @Test
    public void equals() {
        PetContainsKeywordsPredicate first = new PetContainsKeywordsPredicate(List.of("Milo"));
        PetContainsKeywordsPredicate same = new PetContainsKeywordsPredicate(List.of("Milo"));
        PetContainsKeywordsPredicate different = new PetContainsKeywordsPredicate(List.of("Luna"));

        assertTrue(first.equals(first));
        assertTrue(first.equals(same));
        assertFalse(first.equals(different));
        assertFalse(first.equals(null));
        assertFalse(first.equals(1));
    }

    @Test
    public void test_matchingPetNames_returnsTrue() {
        ClientBuilder clientBuilder = new ClientBuilder().withPets(
                new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle")),
                new Pet(new PetName("Sir Fluffy"), new Species("Cat"), new Breed("Persian")),
                new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian")));

        assertTrue(new PetContainsKeywordsPredicate(List.of("Milo")).test(clientBuilder.build()));
        assertTrue(new PetContainsKeywordsPredicate(List.of("mIlO")).test(clientBuilder.build()));
        assertTrue(new PetContainsKeywordsPredicate(List.of("Sir")).test(clientBuilder.build()));
        assertTrue(new PetContainsKeywordsPredicate(List.of("Fluffy")).test(clientBuilder.build()));
        assertTrue(new PetContainsKeywordsPredicate(List.of("Coco", "Luna")).test(clientBuilder.build()));
    }

    @Test
    public void test_nonMatchingPetNames_returnsFalse() {
        ClientBuilder clientBuilder = new ClientBuilder().withPets(
                new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle")));

        assertFalse(new PetContainsKeywordsPredicate(List.of()).test(clientBuilder.build()));
        assertFalse(new PetContainsKeywordsPredicate(List.of("Mil")).test(clientBuilder.build()));
        assertFalse(new PetContainsKeywordsPredicate(List.of("Fluf")).test(clientBuilder.build()));
        assertFalse(new PetContainsKeywordsPredicate(List.of("Coco")).test(clientBuilder.build()));
    }

    @Test
    public void getMatchingPetCount_returnsNumberOfMatchingPets() {
        ClientBuilder clientBuilder = new ClientBuilder().withPets(
                new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle")),
                new Pet(new PetName("Milo Jr"), new Species("Dog"), new Breed("Poodle")),
                new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian")));
        PetContainsKeywordsPredicate predicate = new PetContainsKeywordsPredicate(List.of("Milo", "Luna"));

        assertEquals(3, predicate.getMatchingPetCount(clientBuilder.build()));
    }

    @Test
    public void toStringMethod_returnsExpectedString() {
        PetContainsKeywordsPredicate predicate = new PetContainsKeywordsPredicate(List.of("Milo", "Luna"));

        assertEquals("PetContainsKeywordsPredicate{keywords=[Milo, Luna]}", predicate.toString());
    }
}
