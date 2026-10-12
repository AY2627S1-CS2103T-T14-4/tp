package seedu.address.model.client;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.exceptions.DuplicateAppointmentException;
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
    private final List<Appointment> appointments;
    private final List<Pet> pets;

    /**
     * Creates a client with no pets or appointments.
     */
    public Client(Name name, Phone phone, Email email,
            Address address, Set<Tag> tags) {
        this(name, phone, email, address, tags, List.of(), List.of());
    }

    /**
     * Creates a client with appointments but no pets.
     */
    public Client(Name name, Phone phone, Email email,
            Address address, Set<Tag> tags,
            List<Appointment> appointments) {
        this(name, phone, email, address, tags, List.of(), appointments);
    }

    /**
     * Creates a client with pets and appointments.
     */
    public Client(Name name, Phone phone, Email email,
            Address address, Set<Tag> tags,
            List<Pet> pets, List<Appointment> appointments) {

        requireAllNonNull(name, phone, email, address, tags,
                pets, appointments);

        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);

        this.pets = List.copyOf(pets);
        this.appointments = List.copyOf(appointments);

        ensurePetsAreUnique(this.pets);
        ensureAppointmentsAreUnique(this.appointments);
    }

    private static void ensurePetsAreUnique(List<Pet> pets) {
        for (int i = 0; i < pets.size(); i++) {
            for (int j = i + 1; j < pets.size(); j++) {
                if (pets.get(i).isSamePet(pets.get(j))) {
                    throw new DuplicatePetException();
                }
            }
        }
    }

    /**
     * Checks that no two appointments have the same date and time.
     *
     * @throws DuplicateAppointmentException if duplicate date/time values are found.
     */
    private static void ensureAppointmentsAreUnique(List<Appointment> appointments) {
        for (int i = 0; i < appointments.size(); i++) {
            for (int j = i + 1; j < appointments.size(); j++) {
                if (appointments.get(i).hasSameDateTime(appointments.get(j))) {
                    throw new DuplicateAppointmentException();
                }
            }
        }
    }

    /**
     * Returns this client's appointments as an immutable list.
     */
    public List<Appointment> getAppointments() {
        return appointments;
    }

    /**
     * Returns true if this client has an appointment with the same date and time.
     */
    public boolean hasAppointment(Appointment appointment) {
        requireAllNonNull(appointment);
        return appointments.stream().anyMatch(appointment::hasSameDateTime);
    }

    /**
     * Returns a copy of this client with the appointment added,
     * leaving this client unchanged.
     *
     * @throws DuplicateAppointmentException if the date and time
     *         already exist for this client.
     */
    public Client copyWithAppointment(Appointment appointment) {
        requireAllNonNull(appointment);

        if (hasAppointment(appointment)) {
            throw new DuplicateAppointmentException();
        }

        List<Appointment> updatedAppointments = new ArrayList<>(appointments);
        updatedAppointments.add(appointment);

        return new Client(name, phone, email, address, tags,
                pets, updatedAppointments);
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
        return new Client(name, phone, email, address, tags,
        updatedPets, appointments);
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
                && pets.equals(otherClient.pets)
                && appointments.equals(otherClient.appointments);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, tags,
        pets, appointments);
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
                .add("appointments", appointments)
                .toString();
    }

}
