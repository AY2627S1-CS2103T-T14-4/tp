package seedu.address.model.client;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.appointment.exceptions.AppointmentNotFoundException;
import seedu.address.model.appointment.exceptions.DuplicateAppointmentException;
import seedu.address.model.history.CompletedSession;
import seedu.address.model.history.SessionNotes;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.exceptions.DuplicatePetException;
import seedu.address.model.pet.exceptions.PetNotFoundException;
import seedu.address.model.tag.Tag;

/**
 * Represents a Client in the PawPals data.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Client {

    public static final String MESSAGE_APPOINTMENT_NOT_STARTED =
            "An appointment cannot be completed before its scheduled start time.";

    private static final Logger logger = LogsCenter.getLogger(Client.class);

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address;
    private final Set<Tag> tags = new HashSet<>();
    private final List<Appointment> appointments;
    private final List<Pet> pets;
    private final List<CompletedSession> history;

    /**
     * Creates a client with no pets or appointments, delegating validation to the full constructor.
     *
     * @param name The client's name.
     * @param phone The client's phone number.
     * @param email The client's email address.
     * @param address The client's address.
     * @param tags The client's tags.
     * @throws NullPointerException if any argument is null.
     */
    public Client(Name name, Phone phone, Email email,
            Address address, Set<Tag> tags) {
        this(name, phone, email, address, tags, List.of(), List.of());
    }

    /**
     * Creates a client with appointments but no pets, delegating validation to the full constructor.
     *
     * @param name The client's name.
     * @param phone The client's phone number.
     * @param email The client's email address.
     * @param address The client's address.
     * @param tags The client's tags.
     * @param appointments The appointments belonging to this client.
     * @throws NullPointerException if any argument or appointment is null.
     * @throws DuplicateAppointmentException if appointments share a date and time.
     */
    public Client(Name name, Phone phone, Email email,
            Address address, Set<Tag> tags,
            List<Appointment> appointments) {
        this(name, phone, email, address, tags, List.of(), appointments);
    }

    /**
     * Creates a client with defensively copied pets and appointments.
     *
     * @param name The client's name.
     * @param phone The client's phone number.
     * @param email The client's email address.
     * @param address The client's address.
     * @param tags The client's tags.
     * @param pets The pets belonging to this client.
     * @param appointments The appointments belonging to this client.
     * @throws NullPointerException if any argument, pet, or appointment is null.
     * @throws DuplicatePetException if pets share an identity.
     * @throws DuplicateAppointmentException if appointments share a date and time.
     */
    public Client(Name name, Phone phone, Email email,
            Address address, Set<Tag> tags,
            List<Pet> pets, List<Appointment> appointments) {
        this(name, phone, email, address, tags, pets, appointments, List.of());
    }

    /**
     * Creates a client with defensively copied pets, active appointments and completed-session history.
     * History is kept newest appointment first, with logging time breaking ties. It has no slot-uniqueness
     * constraint and does not participate in active-appointment validation.
     *
     * @throws NullPointerException if any argument or collection entry is null.
     * @throws DuplicatePetException if pets share an identity.
     * @throws DuplicateAppointmentException if active appointments share a date and start time.
     */
    public Client(Name name, Phone phone, Email email,
            Address address, Set<Tag> tags,
            List<Pet> pets, List<Appointment> appointments, List<CompletedSession> history) {

        requireAllNonNull(name, phone, email, address, tags,
                pets, appointments, history);

        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);

        this.pets = List.copyOf(pets);
        this.appointments = List.copyOf(appointments);
        this.history = List.copyOf(history).stream().sorted(CompletedSession.NEWEST_FIRST).toList();

        ensurePetsAreUnique(this.pets);
        ensureAppointmentsAreUnique(this.appointments);
    }

    /**
     * Ensures that no two pets have the same name, ignoring case.
     *
     * @param pets the pets to check
     * @throws DuplicatePetException if duplicate pet names are found
     */
    private static void ensurePetsAreUnique(List<Pet> pets) {
        Set<String> petNames = new HashSet<>();

        for (Pet pet : pets) {
            String normalizedName = pet.getIdentityKey();

            if (!petNames.add(normalizedName)) {
                throw new DuplicatePetException();
            }
        }
    }

    /**
     * Ensures that no two appointments have the same date and time,
     * regardless of their details.
     *
     * @param appointments the appointments to check
     * @throws DuplicateAppointmentException if duplicate date/time values are found
     */
    private static void ensureAppointmentsAreUnique(List<Appointment> appointments) {
        Set<LocalDateTime> appointmentSlots = new HashSet<>();

        for (Appointment appointment : appointments) {
            LocalDateTime slot = LocalDateTime.of(
                    appointment.getDate().getValue(),
                    appointment.getTime().getValue());

            if (!appointmentSlots.add(slot)) {
                throw new DuplicateAppointmentException();
            }
        }
    }

    /**
     * Returns this client's appointments as an immutable list.
     *
     * @return The appointments belonging to this client, in insertion order.
     */
    public List<Appointment> getAppointments() {
        return appointments;
    }

    /**
     * Returns all completed sessions, newest appointment first, without truncating older records.
     */
    public List<CompletedSession> getHistory() {
        return history;
    }

    /**
     * Returns whether history contains a session at the given date and start time.
     * An active appointment at the same slot must still be looked up first when completing it.
     */
    public boolean hasCompletedAppointment(AppointmentDate date, AppointmentTime time) {
        requireAllNonNull(date, time);
        return history.stream().anyMatch(session -> session.getAppointment().getDate().equals(date)
                && session.getAppointment().getTime().equals(time));
    }

    /**
     * Returns a new client with an active appointment moved into completed-session history in one update.
     * The supplied clock determines both completion eligibility in its local zone and the logging instant.
     * An appointment may be completed at or after its start, even before its planned end.
     * Historical pet names are captured from the appointment, including an empty list for client-only visits.
     *
     * @throws NullPointerException if any argument is null.
     * @throws AppointmentNotFoundException if no active appointment exists at the requested slot.
     * @throws IllegalArgumentException if the appointment has not started.
     */
    public Client completeAppointment(AppointmentDate date, AppointmentTime time, SessionNotes notes, Clock clock) {
        requireAllNonNull(date, time, notes, clock);
        Appointment appointment = getAppointment(date, time).orElseThrow(AppointmentNotFoundException::new);
        Instant loggedAt = clock.instant();
        LocalDateTime start = LocalDateTime.of(date.getValue(), time.getValue());
        if (start.isAfter(LocalDateTime.ofInstant(loggedAt, clock.getZone()))) {
            throw new IllegalArgumentException(MESSAGE_APPOINTMENT_NOT_STARTED);
        }
        List<Appointment> updatedAppointments = new ArrayList<>(appointments);
        updatedAppointments.remove(appointment);
        List<CompletedSession> updatedHistory = new ArrayList<>(history);
        updatedHistory.add(new CompletedSession(appointment, notes, loggedAt));
        return new Client(name, phone, email, address, tags, pets, updatedAppointments, updatedHistory);
    }

    /**
     * Returns true if this client has an appointment with the same date and time.
     *
     * @param appointment The appointment whose slot to check.
     * @return Whether this client already has the date/time slot, regardless of details.
     * @throws NullPointerException if {@code appointment} is null.
     */
    public boolean hasAppointment(Appointment appointment) {
        requireAllNonNull(appointment);
        return appointments.stream().anyMatch(appointment::hasSameDateTime);
    }

    /**
     * Returns a copy of this client with the appointment added,
     * leaving this client unchanged.
     *
     * @param appointment The appointment to add.
     * @return A new client preserving this client's other data and existing appointments.
     * @throws NullPointerException if {@code appointment} is null.
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
                pets, updatedAppointments, history);
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
                updatedPets, appointments, history);
    }

    /**
     * Returns this client's pet with the given name, ignoring case.
     *
     * @param petName the name of the pet to find
     * @return the matching pet, or an empty optional if this client has no such pet
     */
    public Optional<Pet> getPet(PetName petName) {
        requireAllNonNull(petName);
        int index = indexOfPet(petName);
        return index < 0 ? Optional.empty() : Optional.of(pets.get(index));
    }

    /**
     * Returns this client's pets that {@code appointment} is linked to, in the appointment's order.
     * Pet names are matched ignoring case. Linked names with no matching pet are skipped and logged.
     *
     * @param appointment the appointment whose pets to look up
     * @return an immutable list of the linked pets, which is empty if the appointment has none
     */
    public List<Pet> getPetsFor(Appointment appointment) {
        requireAllNonNull(appointment);
        List<Pet> linkedPets = new ArrayList<>();
        for (PetName petName : appointment.getPetNames()) {
            getPet(petName).ifPresentOrElse(linkedPets::add, () -> logger.warning(String.format(
                    "Appointment on %s at %s is linked to pet %s, but %s has no such pet",
                    appointment.getDate(), appointment.getTime(), petName, name)));
        }
        return List.copyOf(linkedPets);
    }

    /**
     * Returns a new client with the pet named {@code targetName} replaced by {@code editedPet},
     * keeping the pet's position. Appointments linked to the pet are updated to its new name.
     * This client is unchanged.
     *
     * @param targetName the name of the pet to replace, ignoring case
     * @param editedPet the replacement pet
     * @return a new client containing the edited pet
     * @throws PetNotFoundException if this client has no pet named {@code targetName}
     * @throws DuplicatePetException if the edited pet's name matches another of this client's pets
     */
    public Client withPetReplaced(PetName targetName, Pet editedPet) {
        requireAllNonNull(targetName, editedPet);
        int index = indexOfPet(targetName);
        if (index < 0) {
            throw new PetNotFoundException();
        }

        List<Pet> updatedPets = new ArrayList<>(pets);
        updatedPets.set(index, editedPet);
        return new Client(name, phone, email, address, tags,
                updatedPets, relinkAppointments(targetName, Optional.of(editedPet.getName())), history);
    }

    /**
     * Returns a new client without the pet named {@code targetName}.
     * The pet is also unlinked from this client's appointments. This client is unchanged.
     *
     * @param targetName the name of the pet to remove, ignoring case
     * @return a new client without the pet
     * @throws PetNotFoundException if this client has no pet named {@code targetName}
     */
    public Client withoutPet(PetName targetName) {
        requireAllNonNull(targetName);
        int index = indexOfPet(targetName);
        if (index < 0) {
            throw new PetNotFoundException();
        }

        List<Pet> updatedPets = new ArrayList<>(pets);
        updatedPets.remove(index);
        return new Client(name, phone, email, address, tags,
                updatedPets, relinkAppointments(targetName, Optional.empty()), history);
    }

    /**
     * Returns this client's appointments with every link to the pet named {@code targetName}, ignoring case,
     * replaced by {@code replacementName}, or removed if {@code replacementName} is empty.
     * The order of the other linked pets is kept.
     */
    private List<Appointment> relinkAppointments(PetName targetName, Optional<PetName> replacementName) {
        logger.fine("Updating appointment links of " + name + " for pet " + targetName + ": "
                + replacementName.map(newName -> "renamed to " + newName).orElse("removed"));
        String targetKey = identityKeyOf(targetName);
        List<Appointment> updatedAppointments = new ArrayList<>();
        for (Appointment appointment : appointments) {
            Set<PetName> updatedPetNames = new LinkedHashSet<>();
            for (PetName linkedName : appointment.getPetNames()) {
                if (identityKeyOf(linkedName).equals(targetKey)) {
                    replacementName.ifPresent(updatedPetNames::add);
                } else {
                    updatedPetNames.add(linkedName);
                }
            }
            updatedAppointments.add(appointment.withPetNames(updatedPetNames));
        }
        assert updatedAppointments.size() == appointments.size()
                : "Relinking pets must preserve the appointment count";
        return updatedAppointments;
    }

    /**
     * Returns the case-insensitive key used to match pet names, consistent with {@link Pet#getIdentityKey()}.
     */
    private static String identityKeyOf(PetName petName) {
        return petName.fullName.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the position of the pet named {@code petName}, ignoring case, or -1 if there is none.
     */
    private int indexOfPet(PetName petName) {
        String identityKey = identityKeyOf(petName);
        for (int i = 0; i < pets.size(); i++) {
            if (pets.get(i).getIdentityKey().equals(identityKey)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns this client's appointment at the given date and time.
     *
     * @param date the appointment date
     * @param time the appointment time
     * @return the matching appointment, or an empty optional if there is none
     */
    public Optional<Appointment> getAppointment(AppointmentDate date, AppointmentTime time) {
        requireAllNonNull(date, time);
        int index = indexOfAppointment(date, time);
        return index < 0 ? Optional.empty() : Optional.of(appointments.get(index));
    }

    /**
     * Returns a new client with the appointment at the given date and time replaced by
     * {@code editedAppointment}, keeping its position. This client is unchanged.
     *
     * @param date the date of the appointment to replace
     * @param time the time of the appointment to replace
     * @param editedAppointment the replacement appointment, which may have a new date and time
     * @return a new client containing the edited appointment
     * @throws AppointmentNotFoundException if this client has no appointment at that date and time
     * @throws DuplicateAppointmentException if the edited appointment's slot matches another appointment
     */
    public Client withAppointmentReplaced(AppointmentDate date, AppointmentTime time,
            Appointment editedAppointment) {
        requireAllNonNull(date, time, editedAppointment);
        int index = indexOfAppointment(date, time);
        if (index < 0) {
            throw new AppointmentNotFoundException();
        }

        List<Appointment> updatedAppointments = new ArrayList<>(appointments);
        updatedAppointments.set(index, editedAppointment);
        return new Client(name, phone, email, address, tags,
                pets, updatedAppointments, history);
    }

    /**
     * Returns a new client without the appointment at the given date and time. This client is unchanged.
     *
     * @param date the date of the appointment to remove
     * @param time the time of the appointment to remove
     * @return a new client without the appointment
     * @throws AppointmentNotFoundException if this client has no appointment at that date and time
     */
    public Client withoutAppointment(AppointmentDate date, AppointmentTime time) {
        requireAllNonNull(date, time);
        int index = indexOfAppointment(date, time);
        if (index < 0) {
            throw new AppointmentNotFoundException();
        }

        List<Appointment> updatedAppointments = new ArrayList<>(appointments);
        updatedAppointments.remove(index);
        return new Client(name, phone, email, address, tags,
                pets, updatedAppointments, history);
    }

    /**
     * Returns the position of the appointment at the given date and time, or -1 if there is none.
     */
    private int indexOfAppointment(AppointmentDate date, AppointmentTime time) {
        for (int i = 0; i < appointments.size(); i++) {
            Appointment appointment = appointments.get(i);
            if (appointment.getDate().equals(date) && appointment.getTime().equals(time)) {
                return i;
            }
        }
        return -1;
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
                && appointments.equals(otherClient.appointments)
                && history.equals(otherClient.history);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, tags,
                pets, appointments, history);
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
                .add("history", history)
                .toString();
    }

}
