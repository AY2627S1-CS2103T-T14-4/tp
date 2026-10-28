package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_CLIENT;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.UserPrefs;
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
import seedu.address.testutil.ClientBuilder;

public class AppointmentLinkedPetsCommandTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-12-12T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"))
            .withNote(NoteType.SENS, new PetNote("Sensitive paws"));
    private static final Pet FLUFFY = new Pet(new PetName("Sir Fluffy"), new Species("Cat"), new Breed("Persian"));
    private static final Pet LUNA = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));
    private static final Appointment APPOINTMENT = new Appointment(new AppointmentDate("13-12-2026"),
            new AppointmentTime("14:00"), "Call on arrival").withEndTime(new AppointmentTime("15:00"));
    private static final Appointment OTHER = new Appointment(new AppointmentDate("13-12-2026"),
            new AppointmentTime("09:00"));
    private static final CompletedSession HISTORY = new CompletedSession(OTHER, new SessionNotes("Previous visit"),
            CLOCK.instant().minusSeconds(1));
    private static final Client SELECTED = new ClientBuilder(BENSON).withPets(MILO, FLUFFY)
            .withAppointments(OTHER).withHistory(HISTORY).build();
    private static final Client OTHER_CLIENT = new ClientBuilder(ALICE).withPets(LUNA).build();

    @Test
    public void execute_afterFiltering_resolvesNamesAndDuplicatesWhilePreservingAllOtherData() throws Exception {
        Model model = modelWith(OTHER_CLIENT, SELECTED);
        model.updateFilteredClientList(client -> client.isSameClient(SELECTED));
        Appointment requested = APPOINTMENT.withPetNames(new LinkedHashSet<>(List.of(new PetName("sir   FLUFFY"),
                new PetName("MILO"), new PetName("milo"), new PetName("Sir Fluffy"))));

        CommandResult result = new AppointmentCommand(INDEX_FIRST_CLIENT, requested, CLOCK).execute(model);

        Appointment resolved = APPOINTMENT.withPetNames(new LinkedHashSet<>(List.of(FLUFFY.getName(), MILO.getName())));
        Client expected = SELECTED.copyWithAppointment(resolved);
        assertEquals(List.of(OTHER_CLIENT, expected), model.getPawPals().getClientList());
        assertEquals(List.of(expected), model.getFilteredClientList());
        assertEquals(List.of(FLUFFY.getName(), MILO.getName()),
                List.copyOf(model.getFilteredClientList().getFirst().getAppointments().getLast().getPetNames()));
        assertEquals("Added appointment: Benson Meier d/13-12-2026 t/14:00 et/15:00 pn/Sir Fluffy pn/Milo",
                result.getFeedbackToUser());
    }

    @Test
    public void execute_unknownOrOtherClientsPet_rejectsWholeAppointmentWithoutMutation() {
        Model model = modelWith(OTHER_CLIENT, SELECTED);
        model.updateFilteredClientList(client -> client.isSameClient(SELECTED));
        for (String missing : List.of("Unknown", "Luna")) {
            Appointment requested = APPOINTMENT.withPetNames(
                    new LinkedHashSet<>(List.of(MILO.getName(), new PetName(missing))));
            assertCommandFailure(new AppointmentCommand(INDEX_FIRST_CLIENT, requested, CLOCK), model,
                    "This client has no pet named " + missing + ".");
        }
    }

    @Test
    public void execute_withoutPetNames_doesNotInferCurrentPetsAndKeepsExistingFeedback() throws Exception {
        Model model = modelWith(OTHER_CLIENT, SELECTED);
        CommandResult result = new AppointmentCommand(INDEX_SECOND_CLIENT, APPOINTMENT, CLOCK).execute(model);
        assertEquals(SELECTED.copyWithAppointment(APPOINTMENT), model.getPawPals().getClientList().get(1));
        assertEquals("Added appointment: Benson Meier d/13-12-2026 t/14:00 et/15:00", result.getFeedbackToUser());
    }

    @Test
    public void execute_linkedPetWithoutEndTime_storesCanonicalName() throws Exception {
        Model model = modelWith(OTHER_CLIENT, SELECTED);
        Appointment requested = new Appointment(APPOINTMENT.getDate(), APPOINTMENT.getTime(), "",
                Set.of(new PetName("milo")));
        CommandResult result = new AppointmentCommand(INDEX_SECOND_CLIENT, requested, CLOCK).execute(model);
        assertEquals(Set.of(MILO.getName()), model.getPawPals().getClientList().get(1)
                .getAppointments().getLast().getPetNames());
        assertEquals("Added appointment: Benson Meier d/13-12-2026 t/14:00 pn/Milo", result.getFeedbackToUser());
    }

    @Test
    public void execute_invalidDisplayedIndex_preservesData() {
        Model model = modelWith(OTHER_CLIENT, SELECTED);
        model.updateFilteredClientList(client -> client.isSameClient(SELECTED));
        Appointment requested = APPOINTMENT.withPetNames(Set.of(MILO.getName()));
        assertCommandFailure(new AppointmentCommand(INDEX_SECOND_CLIENT, requested, CLOCK), model,
                Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_pastStartAfterFiltering_resolvesPetAndPreservesOtherData() throws Exception {
        Model model = modelWith(OTHER_CLIENT, SELECTED);
        model.updateFilteredClientList(client -> client.isSameClient(SELECTED));
        Appointment past = new Appointment(new AppointmentDate("11-12-2026"), APPOINTMENT.getTime(), "",
                Set.of(new PetName("milo"))).withEndTime(new AppointmentTime("15:00"));

        CommandResult result = new AppointmentCommand(INDEX_FIRST_CLIENT, past, CLOCK).execute(model);

        Client expected = SELECTED.copyWithAppointment(past.withPetNames(Set.of(MILO.getName())));
        assertEquals(List.of(OTHER_CLIENT, expected), model.getPawPals().getClientList());
        assertEquals(List.of(expected), model.getFilteredClientList());
        assertEquals("Added appointment: Benson Meier d/11-12-2026 t/14:00 et/15:00 pn/Milo\n"
                + AppointmentCommand.MESSAGE_PAST_APPOINTMENT, result.getFeedbackToUser());

        assertCommandFailure(new AppointmentCommand(INDEX_FIRST_CLIENT,
                past.withPetNames(Set.of(LUNA.getName())), CLOCK), model,
                "This client has no pet named Luna.");
    }

    @Test
    public void execute_samePetNameOnDifferentClients_usesSelectedClientsRecordedSpelling() throws Exception {
        Client other = new ClientBuilder(ALICE).withPets(new Pet(new PetName("MILO"),
                new Species("Cat"), new Breed("Persian"))).build();
        Model model = modelWith(other, SELECTED);
        Appointment requested = APPOINTMENT.withPetNames(Set.of(new PetName("mIlO")));
        new AppointmentCommand(INDEX_SECOND_CLIENT, requested, CLOCK).execute(model);
        assertEquals(Set.of(MILO.getName()), model.getPawPals().getClientList().get(1)
                .getAppointments().getLast().getPetNames());
        assertEquals(other, model.getPawPals().getClientList().getFirst());
    }

    private static Model modelWith(Client... clients) {
        PawPals data = new PawPals();
        for (Client client : clients) {
            data.addClient(client);
        }
        return new ModelManager(data, new UserPrefs());
    }
}
