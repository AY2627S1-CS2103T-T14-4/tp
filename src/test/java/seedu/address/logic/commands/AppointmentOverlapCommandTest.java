package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_CLIENT;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.model.pet.PetName;
import seedu.address.testutil.ClientBuilder;

public class AppointmentOverlapCommandTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-12-12T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final Appointment EXISTING = slot("14:00", "15:00");

    @Test
    public void execute_overlapsSameOrDifferentClient_rejectsEveryRangeShapeWithoutMutation() {
        List<Appointment> conflicts = List.of(EXISTING, slot("14:30", "15:30"), slot("13:30", "14:30"),
                slot("14:15", "14:45"), slot("13:00", "16:00"), slot("14:00", null), slot("14:59", null));
        for (int ownerIndex : new int[] {0, 1}) {
            Client owner = new ClientBuilder(ownerIndex == 0 ? ALICE : BENSON).withAppointments(EXISTING).build();
            Model model = modelWith(ownerIndex == 0 ? owner : ALICE, ownerIndex == 1 ? owner : BENSON);
            for (Appointment candidate : conflicts) {
                // Booking for another pet cannot bypass the global overlap rule.
                Appointment linked = candidate.withPetNames(Set.of(new PetName("Luna")));
                assertCommandFailure(new AppointmentCommand(INDEX_SECOND_CLIENT, linked, CLOCK), model,
                        "Appointment overlaps with " + owner.getName()
                                + "'s appointment: d/13-12-2026 t/14:00 et/15:00.");
            }
        }
    }

    @Test
    public void execute_adjacentOrDifferentDate_allowsBookingForEitherClient() throws Exception {
        List<Appointment> allowed = List.of(slot("13:00", "14:00"), slot("15:00", "16:00"),
                slot("15:00", null), new Appointment(new AppointmentDate("14-12-2026"), EXISTING.getTime())
                        .withEndTime(new AppointmentTime("15:00")));
        for (int ownerIndex : new int[] {0, 1}) {
            Client owner = new ClientBuilder(ownerIndex == 0 ? ALICE : BENSON).withAppointments(EXISTING).build();
            for (Appointment candidate : allowed) {
                Model model = modelWith(ownerIndex == 0 ? owner : ALICE, ownerIndex == 1 ? owner : BENSON);
                Client selected = model.getFilteredClientList().get(1);
                new AppointmentCommand(INDEX_SECOND_CLIENT, candidate, CLOCK).execute(model);
                assertEquals(selected.copyWithAppointment(candidate), model.getFilteredClientList().get(1));
                assertEquals(ownerIndex == 0 ? owner : ALICE, model.getPawPals().getClientList().getFirst());
            }
        }
    }

    @Test
    public void execute_hiddenClientConflict_rejectsAndPreservesFilter() {
        Client owner = new ClientBuilder(ALICE).withAppointments(EXISTING).build();
        Model model = modelWith(owner, BENSON);
        model.updateFilteredClientList(client -> client.isSameClient(BENSON));
        assertCommandFailure(new AppointmentCommand(Index.fromOneBased(1), slot("14:30", "15:30"), CLOCK), model,
                "Appointment overlaps with Alice Pauline's appointment: d/13-12-2026 t/14:00 et/15:00.");
        assertEquals(List.of(BENSON), model.getFilteredClientList());
        assertEquals(List.of(owner, BENSON), model.getPawPals().getClientList());
    }

    private static Appointment slot(String start, String end) {
        return new Appointment(new AppointmentDate("13-12-2026"), new AppointmentTime(start),
                Optional.ofNullable(end).map(AppointmentTime::new), "", Set.of());
    }

    private static Model modelWith(Client... clients) {
        PawPals pawPals = new PawPals();
        for (Client client : clients) {
            pawPals.addClient(client);
        }
        return new ModelManager(pawPals, new UserPrefs());
    }
}
