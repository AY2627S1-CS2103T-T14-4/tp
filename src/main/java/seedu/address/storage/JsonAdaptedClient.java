package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.exceptions.DuplicatePetException;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Client}.
 */
class JsonAdaptedClient {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Client's %s field is missing!";
    public static final String MESSAGE_MISSING_PET = "Pet entry is missing!";
    public static final String MESSAGE_MISSING_APPOINTMENT = "Appointment entry is missing!";
    public static final String MESSAGE_MISSING_SESSION = "History entry is missing!";
    public static final String MESSAGE_DUPLICATE_PETS = "Pets list contains duplicate pet names.";
    public static final String MESSAGE_DUPLICATE_APPOINTMENTS = "Appointments list contains duplicate date/time slots.";
    public static final String MESSAGE_UNKNOWN_LINKED_PET =
            "Appointment is linked to pet %s, but the client has no such pet.";

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
        final List<Pet> modelPets = toModelPets();
        final List<Appointment> modelAppointments = toModelAppointments(modelPets);
        final List<CompletedSession> modelHistory = toModelHistory();
        try {
            return new Client(modelName, modelPhone, modelEmail, modelAddress, modelTags,
                    modelPets, modelAppointments, modelHistory);
        } catch (DuplicatePetException e) {
            throw new IllegalValueException(MESSAGE_DUPLICATE_PETS);
        } catch (DuplicateAppointmentException e) {
            throw new IllegalValueException(MESSAGE_DUPLICATE_APPOINTMENTS);
        }
    }

    /**
     * Converts the stored pets into model pets.
     *
     * @throws IllegalValueException if a pet entry is missing or invalid.
     */
    private List<Pet> toModelPets() throws IllegalValueException {
        List<Pet> modelPets = new ArrayList<>();
        for (JsonAdaptedPet pet : pets) {
            if (pet == null) {
                throw new IllegalValueException(MESSAGE_MISSING_PET);
            }
            modelPets.add(pet.toModelType());
        }
        return modelPets;
    }

    /**
     * Converts the stored appointments into model appointments linked to {@code modelPets}.
     *
     * @throws IllegalValueException if an appointment entry is missing or invalid, or links to an unknown pet.
     */
    private List<Appointment> toModelAppointments(List<Pet> modelPets) throws IllegalValueException {
        List<Appointment> modelAppointments = new ArrayList<>();
        for (JsonAdaptedAppointment appointment : appointments) {
            if (appointment == null) {
                throw new IllegalValueException(MESSAGE_MISSING_APPOINTMENT);
            }
            modelAppointments.add(linkToRecordedPets(appointment.toModelType(), modelPets));
        }
        return modelAppointments;
    }

    /**
     * Converts the stored completed sessions into model sessions.
     *
     * @throws IllegalValueException if a session entry is missing or invalid.
     */
    private List<CompletedSession> toModelHistory() throws IllegalValueException {
        List<CompletedSession> modelHistory = new ArrayList<>();
        for (JsonAdaptedCompletedSession session : history) {
            if (session == null) {
                throw new IllegalValueException(MESSAGE_MISSING_SESSION);
            }
            modelHistory.add(session.toModelType());
        }
        return modelHistory;
    }

    /**
     * Returns {@code appointment} linked to the recorded names of {@code pets}.
     * Links are matched ignoring case, so links that name the same pet are merged.
     *
     * @throws IllegalValueException if a link names a pet that is not in {@code pets}.
     */
    private static Appointment linkToRecordedPets(Appointment appointment, List<Pet> pets)
            throws IllegalValueException {
        Set<PetName> recordedNames = new LinkedHashSet<>();
        for (PetName linkedName : appointment.getPetNames()) {
            String identityKey = linkedName.identityKey();
            Pet linkedPet = pets.stream()
                    .filter(pet -> pet.getIdentityKey().equals(identityKey))
                    .findFirst()
                    .orElseThrow(() -> new IllegalValueException(
                            String.format(MESSAGE_UNKNOWN_LINKED_PET, linkedName)));
            recordedNames.add(linkedPet.getName());
        }
        assert recordedNames.size() <= appointment.getPetNames().size() : "Linking can only merge pet names";
        return appointment.withPetNames(recordedNames);
    }

}
