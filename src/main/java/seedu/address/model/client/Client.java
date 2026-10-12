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

    /**
     * Creates a client with no appointments, delegating field validation to the full constructor.
     *
     * @throws NullPointerException if any field is null.
     */
    public Client(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(name, phone, email, address, tags, List.of());
    }

    /**
     * Creates a client with the given appointments. All fields and appointments must be non-null.
     *
     * @throws DuplicateAppointmentException if appointments share a date and time.
     */
    public Client(Name name, Phone phone, Email email, Address address, Set<Tag> tags,
            List<Appointment> appointments) {
        requireAllNonNull(name, phone, email, address, tags, appointments);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);
        this.appointments = List.copyOf(appointments);
        ensureAppointmentsAreUnique(this.appointments);
    }

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
     * Returns a new client with the appointment added, leaving this client unchanged.
     *
     * @throws DuplicateAppointmentException if the date and time already exist for this client.
     */
    public Client copyWithAppointment(Appointment appointment) {
        requireAllNonNull(appointment);
        if (hasAppointment(appointment)) {
            throw new DuplicateAppointmentException();
        }
        List<Appointment> updatedAppointments = new ArrayList<>(appointments);
        updatedAppointments.add(appointment);
        return new Client(name, phone, email, address, tags, updatedAppointments);
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
     * Returns true if both clients have the same identity and data fields.
     * This defines a stronger notion of equality between two clients.
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
                && appointments.equals(otherClient.appointments);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, tags, appointments);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("tags", tags)
                .add("appointments", appointments)
                .toString();
    }

}
