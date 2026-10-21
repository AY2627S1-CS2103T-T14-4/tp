package seedu.address.model.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalClients.ALICE;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.appointment.exceptions.AppointmentNotFoundException;
import seedu.address.model.history.CompletedSession;
import seedu.address.model.history.SessionNotes;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.PetNote;
import seedu.address.model.pet.Species;
import seedu.address.testutil.ClientBuilder;

public class ClientHistoryTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-21T06:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final SessionNotes NOTES = new SessionNotes("Teddy cut, short ears");
    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"))
            .withNote(NoteType.SENS, new PetNote("Sensitive paws"));
    private static final Appointment STARTING_NOW = appointment("21-10-2026", "14:00")
            .withEndTime(new AppointmentTime("15:00")).withPetNames(Set.of(MILO.getName()));
    private static final Appointment PAST = appointment("20-10-2026", "09:00");
    private static final Appointment FUTURE = appointment("22-10-2026", "14:00");
    private static final CompletedSession OLD_SESSION = session(PAST, CLOCK.instant().minusSeconds(1));

    @Test
    public void existingConstructors_defaultToEmptyHistory() {
        assertTrue(ALICE.getHistory().isEmpty());
        assertTrue(new Client(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(),
                ALICE.getTags(), List.of(PAST)).getHistory().isEmpty());
        assertTrue(new Client(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(),
                ALICE.getTags(), List.of(MILO), List.of(PAST)).getHistory().isEmpty());
    }

    @Test
    public void constructor_copiesHistoryAndReturnsUnmodifiableList() {
        List<CompletedSession> history = new ArrayList<>(List.of(OLD_SESSION));
        Client client = withHistory(history);
        history.clear();
        assertEquals(List.of(OLD_SESSION), client.getHistory());
        assertThrows(UnsupportedOperationException.class, () -> client.getHistory().clear());
        assertThrows(NullPointerException.class, () -> withHistory(null));
        assertThrows(NullPointerException.class, () -> withHistory(Arrays.asList(OLD_SESSION, null)));
    }

    @Test
    public void constructor_sortsByAppointmentThenLoggingTimeAndRetainsAllRecords() {
        CompletedSession latest = session(STARTING_NOW, CLOCK.instant());
        CompletedSession earlierLogging = session(STARTING_NOW, CLOCK.instant().minusSeconds(1));
        CompletedSession earlierTime = session(appointment("21-10-2026", "13:00"), CLOCK.instant().plusSeconds(10));
        CompletedSession loggedLate = session(PAST, CLOCK.instant().plusSeconds(100));
        Client client = withHistory(List.of(loggedLate, earlierTime, earlierLogging, latest));
        assertEquals(List.of(latest, earlierLogging, earlierTime, loggedLate), client.getHistory());
    }

    @Test
    public void completeAppointment_atStartBeforePlannedEnd_movesOnlyOneAppointment() {
        Client original = new ClientBuilder(ALICE).withPets(MILO).withAppointments(PAST, STARTING_NOW, FUTURE)
                .withHistory(OLD_SESSION).build();

        Client completed = complete(original, STARTING_NOW, CLOCK);

        CompletedSession expected = new CompletedSession(STARTING_NOW, NOTES, CLOCK.instant());
        assertEquals(new ClientBuilder(original).withAppointments(PAST, FUTURE)
                .withHistory(expected, OLD_SESSION).build(), completed);
        assertEquals(List.of(PAST, STARTING_NOW, FUTURE), original.getAppointments());
        assertEquals(List.of(OLD_SESSION), original.getHistory());
        assertTrue(original.isSameClient(completed));
        assertEquals(MILO.getNotes(), completed.getPets().getFirst().getNotes());
    }

    @Test
    public void completeAppointment_pastAndClientOnlyAppointment_succeeds() {
        Client original = new ClientBuilder(ALICE).withPets(MILO).withAppointments(PAST).build();
        Client completed = complete(original, PAST, CLOCK);
        assertTrue(completed.getAppointments().isEmpty());
        assertTrue(completed.getHistory().getFirst().getAppointment().getPetNames().isEmpty());
        assertTrue(completed.hasCompletedAppointment(PAST.getDate(), PAST.getTime()));
        assertFalse(completed.hasAppointment(PAST));
    }

    @Test
    public void completeAppointment_multiplePets_createsOneSessionWithAllRecordedNames() {
        Pet luna = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));
        Appointment appointment = STARTING_NOW.withPetNames(Set.of(MILO.getName(), luna.getName()));
        Client original = new ClientBuilder(ALICE).withPets(MILO, luna).withAppointments(appointment).build();

        Client completed = complete(original, appointment, CLOCK);

        assertEquals(1, completed.getHistory().size());
        assertEquals(appointment, completed.getHistory().getFirst().getAppointment());
        assertEquals(List.of(MILO, luna), completed.getPets());
        assertTrue(completed.getAppointments().isEmpty());
    }

    @Test
    public void completeAppointment_futureOrWrongZone_throwsWithoutChangingOriginal() {
        Client original = new ClientBuilder(ALICE).withAppointments(STARTING_NOW, FUTURE).build();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, ()
                -> complete(original, FUTURE, CLOCK));
        assertEquals(Client.MESSAGE_APPOINTMENT_NOT_STARTED, exception.getMessage());
        Clock oneSecondEarly = Clock.fixed(CLOCK.instant().minusSeconds(1), CLOCK.getZone());
        assertThrows(IllegalArgumentException.class, () -> complete(original, STARTING_NOW, oneSecondEarly));
        // The same instant is 06:00 in UTC, before the appointment's local 14:00 start.
        assertThrows(IllegalArgumentException.class, ()
                -> complete(original, STARTING_NOW, CLOCK.withZone(ZoneOffset.UTC)));
        assertEquals(List.of(STARTING_NOW, FUTURE), original.getAppointments());
        assertTrue(original.getHistory().isEmpty());
    }

    @Test
    public void completeAppointment_repeatedOrMissingSlot_throwsWithoutChangingHistory() {
        Client original = new ClientBuilder(ALICE).withAppointments(PAST).build();
        Client completed = complete(original, PAST, CLOCK);
        assertThrows(AppointmentNotFoundException.class, () -> complete(completed, PAST, CLOCK));
        assertThrows(AppointmentNotFoundException.class, () -> complete(original, STARTING_NOW, CLOCK));
        assertEquals(1, completed.getHistory().size());
        assertTrue(original.getHistory().isEmpty());
    }

    @Test
    public void completeAppointment_activeSlotTakesPriorityOverMatchingHistory() {
        Client original = new ClientBuilder(ALICE).withHistory(OLD_SESSION).withAppointments(PAST).build();
        assertTrue(original.hasCompletedAppointment(PAST.getDate(), PAST.getTime()));
        Client completed = complete(original, PAST, CLOCK);
        assertEquals(2, completed.getHistory().size());
        assertTrue(completed.getAppointments().isEmpty());
        assertFalse(completed.hasCompletedAppointment(PAST.getDate(), new AppointmentTime("10:00")));
        assertFalse(completed.hasCompletedAppointment(STARTING_NOW.getDate(), PAST.getTime()));
    }

    @Test
    public void completeAppointment_nullArguments_throwsWithoutChangingOriginal() {
        Client client = new ClientBuilder(ALICE).withAppointments(PAST).build();
        assertThrows(NullPointerException.class, () -> client.completeAppointment(null, PAST.getTime(), NOTES, CLOCK));
        assertThrows(NullPointerException.class, () -> client.completeAppointment(PAST.getDate(), null, NOTES, CLOCK));
        assertThrows(NullPointerException.class, ()
                -> client.completeAppointment(PAST.getDate(), PAST.getTime(), null, CLOCK));
        assertThrows(NullPointerException.class, ()
                -> client.completeAppointment(PAST.getDate(), PAST.getTime(), NOTES, null));
        assertThrows(NullPointerException.class, () -> client.hasCompletedAppointment(null, PAST.getTime()));
        assertThrows(NullPointerException.class, () -> client.hasCompletedAppointment(PAST.getDate(), null));
        assertEquals(List.of(PAST), client.getAppointments());
        assertTrue(client.getHistory().isEmpty());
    }

    @Test
    public void appointmentCopies_preserveHistoryAndDoNotReserveHistoricalSlots() {
        Client original = new ClientBuilder(ALICE).withHistory(OLD_SESSION).build();
        Client added = original.copyWithAppointment(PAST);
        Client rescheduled = added.withAppointmentReplaced(PAST.getDate(), PAST.getTime(), STARTING_NOW);
        Client cancelled = rescheduled.withoutAppointment(STARTING_NOW.getDate(), STARTING_NOW.getTime());
        assertEquals(List.of(OLD_SESSION), added.getHistory());
        assertEquals(List.of(OLD_SESSION), rescheduled.getHistory());
        assertEquals(original, cancelled);
    }

    @Test
    public void petCopies_updateActiveLinksAndPreserveHistoricalNames() {
        CompletedSession historical = session(STARTING_NOW, CLOCK.instant());
        Client original = new ClientBuilder(ALICE).withPets(MILO).withAppointments(STARTING_NOW)
                .withHistory(historical).build();
        Pet renamedPet = new Pet(new PetName("Max"), MILO.getSpecies(), MILO.getBreed(), MILO.getNotes());
        Client renamed = original.withPetReplaced(MILO.getName(), renamedPet);
        Pet notedPet = MILO.withNote(NoteType.PREF, new PetNote("Short ears"));
        Client noted = original.withPetReplaced(MILO.getName(), notedPet);
        Client removed = original.withoutPet(MILO.getName());
        Pet luna = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));
        Client added = original.withPet(luna);

        for (Client updated : List.of(renamed, noted, removed, added)) {
            assertEquals(List.of(historical), updated.getHistory());
            assertEquals(Set.of(new PetName("Milo")), updated.getHistory().getFirst().getAppointment().getPetNames());
        }
        assertEquals(Set.of(new PetName("Max")), renamed.getAppointments().getFirst().getPetNames());
        assertTrue(removed.getAppointments().getFirst().getPetNames().isEmpty());
    }

    @Test
    public void equalsAndHashCode_includeHistoryButIdentityDoesNot() {
        Client original = new ClientBuilder(ALICE).withHistory(OLD_SESSION).build();
        Client same = new ClientBuilder(original).build();
        assertEquals(original, same);
        assertEquals(original.hashCode(), same.hashCode());
        assertNotEquals(ALICE, original);
        assertTrue(ALICE.isSameClient(original));
    }

    private static Appointment appointment(String date, String time) {
        return new Appointment(new AppointmentDate(date), new AppointmentTime(time), "Call on arrival");
    }

    private static CompletedSession session(Appointment appointment, Instant loggedAt) {
        return new CompletedSession(appointment, NOTES, loggedAt);
    }

    private static Client withHistory(List<CompletedSession> history) {
        return new Client(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(), ALICE.getTags(),
                List.of(), List.of(), history);
    }

    private static Client complete(Client client, Appointment appointment, Clock clock) {
        return client.completeAppointment(appointment.getDate(), appointment.getTime(), NOTES, clock);
    }
}
