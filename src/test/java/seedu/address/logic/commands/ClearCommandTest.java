package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalClients.getTypicalPawPals;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;

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

}
