package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.EmptyStateFixtures.CLIENT_WITH_ALL_NOTES;
import static seedu.address.testutil.EmptyStateFixtures.CLIENT_WITH_APPOINTMENT;
import static seedu.address.testutil.EmptyStateFixtures.CLIENT_WITH_HISTORY;
import static seedu.address.testutil.EmptyStateFixtures.CLIENT_WITH_PARTIAL_NOTES;
import static seedu.address.testutil.EmptyStateFixtures.CLIENT_WITH_PET;
import static seedu.address.testutil.EmptyStateFixtures.EMPTY_CLIENT;
import static seedu.address.testutil.JavaFxTestUtil.initializeToolkit;
import static seedu.address.testutil.JavaFxTestUtil.runOnFxThread;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.ClientDisplayMode;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;
import seedu.address.testutil.ClientBuilder;

public class ClientCardEmptyStateTest {
    @BeforeAll
    public static void setUpJavaFxToolkit() throws InterruptedException {
        initializeToolkit();
    }

    @Test
    public void constructor_nullClient_throwsNullPointerException() throws Exception {
        runOnFxThread(() -> {
            assertThrows(NullPointerException.class, () -> new ClientCard(null, 1));
            return null;
        });
    }

    @Test
    public void constructor_emptyClient_displaysOnlyApplicableEmptyStates() throws Exception {
        runOnFxThread(() -> {
            Region root = new ClientCard(EMPTY_CLIENT, 1).getRoot();
            assertEquals(List.of("No pets recorded", "No appointments scheduled", "No completed sessions"),
                    emptyLabels(root).stream().map(Label::getText).toList());
            return null;
        });
    }

    @Test
    public void constructor_populatedSections_omitTheirEmptyMessages() throws Exception {
        runOnFxThread(() -> {
            assertFalse(emptyTexts(CLIENT_WITH_PET).contains("No pets recorded"));
            assertTrue(emptyTexts(CLIENT_WITH_PET).contains("No notes recorded"));
            Region appointmentCard = new ClientCard(CLIENT_WITH_APPOINTMENT, 1,
                    ClientDisplayMode.SCHEDULE_CLIENT).getRoot();
            assertFalse(emptyLabels(appointmentCard).stream()
                    .map(Label::getText).toList().contains("No appointments scheduled"));
            Label emptyHistory = (Label) new ClientCard(CLIENT_WITH_HISTORY, 1).getRoot().lookup("#emptyHistory");
            assertFalse(emptyHistory.isVisible());
            assertFalse(emptyHistory.isManaged());
            assertTrue(emptyTexts(CLIENT_WITH_HISTORY).contains("No appointments scheduled"));
            return null;
        });
    }

    @Test
    public void constructor_partialAndFullNotes_omitGeneralNotePlaceholder() throws Exception {
        runOnFxThread(() -> {
            for (Client client : List.of(CLIENT_WITH_PARTIAL_NOTES, CLIENT_WITH_ALL_NOTES)) {
                Region root = new ClientCard(client, 1).getRoot();
                assertFalse(emptyTexts(client).contains("No notes recorded"));
                assertEquals(client.getPets().getFirst().getNotes().size(),
                        root.lookupAll(".cell_note_label").size());
                assertEquals(1, root.lookupAll(".cell_safety_note_label").stream()
                        .map(node -> (Label) node).filter(label -> label.getText().equals("⚠ Sens: Sensitive paws"))
                        .count());
            }
            return null;
        });
    }

    @Test
    public void constructor_multiplePets_keepsEmptyNoteMessageWithItsPet() throws Exception {
        runOnFxThread(() -> {
            Pet luna = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));
            Client client = new ClientBuilder(CLIENT_WITH_PARTIAL_NOTES).withPets(
                    CLIENT_WITH_PARTIAL_NOTES.getPets().getFirst(), luna).build();
            Region root = new ClientCard(client, 1).getRoot();
            List<Label> placeholders = emptyLabels(root).stream()
                    .filter(label -> label.getText().equals("No notes recorded")).toList();
            assertEquals(1, placeholders.size());
            for (Label placeholder : placeholders) {
                Parent petDetails = placeholder.getParent();
                assertEquals(2, petDetails.getChildrenUnmodifiable().size());
                Label petLabel = (Label) petDetails.getChildrenUnmodifiable().getFirst();
                assertEquals("Pet: Luna (Cat, Persian)", petLabel.getText());
            }
            return null;
        });
    }

    @Test
    public void constructor_emptyLabels_wrapWithinNarrowWidths() throws Exception {
        runOnFxThread(() -> {
            for (Client client : List.of(EMPTY_CLIENT, CLIENT_WITH_PET)) {
                Region root = new ClientCard(client, 1).getRoot();
                new Scene(root);
                root.applyCss();
                for (Label label : emptyLabels(root)) {
                    assertTrue(label.isWrapText());
                    assertEquals(0, label.getMinWidth());
                    assertEquals(Region.USE_PREF_SIZE, label.getMinHeight());
                    double narrowHeight = label.prefHeight(60);
                    label.resize(60, narrowHeight);
                    label.layout();
                    assertTrue(narrowHeight > label.prefHeight(300));
                }
            }
            return null;
        });
    }

    private static List<String> emptyTexts(Client client) {
        return emptyLabels(new ClientCard(client, 1).getRoot()).stream().map(Label::getText).toList();
    }

    private static List<Label> emptyLabels(Node node) {
        List<Label> labels = new ArrayList<>();
        if (node instanceof Label label && label.isVisible() && label.getStyleClass().contains("empty-state")) {
            labels.add(label);
        }
        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> labels.addAll(emptyLabels(child)));
        }
        return labels;
    }
}
