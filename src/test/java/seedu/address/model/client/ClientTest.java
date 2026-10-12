package seedu.address.model.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;
import seedu.address.model.pet.exceptions.DuplicatePetException;
import seedu.address.testutil.ClientBuilder;

public class ClientTest {

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Client client = new ClientBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> client.getTags().remove(0));
    }

    @Test
    public void withPet_addsPetWithoutChangingOriginal() {
        Client original = new ClientBuilder().build();
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));

        Client updated = original.withPet(milo);

        assertTrue(original.getPets().isEmpty());
        assertEquals(List.of(milo), updated.getPets());
    }

    @Test
    public void getPets_modifyList_throwsUnsupportedOperationException() {
        Client client = new ClientBuilder().build().withPet(
                new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle")));

        assertThrows(UnsupportedOperationException.class, () -> client.getPets().clear());
    }

    @Test
    public void withPet_duplicateNameForSameClient_throwsDuplicatePetException() {
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
        Pet sameName = new Pet(new PetName("mILO"), new Species("Cat"), new Breed("Persian"));
        Client client = new ClientBuilder().build().withPet(milo);

        assertThrows(DuplicatePetException.class, () -> client.withPet(sameName));
    }

    @Test
    public void withPet_sameNameForDifferentClients_succeeds() {
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));

        assertEquals(List.of(milo), new ClientBuilder(ALICE).build().withPet(milo).getPets());
        assertEquals(List.of(milo), new ClientBuilder(BOB).build().withPet(milo).getPets());
    }

    @Test
    public void withPet_addsMultipleDistinctPets() {
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
        Pet luna = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));

        Client client = new ClientBuilder(ALICE).build()
                .withPet(milo)
                .withPet(luna);

        assertEquals(List.of(milo, luna), client.getPets());
    }

    @Test
    public void constructor_duplicatePets_throwsDuplicatePetException() {
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
        Pet sameName = new Pet(new PetName("mILO"), new Species("Cat"), new Breed("Persian"));

        assertThrows(DuplicatePetException.class, () ->
                new Client(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(),
                        ALICE.getTags(), List.of(milo, sameName)));
    }

    @Test
    public void isSameClient() {
        // same object -> returns true
        assertTrue(ALICE.isSameClient(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSameClient(null));

        // same name, different phone -> returns false
        Client editedAlice = new ClientBuilder(ALICE).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.isSameClient(editedAlice));

        editedAlice = new ClientBuilder(ALICE).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSameClient(editedAlice));

        // different name, all other attributes same -> returns false
        editedAlice = new ClientBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSameClient(editedAlice));

        // name differs in case, same phone -> returns true
        Client editedBob = new ClientBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertTrue(BOB.isSameClient(editedBob));

        // name has repeated/trailing spaces, same phone -> returns true
        String nameWithTrailingSpaces = VALID_NAME_BOB.replace(" ", "  ") + " ";
        editedBob = new ClientBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertTrue(BOB.isSameClient(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Client aliceCopy = new ClientBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different client -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Client editedAlice = new ClientBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new ClientBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new ClientBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new ClientBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new ClientBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));

        // different pets -> returns false
        editedAlice = ALICE.withPet(new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle")));
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void hashCode_equalClientsWithSamePets_returnsSameHashCode() {
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
        Client clientWithPet = new ClientBuilder(ALICE).build().withPet(milo);
        Client equalClientWithPet = new ClientBuilder(clientWithPet).build();

        assertEquals(clientWithPet, equalClientWithPet);
        assertEquals(clientWithPet.hashCode(), equalClientWithPet.hashCode());
    }

    @Test
    public void toStringMethod() {
        String expected = Client.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", address=" + ALICE.getAddress() + ", tags=" + ALICE.getTags()
                + ", pets=" + ALICE.getPets() + "}";
        assertEquals(expected, ALICE.toString());
    }
}
