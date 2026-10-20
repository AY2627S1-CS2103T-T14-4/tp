package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.getTypicalPawPals;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.PetNote;
import seedu.address.model.pet.Species;
import seedu.address.testutil.ClientBuilder;

public class ClearCommandTest {

    @Test
    public void execute_emptyPawPals_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nonEmptyPawPals_success() {
        Model model = new ModelManager(getTypicalPawPals(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalPawPals(), new UserPrefs());
        expectedModel.setPawPals(new PawPals());

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_clientWithAppointment_removesClientAndAppointment() throws Exception {
        Model model = new ModelManager(getTypicalPawPals(), new UserPrefs());
        Client original = model.getFilteredClientList().get(0);
        Appointment appointment = new Appointment(new AppointmentDate("01-01-2028"),
                new AppointmentTime("09:05"));
        model.setClient(original, original.copyWithAppointment(appointment));

        new ClearCommand().execute(model);

        assertTrue(model.getPawPals().getClientList().isEmpty());
    }

    @Test
    public void execute_clientWithNotesAndLinkedAppointment_removesEverything() {
        Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"))
                .withNote(NoteType.TEMP, new PetNote("Nervous around dryers"));
        Appointment groom = new Appointment(new AppointmentDate("01-01-2028"), new AppointmentTime("09:00"),
                Optional.of(new AppointmentTime("10:30")), "Full groom", Set.of(new PetName("Milo")));
        PawPals pawPals = new PawPals();
        pawPals.addClient(new ClientBuilder(ALICE).withPets(milo).withAppointments(groom).build());
        Model model = new ModelManager(pawPals, new UserPrefs());
        Model expectedModel = new ModelManager(new PawPals(), new UserPrefs());

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }
}
