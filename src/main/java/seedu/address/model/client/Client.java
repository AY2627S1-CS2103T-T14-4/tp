package seedu.address.model.client;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.exceptions.DuplicatePetException;
import seedu.address.model.tag.Tag;

/**
 * Represents a Client in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Client {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address;
    private final Set<Tag> tags = new HashSet<>();
    private final List<Pet> pets;

    /**
     * Creates a client with no pets.
     *
     * @param name the client's name
     * @param phone the client's phone number
     * @param email the client's email address
     * @param address the client's address
     * @param tags the client's tags
     */
    public Client(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(name, phone, email, address, tags, List.of());
    }

    /**
     * Creates a client with the specified details and pets.
     *
     * @param name the client's name
     * @param phone the client's phone number
     * @param email the client's email address
     * @param address the client's address
     * @param tags the client's tags
     * @param pets the pets belonging to this client
     * @throws DuplicatePetException if the pet list contains duplicate pet identities
     */
    public Client(Name name, Phone phone, Email email, Address address, Set<Tag> tags, List<Pet> pets) {
        requireAllNonNull(name, phone, email, address, tags, pets);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);
        this.pets = List.copyOf(pets);
        ensurePetsAreUnique(this.pets);
    }

    /**
     * Checks that no two pets in the list have the same identity.
     *
     * @param pets the pets to check
     * @throws DuplicatePetException if duplicate pet identities are found
     */
    private static void ensurePetsAreUnique(List<Pet> pets) {
        for (int i = 0; i < pets.size(); i++) {
            for (int j = i + 1; j < pets.size(); j++) {
                if (pets.get(i).isSamePet(pets.get(j))) {
                    throw new DuplicatePetException();
                }
            }
        }
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns this client's pets as an immutable list.
     *
     * @return an immutable list of this client's pets
     */
    public List<Pet> getPets() {
        return pets;
    }

    /**
     * Checks whether this client already has a pet with the same identity as {@code pet}.
     *
     * @param pet the pet to check
     * @return true if this client already has an equivalent pet
     */
    public boolean hasPet(Pet pet) {
        requireAllNonNull(pet);
        return pets.stream().anyMatch(existingPet -> existingPet.isSamePet(pet));
    }

    /**
     * Returns a new client with {@code pet} added, leaving this client unchanged.
     *
     * @param pet the pet to add
     * @return a new client containing the pet
     * @throws DuplicatePetException if this client already has a pet with the same identity
     */
    public Client withPet(Pet pet) {
        requireAllNonNull(pet);
        if (hasPet(pet)) {
            throw new DuplicatePetException();
        }

        List<Pet> updatedPets = new ArrayList<>(pets);
        updatedPets.add(pet);
        return new Client(name, phone, email, address, tags, updatedPets);
    }

    /**
     * Returns true if both clients have the same normalized name and phone number.
     * This defines a weaker notion of equality between two clients.
     */
    public boolean isSameClient(Client otherClient) {
        if (otherClient == this) {
            return true;
        }

        return otherClient != null
                && otherClient.getName().normalized().equals(getName().normalized())
                && otherClient.getPhone().equals(getPhone());
    }

    /**
     * Returns true if both clients have the same identity and data fields,
     * including their pets. This defines a stronger notion of equality between
     * two clients.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Client otherClient)) {
            return false;
        }

        return name.equals(otherClient.name)
                && phone.equals(otherClient.phone)
                && email.equals(otherClient.email)
                && address.equals(otherClient.address)
                && tags.equals(otherClient.tags)
                && pets.equals(otherClient.pets);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, tags, pets);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("tags", tags)
                .add("pets", pets)
                .toString();
    }

}
