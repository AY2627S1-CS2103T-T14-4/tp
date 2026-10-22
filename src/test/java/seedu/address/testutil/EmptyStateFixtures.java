package seedu.address.testutil;

import java.time.Instant;

import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.model.history.CompletedSession;
import seedu.address.model.history.SessionNotes;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.PetNote;
import seedu.address.model.pet.Species;

/**
 * Representative clients with empty and populated card sections, independent of the current date.
 */
public final class EmptyStateFixtures {
    public static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
    public static final Client EMPTY_CLIENT = new ClientBuilder().build();
    public static final Client CLIENT_WITH_PET = new ClientBuilder().withPets(MILO).build();
    public static final Client CLIENT_WITH_PARTIAL_NOTES = new ClientBuilder().withPets(
            MILO.withNote(NoteType.SENS, new PetNote("Sensitive paws"))).build();
    public static final Client CLIENT_WITH_ALL_NOTES = new ClientBuilder().withPets(
            MILO.withNote(NoteType.PREF, new PetNote("Short trim"))
                    .withNote(NoteType.SENS, new PetNote("Sensitive paws"))
                    .withNote(NoteType.TEMP, new PetNote("Calm"))).build();
    public static final Appointment APPOINTMENT = new Appointment(new AppointmentDate("01-01-2020"),
            new AppointmentTime("10:00"), "Short trim");
    public static final Client CLIENT_WITH_APPOINTMENT = new ClientBuilder().withAppointments(APPOINTMENT).build();
    public static final Client CLIENT_WITH_HISTORY = new ClientBuilder().withHistory(new CompletedSession(
            APPOINTMENT, new SessionNotes("Trim completed"), Instant.parse("2020-01-01T03:00:00Z"))).build();

    private EmptyStateFixtures() {
    }
}
