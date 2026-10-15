package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalClients.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.client.Client;
import seedu.address.testutil.ClientBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_newClient_success() {
        Client validClient = new ClientBuilder().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addClient(validClient);

        assertCommandSuccess(new AddCommand(validClient), model,
                String.format(AddCommand.MESSAGE_SUCCESS, validClient.getName()),
                expectedModel);
    }

    @Test
    public void execute_duplicateClient_throwsCommandException() {
        Client clientInList = model.getAddressBook().getClientList().get(0);
        assertCommandFailure(new AddCommand(clientInList), model,
                AddCommand.MESSAGE_DUPLICATE_CLIENT);
    }

    @Test
    public void execute_duplicateNormalizedNameAndPhone_throwsCommandException() {
        Client clientInList = model.getAddressBook().getClientList().get(0);
        Client duplicate = new ClientBuilder(clientInList)
                .withName(clientInList.getName().toString().toLowerCase().replace(" ", "  "))
                .withEmail("different@example.com")
                .withAddress("Different address")
                .build();

        assertCommandFailure(new AddCommand(duplicate), model,
                AddCommand.MESSAGE_DUPLICATE_CLIENT);
    }

}
