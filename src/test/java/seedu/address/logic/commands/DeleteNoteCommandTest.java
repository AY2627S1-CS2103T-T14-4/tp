package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.PawPals;
import seedu.address.model.UserPrefs;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.PetNote;
import seedu.address.model.pet.Species;
import seedu.address.testutil.ClientBuilder;

public class DeleteNoteCommandTest {

    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"))
            .withNote(NoteType.PREF, new PetNote("Teddy bear cut"))
            .withNote(NoteType.TEMP, new PetNote("Nervous around dryers"));

    @Test
    public void execute_existingNote_deletesOnlyThatNote() {
        Model model = modelWithPet(MILO);
        Model expectedModel = modelWithPet(MILO);
        Client expectedClient = expectedModel.getFilteredClientList().get(0);
        expectedModel.setClient(expectedClient,
                expectedClient.withPetReplaced(MILO.getName(), MILO.withoutNote(NoteType.TEMP)));

        DeleteNoteCommand command = new DeleteNoteCommand(INDEX_FIRST_CLIENT, new PetName("milo"), NoteType.TEMP);

        assertCommandSuccess(command, model, "Deleted temperament note for Milo", expectedModel);
        Pet updatedPet = model.getFilteredClientList().get(0).getPet(new PetName("Milo")).orElseThrow();
        assertTrue(updatedPet.hasNote(NoteType.PREF));
        assertFalse(updatedPet.hasSafetyNote());
    }

    @Test
    public void execute_afterFilter_usesDisplayedIndex() {
        PawPals pawPals = new PawPals();
        pawPals.addClient(ALICE);
        pawPals.addClient(new ClientBuilder(BENSON).withPets(MILO).build());
        Model model = new ModelManager(pawPals, new UserPrefs());
        model.updateFilteredClientList(client -> client.isSameClient(BENSON));

        DeleteNoteCommand command = new DeleteNoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"), NoteType.PREF);

        assertEquals(new CommandResult("Deleted preference note for Milo"),
                assertDoesNotThrow(() -> command.execute(model)));
        assertFalse(model.getPawPals().getClientList().get(1).getPet(new PetName("Milo")).orElseThrow()
                .hasNote(NoteType.PREF));
    }

    @Test
    public void execute_noSuchNote_throwsCommandException() {
        Model model = modelWithPet(MILO);
        DeleteNoteCommand command = new DeleteNoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"), NoteType.SENS);

        assertCommandFailure(command, model, "Milo has no sensitivity note to delete.");
    }

    @Test
    public void execute_unknownPet_throwsCommandException() {
        Model model = modelWithPet(MILO);
        DeleteNoteCommand command = new DeleteNoteCommand(INDEX_FIRST_CLIENT, new PetName("Luna"), NoteType.PREF);

        assertCommandFailure(command, model, "This client has no pet named Luna.");
    }

    @Test
    public void execute_invalidClientIndex_throwsCommandException() {
        Model model = modelWithPet(MILO);
        DeleteNoteCommand command = new DeleteNoteCommand(Index.fromOneBased(2), new PetName("Milo"), NoteType.PREF);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteNoteCommand(null, new PetName("Milo"), NoteType.PREF));
        assertThrows(NullPointerException.class, () -> new DeleteNoteCommand(INDEX_FIRST_CLIENT, null, NoteType.PREF));
        assertThrows(NullPointerException.class, () ->
                new DeleteNoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"), null));
    }

    @Test
    public void equals() {
        DeleteNoteCommand command = new DeleteNoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"), NoteType.TEMP);

        assertTrue(command.equals(command));
        assertTrue(command.equals(new DeleteNoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"), NoteType.TEMP)));
        assertEquals(command.hashCode(),
                new DeleteNoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"), NoteType.TEMP).hashCode());
        assertFalse(command.equals(new DeleteNoteCommand(Index.fromOneBased(2), new PetName("Milo"), NoteType.TEMP)));
        assertFalse(command.equals(new DeleteNoteCommand(INDEX_FIRST_CLIENT, new PetName("Luna"), NoteType.TEMP)));
        assertFalse(command.equals(new DeleteNoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"), NoteType.PREF)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new Object()));
    }

    @Test
    public void toStringMethod() {
        DeleteNoteCommand command = new DeleteNoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"), NoteType.TEMP);
        String expected = DeleteNoteCommand.class.getCanonicalName()
                + "{clientIndex=" + INDEX_FIRST_CLIENT + ", petName=Milo, noteType=TEMP}";

        assertEquals(expected, command.toString());
    }

    private static Model modelWithPet(Pet pet) {
        PawPals pawPals = new PawPals();
        pawPals.addClient(new ClientBuilder(ALICE).withPets(pet).build());
        return new ModelManager(pawPals, new UserPrefs());
    }
}
