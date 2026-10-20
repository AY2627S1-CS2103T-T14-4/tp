package seedu.address.model.util;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.PawPals;
import seedu.address.model.ReadOnlyPawPals;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Address;
import seedu.address.model.client.Client;
import seedu.address.model.client.Email;
import seedu.address.model.client.Name;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.PetNote;
import seedu.address.model.pet.Species;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code PawPals} with sample data.
 * The sample clients show the main features on first launch: pets with notes (including safety notes),
 * and appointments with and without an end time, linked to the pets they cover.
 */
public class SampleDataUtil {
    public static Client[] getSampleClients() {
        return new Client[] {
            new Client(new Name("Alex Yeoh"), new Phone("87438807"), new Email("alexyeoh@example.com"),
                new Address("Blk 30 Geylang Street 29, #06-40"),
                getTagSet("regular"),
                List.of(getPet("Milo", "Dog", "Toy Poodle")
                        .withNote(NoteType.PREF, new PetNote("Teddy bear cut, keep the ears long"))
                        .withNote(NoteType.TEMP, new PetNote("Nervous around hair dryers"))),
                List.of(getAppointment("05-12-2026", "10:00", "11:30", "Full groom", "Milo"))),
            new Client(new Name("Bernice Yu"), new Phone("99272758"), new Email("berniceyu@example.com"),
                new Address("Blk 30 Lorong 3 Serangoon Gardens, #07-18"),
                getTagSet("monthly", "vip"),
                List.of(getPet("Luna", "Cat", "Persian")
                                .withNote(NoteType.SENS, new PetNote("Allergic to oatmeal shampoo")),
                        getPet("Mochi", "Cat", "British Shorthair")),
                List.of(getAppointment("07-12-2026", "14:00", "15:00", "Bath and nail trim", "Luna", "Mochi"))),
            new Client(new Name("Charlotte Oliveiro"), new Phone("93210283"), new Email("charlotte@example.com"),
                new Address("Blk 11 Ang Mo Kio Street 74, #11-04"),
                getTagSet("newClient"),
                List.of(getPet("Bao", "Dog", "Shiba Inu")
                        .withNote(NoteType.TEMP, new PetNote("Bites when its paws are handled"))),
                List.of(getAppointment("08-12-2026", "09:30", null, "First visit, assess coat", "Bao"))),
            new Client(new Name("David Li"), new Phone("91031282"), new Email("lidavid@example.com"),
                new Address("Blk 436 Serangoon Gardens Street 26, #16-43"),
                getTagSet("regular"),
                List.of(getPet("Coco", "Dog", "Golden Retriever")
                        .withNote(NoteType.PREF, new PetNote("Short summer cut"))),
                List.of()),
            new Client(new Name("Irfan Ibrahim"), new Phone("92492021"), new Email("irfan@example.com"),
                new Address("Blk 47 Tampines Street 20, #17-35"),
                getTagSet("monthly"),
                List.of(getPet("Simba", "Cat", "Maine Coon")),
                List.of(getAppointment("12-12-2026", "16:00", "17:30", "Deshedding treatment", "Simba"))),
            new Client(new Name("Roy Balakrishnan"), new Phone("92624417"), new Email("royb@example.com"),
                new Address("Blk 45 Aljunied Street 85, #11-31"),
                getTagSet("newClient"))
        };
    }

    public static ReadOnlyPawPals getSamplePawPals() {
        PawPals sampleAb = new PawPals();
        for (Client sampleClient : getSampleClients()) {
            sampleAb.addClient(sampleClient);
        }
        return sampleAb;
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

    /**
     * Returns a pet with no notes.
     */
    private static Pet getPet(String name, String species, String breed) {
        return new Pet(new PetName(name), new Species(species), new Breed(breed));
    }

    /**
     * Returns an appointment linked to the given pets.
     *
     * @param endTime The end time, or null for an appointment with a start time only.
     */
    private static Appointment getAppointment(String date, String startTime, String endTime, String details,
            String... petNames) {
        Set<PetName> linkedPets = Arrays.stream(petNames)
                .map(PetName::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new Appointment(new AppointmentDate(date), new AppointmentTime(startTime),
                Optional.ofNullable(endTime).map(AppointmentTime::new), details, linkedPets);
    }

}
