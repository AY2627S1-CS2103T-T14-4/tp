package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import java.util.Optional;

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

public class NoteCommandTest {

    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));

    @Test
    public void execute_setNote_success() {
        Model model = modelWithPet(MILO);
        Model expectedModel = modelWithPet(MILO);
        Client targetClient = expectedModel.getFilteredClientList().get(0);
        expectedModel.setClient(targetClient, targetClient.withPetReplaced(MILO.getName(),
                MILO.withNote(NoteType.TEMP, new PetNote("Anxious around loud dryers"))));

        NoteCommand command = new NoteCommand(INDEX_FIRST_CLIENT, new PetName("milo"), NoteType.TEMP,
                Optional.of(new PetNote("Anxious around loud dryers")));

        assertCommandSuccess(command, model, "Updated temperament note for Milo: Anxious around loud dryers",
                expectedModel);
    }

    @Test
    public void execute_replaceNote_success() {
        Pet petWithNote = MILO.withNote(NoteType.PREF, new PetNote("Use a soft brush"));
        Model model = modelWithPet(petWithNote);
        Model expectedModel = modelWithPet(petWithNote);
        Client targetClient = expectedModel.getFilteredClientList().get(0);
        expectedModel.setClient(targetClient, targetClient.withPetReplaced(MILO.getName(),
                petWithNote.withNote(NoteType.PREF, new PetNote("Avoid scented shampoo"))));

        NoteCommand command = new NoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"), NoteType.PREF,
                Optional.of(new PetNote("Avoid scented shampoo")));

        assertCommandSuccess(command, model, "Updated preference note for Milo: Avoid scented shampoo",
                expectedModel);
    }

    @Test
    public void execute_clearNote_success() {
        Pet petWithNote = MILO.withNote(NoteType.TEMP, new PetNote("Nervous around strangers"));
        Model model = modelWithPet(petWithNote);
        Model expectedModel = modelWithPet(petWithNote);
        Client targetClient = expectedModel.getFilteredClientList().get(0);
        expectedModel.setClient(targetClient, targetClient.withPetReplaced(MILO.getName(),
                petWithNote.withoutNote(NoteType.TEMP)));

        NoteCommand command = new NoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"), NoteType.TEMP,
                Optional.empty());

        assertCommandSuccess(command, model, "Cleared temperament note for Milo", expectedModel);
    }

    @Test
    public void execute_clearMissingNote_throwsCommandException() {
        Model model = modelWithPet(MILO);
        NoteCommand command = new NoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"), NoteType.SENS,
                Optional.empty());

        assertCommandFailure(command, model, "Milo has no sensitivity note to clear.");
    }

    @Test
    public void execute_unknownPet_throwsCommandException() {
        Model model = modelWithPet(MILO);
        NoteCommand command = new NoteCommand(INDEX_FIRST_CLIENT, new PetName("Luna"), NoteType.PREF,
                Optional.of(new PetNote("Likes treats")));

        assertCommandFailure(command, model, "This client has no pet named Luna.");
    }

    @Test
    public void execute_invalidClientIndex_throwsCommandException() {
        Model model = modelWithPet(MILO);
        NoteCommand command = new NoteCommand(Index.fromOneBased(2), new PetName("Milo"), NoteType.PREF,
                Optional.of(new PetNote("Likes treats")));

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_setNote_preservesOtherNotes() {
        Pet petWithSafetyNote = MILO.withNote(NoteType.SENS, new PetNote("Sensitive paws"));
        Model model = modelWithPet(petWithSafetyNote);
        NoteCommand command = new NoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"), NoteType.TEMP,
                Optional.of(new PetNote("Afraid of dryers")));

        assertEquals(new CommandResult("Updated temperament note for Milo: Afraid of dryers"),
                assertDoesNotThrow(() -> command.execute(model)));

        Pet updatedPet = model.getFilteredClientList().get(0).getPet(new PetName("Milo")).orElseThrow();
        assertEquals(new PetNote("Sensitive paws"), updatedPet.getNote(NoteType.SENS).orElseThrow());
        assertEquals(new PetNote("Afraid of dryers"), updatedPet.getNote(NoteType.TEMP).orElseThrow());
        assertTrue(updatedPet.hasSafetyNote());
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new NoteCommand(null, new PetName("Milo"), NoteType.PREF,
                Optional.empty()));
        assertThrows(NullPointerException.class, () -> new NoteCommand(INDEX_FIRST_CLIENT, null, NoteType.PREF,
                Optional.empty()));
        assertThrows(NullPointerException.class, () -> new NoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"),
                null, Optional.empty()));
        assertThrows(NullPointerException.class, () -> new NoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"),
                NoteType.PREF, null));
    }

    private static Model modelWithPet(Pet pet) {
        Client clientWithPet = new ClientBuilder(ALICE).build().withPet(pet);
        PawPals pawPals = new PawPals();
        pawPals.addClient(clientWithPet);
        return new ModelManager(pawPals, new UserPrefs());
    }
}
