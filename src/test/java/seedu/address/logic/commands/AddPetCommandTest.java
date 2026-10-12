package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showClientAtIndex;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.getTypicalAddressBook;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_CLIENT;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;
import seedu.address.testutil.ClientBuilder;

public class AddPetCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private final Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));

    @Test
    public void execute_validIndex_success() {
        Client targetClient = model.getFilteredClientList().get(INDEX_FIRST_CLIENT.getZeroBased());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Client expectedUpdatedClient = targetClient.withPet(milo);
        expectedModel.setClient(targetClient, expectedUpdatedClient);

        AddPetCommand command = new AddPetCommand(INDEX_FIRST_CLIENT, milo);

        assertCommandSuccess(command, model,
                String.format(AddPetCommand.MESSAGE_SUCCESS, milo.getName()),
                expectedModel);
    }

    @Test
    public void execute_duplicatePet_throwsCommandException() {
        Client clientWithPet = new ClientBuilder(ALICE).build().withPet(milo);
        AddressBook addressBook = new AddressBook();
        addressBook.addClient(clientWithPet);
        Model modelWithPet = new ModelManager(addressBook, new UserPrefs());

        AddPetCommand command = new AddPetCommand(INDEX_FIRST_CLIENT, milo);

        assertCommandFailure(command, modelWithPet, AddPetCommand.MESSAGE_DUPLICATE_PET);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundsIndex = Index.fromOneBased(model.getFilteredClientList().size() + 1);
        AddPetCommand command = new AddPetCommand(outOfBoundsIndex, milo);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showClientAtIndex(model, INDEX_SECOND_CLIENT);
        Client targetClient = model.getFilteredClientList().get(INDEX_FIRST_CLIENT.getZeroBased());

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        showClientAtIndex(expectedModel, INDEX_SECOND_CLIENT);
        Client expectedTarget = expectedModel.getFilteredClientList().get(INDEX_FIRST_CLIENT.getZeroBased());
        expectedModel.setClient(expectedTarget, expectedTarget.withPet(milo));

        AddPetCommand command = new AddPetCommand(INDEX_FIRST_CLIENT, milo);

        assertCommandSuccess(command, model,
                String.format(AddPetCommand.MESSAGE_SUCCESS, milo.getName()),
                expectedModel);
        assertTrue(model.getAddressBook().getClientList().contains(targetClient.withPet(milo)));
    }

    @Test
    public void execute_indexOutsideFilteredListButInsideFullList_throwsCommandException() {
        showClientAtIndex(model, INDEX_FIRST_CLIENT);

        // The second index exists in the full list, but not in the one-client filtered list.
        assertTrue(INDEX_SECOND_CLIENT.getZeroBased() < model.getAddressBook().getClientList().size());

        AddPetCommand command = new AddPetCommand(INDEX_SECOND_CLIENT, milo);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }
}