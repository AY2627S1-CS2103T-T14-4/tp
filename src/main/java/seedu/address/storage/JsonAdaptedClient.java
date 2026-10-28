package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.exceptions.DuplicateAppointmentException;
import seedu.address.model.client.Address;
import seedu.address.model.client.Client;
import seedu.address.model.client.Email;
import seedu.address.model.client.Name;
import seedu.address.model.client.Phone;
import seedu.address.model.history.CompletedSession;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.exceptions.DuplicatePetException;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Client}.
 */
class JsonAdaptedClient {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Client's %s field is missing!";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();
    private final List<JsonAdaptedPet> pets = new ArrayList<>();
    private final List<JsonAdaptedAppointment> appointments = new ArrayList<>();
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<JsonAdaptedCompletedSession> history = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedClient} from stored fields.
     * Missing appointments or history properties in older data files default to empty lists.
     */
    @JsonCreator
    public JsonAdaptedClient(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("pets") List<JsonAdaptedPet> pets,
            @JsonProperty("appointments") List<JsonAdaptedAppointment> appointments,
            @JsonProperty("history") List<JsonAdaptedCompletedSession> history) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        if (tags != null) {
            this.tags.addAll(tags);
        }
        if (pets != null) {
            this.pets.addAll(pets);
        }
        if (appointments != null) {
            this.appointments.addAll(appointments);
        }
        if (history != null) {
            this.history.addAll(history);
        }
    }

    /**
     * Creates an adapted client without completed history, as in older data files.
     */
    public JsonAdaptedClient(String name, String phone, String email, String address, List<JsonAdaptedTag> tags,
            List<JsonAdaptedPet> pets, List<JsonAdaptedAppointment> appointments) {
        this(name, phone, email, address, tags, pets, appointments, null);
    }

    /**
     * Creates an adapted client without appointments, as in older data files.
     */
    public JsonAdaptedClient(String name, String phone, String email, String address, List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, tags, null, null);
    }

    /**
     * Creates an adapted client without pets, as in older data files.
     */
    public JsonAdaptedClient(String name, String phone, String email, String address, List<JsonAdaptedTag> tags,
            List<JsonAdaptedAppointment> appointments) {
        this(name, phone, email, address, tags, null, appointments);
    }

    /**
     * Converts a given {@code Client} into this class for Jackson use.
     */
    public JsonAdaptedClient(Client source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
        pets.addAll(source.getPets().stream()
                .map(JsonAdaptedPet::new)
                .collect(Collectors.toList()));
        appointments.addAll(source.getAppointments().stream()
                .map(JsonAdaptedAppointment::new)
                .collect(Collectors.toList()));
        history.addAll(source.getHistory().stream()
                .map(JsonAdaptedCompletedSession::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted client object into the model's {@code Client} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted client.
     */
    public Client toModelType() throws IllegalValueException {
        final List<Tag> clientTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            if (tag == null) {
                throw new IllegalValueException("Tag entry is missing!");
            }
            clientTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        final Set<Tag> modelTags = new HashSet<>(clientTags);
        final List<Pet> modelPets = new ArrayList<>();
        for (JsonAdaptedPet pet : pets) {
            if (pet == null) {
                throw new IllegalValueException("Pet entry is missing!");
            }
            modelPets.add(pet.toModelType());
        }
        final List<Appointment> modelAppointments = new ArrayList<>();
        for (JsonAdaptedAppointment appointment : appointments) {
            if (appointment == null) {
                throw new IllegalValueException("Appointment entry is missing!");
            }
            modelAppointments.add(appointment.toModelType());
        }
        final List<CompletedSession> modelHistory = new ArrayList<>();
        for (JsonAdaptedCompletedSession session : history) {
            if (session == null) {
                throw new IllegalValueException("History entry is missing!");
            }
            modelHistory.add(session.toModelType());
        }
        try {
            return new Client(modelName, modelPhone, modelEmail, modelAddress, modelTags,
                    modelPets, modelAppointments, modelHistory);
        } catch (DuplicatePetException e) {
            throw new IllegalValueException("Pets list contains duplicate pet names.");
        } catch (DuplicateAppointmentException e) {
            throw new IllegalValueException("Appointments list contains duplicate date/time slots.");
        }
    }

}
