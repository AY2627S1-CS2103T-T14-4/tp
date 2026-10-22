package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.PetNote;
import seedu.address.model.pet.Species;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.JavaFxTestUtil;

public class ClientCardTest {
    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
    private static final Pet LUNA = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));

    @BeforeAll
    public static void setUpJavaFxToolkit() throws Exception {
        JavaFxTestUtil.startToolkit();
    }

    @Test
    public void constructor_clientWithPets_displaysPetDetailsInOrder() throws Exception {
        JavaFxTestUtil.runAndWait(() -> {
            Client client = new ClientBuilder().withPets(MILO, LUNA).build();

            List<String> labels = getSmallLabelTexts(new ClientCard(client, 1).getRoot());

            String miloText = "Pet: Milo (Dog, Poodle)";
            String lunaText = "Pet: Luna (Cat, Persian)";
            assertTrue(labels.contains(miloText));
            assertTrue(labels.contains(lunaText));
            assertTrue(labels.indexOf(miloText) < labels.indexOf(lunaText));
        });
    }

    @Test
    public void constructor_clientWithoutPets_doesNotDisplayPetDetails() throws Exception {
        JavaFxTestUtil.runAndWait(() -> {
            Client client = new ClientBuilder().build();

            List<String> labels = getSmallLabelTexts(new ClientCard(client, 1).getRoot());

            assertFalse(labels.stream().anyMatch(label -> label.startsWith("Pet: ")));
        });
    }

    @Test
    public void constructor_differentClients_displayOnlyTheirOwnPets() throws Exception {
        JavaFxTestUtil.runAndWait(() -> {
            Client alice = new ClientBuilder().withName("Alice Test").withPets(MILO).build();
            Client bob = new ClientBuilder().withName("Bob Test").withPets(LUNA).build();

            List<String> aliceLabels = getSmallLabelTexts(new ClientCard(alice, 1).getRoot());
            List<String> bobLabels = getSmallLabelTexts(new ClientCard(bob, 2).getRoot());

            assertTrue(aliceLabels.contains("Pet: Milo (Dog, Poodle)"));
            assertFalse(aliceLabels.contains("Pet: Luna (Cat, Persian)"));
            assertTrue(bobLabels.contains("Pet: Luna (Cat, Persian)"));
            assertFalse(bobLabels.contains("Pet: Milo (Dog, Poodle)"));
        });
    }

    @Test
    public void constructor_petWithNotes_displaysLabelsAndSafetyMarkers() throws Exception {
        JavaFxTestUtil.runAndWait(() -> {
            Pet pet = MILO.withNote(NoteType.PREF, new PetNote("Likes quiet grooming"))
                    .withNote(NoteType.SENS, new PetNote("Sensitive paws"))
                    .withNote(NoteType.TEMP, new PetNote("Anxious around dryers"));
            Client client = new ClientBuilder().withPets(pet).build();

            Node root = new ClientCard(client, 1).getRoot();
            List<String> noteLabels = getLabelsWithClass(root, "cell_note_label");
            List<String> safetyLabels = getLabelsWithClass(root, "cell_safety_note_label");

            assertTrue(noteLabels.contains("Pref: Likes quiet grooming"));
            assertTrue(noteLabels.contains("⚠ Sens: Sensitive paws"));
            assertTrue(noteLabels.contains("⚠ Temp: Anxious around dryers"));
            assertTrue(safetyLabels.contains("⚠ Sens: Sensitive paws"));
            assertTrue(safetyLabels.contains("⚠ Temp: Anxious around dryers"));
        });
    }

    @Test
    public void constructor_petWithOneNote_omitsMissingNoteLabels() throws Exception {
        JavaFxTestUtil.runAndWait(() -> {
            Pet pet = MILO.withNote(NoteType.SENS, new PetNote("Sensitive paws"));
            Client client = new ClientBuilder().withPets(pet).build();

            List<String> noteLabels = getLabelsWithClass(new ClientCard(client, 1).getRoot(), "cell_note_label");

            assertTrue(noteLabels.contains("⚠ Sens: Sensitive paws"));
            assertFalse(noteLabels.stream().anyMatch(label -> label.startsWith("Pref: ")));
            assertFalse(noteLabels.stream().anyMatch(label -> label.startsWith("⚠ Temp: ")));
        });
    }

    @Test
    public void constructor_multiplePets_keepsNotesWithTheirPet() throws Exception {
        JavaFxTestUtil.runAndWait(() -> {
            Pet milo = MILO.withNote(NoteType.SENS, new PetNote("Sensitive paws"));
            Pet luna = LUNA.withNote(NoteType.PREF, new PetNote("Likes short trims"));
            Client client = new ClientBuilder().withPets(milo, luna).build();

            List<String> labels = getLabelsWithClass(new ClientCard(client, 1).getRoot(), "cell_note_label");

            assertTrue(labels.contains("⚠ Sens: Sensitive paws"));
            assertTrue(labels.contains("Pref: Likes short trims"));
            assertEquals(2, labels.size());
        });
    }

    @Test
    public void constructor_longNote_preservesTextAndEnablesWrapping() throws Exception {
        JavaFxTestUtil.runAndWait(() -> {
            String longNote = "a".repeat(PetNote.MAX_LENGTH);
            Pet pet = MILO.withNote(NoteType.PREF, new PetNote(longNote));
            Client client = new ClientBuilder().withPets(pet).build();

            Node root = new ClientCard(client, 1).getRoot();
            Label noteLabel = getLabelsWithClass(root, "cell_note_label").stream()
                    .map(text -> findLabel(root, text))
                    .filter(label -> label != null)
                    .findFirst()
                    .orElseThrow();

            assertTrue(noteLabel.isWrapText());
            assertTrue(noteLabel.getText().contains(longNote));
        });
    }

    private List<String> getSmallLabelTexts(Node node) {
        List<String> labels = new ArrayList<>();
        if (node instanceof Label label && label.getStyleClass().contains("cell_small_label")) {
            labels.add(label.getText());
        }
        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> labels.addAll(getSmallLabelTexts(child)));
        }
        return labels;
    }

    private List<String> getLabelsWithClass(Node node, String styleClass) {
        List<String> labels = new ArrayList<>();
        if (node instanceof Label label && label.getStyleClass().contains(styleClass)) {
            labels.add(label.getText());
        }
        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> labels.addAll(getLabelsWithClass(child, styleClass)));
        }
        return labels;
    }

    private Label findLabel(Node node, String text) {
        if (node instanceof Label label && label.getStyleClass().contains("cell_note_label")
                && label.getText().equals(text)) {
            return label;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                Label label = findLabel(child, text);
                if (label != null) {
                    return label;
                }
            }
        }
        return null;
    }
}
