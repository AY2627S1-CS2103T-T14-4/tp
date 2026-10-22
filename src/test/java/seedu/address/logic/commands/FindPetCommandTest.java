package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_PETS_FOUND;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.UserPrefs;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetContainsKeywordsPredicate;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;
import seedu.address.testutil.ClientBuilder;

public class FindPetCommandTest {
    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
    private static final Pet LUNA = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));

    @Test
    public void execute_matchingOwnersAreDisplayed() {
        Client alice = new ClientBuilder().withName("Alice").withPets(MILO).build();
        Client bob = new ClientBuilder().withName("Bob").withPets(LUNA).build();
        PawPals pawPals = new PawPals();
        pawPals.addClient(alice);
        pawPals.addClient(bob);
        Model model = new ModelManager(pawPals, new UserPrefs());
        Model expectedModel = new ModelManager(pawPals, new UserPrefs());
        PetContainsKeywordsPredicate predicate = new PetContainsKeywordsPredicate(List.of("milo"));

        expectedModel.updateFilteredClientList(predicate);
        CommandResult result = new FindPetCommand(predicate).execute(model);

        assertEquals(String.format(MESSAGE_PETS_FOUND, 1), result.getFeedbackToUser());
        assertEquals(FeedbackType.NEUTRAL, result.getFeedbackType());
        assertEquals(List.of(alice), model.getFilteredClientList());
        assertEquals(expectedModel.getFilteredClientList(), model.getFilteredClientList());
    }

    @Test
    public void execute_multipleMatchingPetsAndOwners_displaysEachOwnerOnce() {
        Pet miloJr = new Pet(new PetName("Milo Jr"), new Species("Dog"), new Breed("Poodle"));
        Client alice = new ClientBuilder().withName("Alice").withPets(MILO, miloJr).build();
        Client bob = new ClientBuilder().withName("Bob").withPets(MILO).build();
        PawPals pawPals = new PawPals();
        pawPals.addClient(alice);
        pawPals.addClient(bob);
        Model model = new ModelManager(pawPals, new UserPrefs());
        PetContainsKeywordsPredicate predicate = new PetContainsKeywordsPredicate(List.of("Milo", "Milo Jr"));

        CommandResult result = new FindPetCommand(predicate).execute(model);

        assertEquals(String.format(MESSAGE_PETS_FOUND, 3), result.getFeedbackToUser());
        assertEquals(List.of(alice, bob), model.getFilteredClientList());
    }

    @Test
    public void execute_noMatchingPets_displaysNoOwners() {
        Client alice = new ClientBuilder().withName("Alice").withPets(MILO).build();
        PawPals pawPals = new PawPals();
        pawPals.addClient(alice);
        Model model = new ModelManager(pawPals, new UserPrefs());

        CommandResult result = new FindPetCommand(
                new PetContainsKeywordsPredicate(List.of("Coco"))).execute(model);

        assertEquals(String.format(MESSAGE_PETS_FOUND, 0), result.getFeedbackToUser());
        assertEquals(FeedbackType.NEUTRAL, result.getFeedbackType());
        assertTrue(model.getFilteredClientList().isEmpty());
    }

    @Test
    public void execute_doesNotModifyPawPalsData() {
        Client alice = new ClientBuilder().withName("Alice").withPets(MILO).build();
        PawPals pawPals = new PawPals();
        pawPals.addClient(alice);
        Model model = new ModelManager(pawPals, new UserPrefs());

        new FindPetCommand(new PetContainsKeywordsPredicate(List.of("Milo"))).execute(model);

        assertEquals(List.of(alice), model.getPawPals().getClientList());
    }

    @Test
    public void equals() {
        FindPetCommand first = new FindPetCommand(new PetContainsKeywordsPredicate(List.of("Milo")));
        FindPetCommand same = new FindPetCommand(new PetContainsKeywordsPredicate(List.of("Milo")));
        FindPetCommand different = new FindPetCommand(new PetContainsKeywordsPredicate(List.of("Luna")));

        assertTrue(first.equals(first));
        assertTrue(first.equals(same));
        assertFalse(first.equals(different));
        assertFalse(first.equals(null));
        assertFalse(first.equals(1));
    }
}
