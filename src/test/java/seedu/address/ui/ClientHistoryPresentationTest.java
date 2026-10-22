package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxTestUtil.initializeToolkit;
import static seedu.address.testutil.JavaFxTestUtil.runOnFxThread;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import seedu.address.model.client.Client;
import seedu.address.model.history.CompletedSession;
import seedu.address.model.history.SessionNotes;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.HistoryFixtures;

/** Tests the history preview independently of active bookings and the current pet list. */
public class ClientHistoryPresentationTest {
    @BeforeAll
    public static void setUpJavaFxToolkit() throws InterruptedException {
        initializeToolkit();
    }

    @Test
    public void constructor_historyCountBoundaries_displaysNewestThreeWithoutRemovingRecords() throws Exception {
        runOnFxThread(() -> {
            List<List<String>> expected = List.of(
                    List.of(),
                    List.of("17-10-2026 09:00 · No pets linked", "Notes: Oldest visit"),
                    List.of("19-10-2026 11:00 · Luna", "Notes: Short trim",
                            "17-10-2026 09:00 · No pets linked", "Notes: Oldest visit"),
                    List.of("19-10-2026 11:00 · Luna", "Notes: Short trim",
                            "18-10-2026 10:00 · Milo", "Notes: Gentle shampoo",
                            "17-10-2026 09:00 · No pets linked", "Notes: Oldest visit"),
                    List.of("20-10-2026 14:00–15:30 · Milo, Luna", "Notes: Teddy cut, short ears",
                            "19-10-2026 11:00 · Luna", "Notes: Short trim",
                            "18-10-2026 10:00 · Milo", "Notes: Gentle shampoo"));
            for (int count = 0; count <= 4; count++) {
                Client client = HistoryFixtures.clientWithHistory(count);
                List<CompletedSession> before = List.copyOf(client.getHistory());
                Node card = new ClientCard(client, 1).getRoot();
                assertEquals(expected.get(count), historyTexts(card));
                assertEquals(before, client.getHistory());
                assertEquals(count, client.getHistory().size());
            }
            return null;
        });
    }

    @Test
    public void constructor_sameDateAndStart_keepsModelTieOrdering() throws Exception {
        runOnFxThread(() -> {
            CompletedSession older = HistoryFixtures.session("20-10-2026", "14:00", "Logged later");
            Client client = new ClientBuilder().withHistory(HistoryFixtures.NEWEST, older).build();
            assertEquals(List.of("20-10-2026 14:00 · No pets linked", "Notes: Logged later",
                    "20-10-2026 14:00–15:30 · Milo, Luna", "Notes: Teddy cut, short ears"),
                    historyTexts(new ClientCard(client, 1).getRoot()));
            return null;
        });
    }

    @Test
    public void constructor_renamedAndDeletedPets_keepsHistoricalNames() throws Exception {
        runOnFxThread(() -> {
            Pet milo = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
            Pet renamed = new Pet(new PetName("Milo Junior"), milo.getSpecies(), milo.getBreed());
            Client original = new ClientBuilder().withPets(milo).withHistory(HistoryFixtures.NEWEST).build();
            Client edited = original.withPetReplaced(milo.getName(), renamed);
            Client deleted = edited.withoutPet(renamed.getName());
            for (Client client : List.of(original, edited, deleted)) {
                assertEquals(List.of("20-10-2026 14:00–15:30 · Milo, Luna", "Notes: Teddy cut, short ears"),
                        historyTexts(new ClientCard(client, 1).getRoot()));
            }
            return null;
        });
    }

    @Test
    public void constructor_differentClients_displaysOnlyOwnHistory() throws Exception {
        runOnFxThread(() -> {
            Client first = new ClientBuilder().withHistory(HistoryFixtures.NEWEST).build();
            Client second = new ClientBuilder().withName("Another Client").withHistory(HistoryFixtures.OLDEST).build();
            assertEquals(List.of("20-10-2026 14:00–15:30 · Milo, Luna", "Notes: Teddy cut, short ears"),
                    historyTexts(new ClientCard(first, 1).getRoot()));
            assertEquals(List.of("17-10-2026 09:00 · No pets linked", "Notes: Oldest visit"),
                    historyTexts(new ClientCard(second, 2).getRoot()));
            return null;
        });
    }

    @Test
    public void layout_maximumNotesAndLongNames_wrapsWithoutClippingAtNarrowWidths() throws Exception {
        runOnFxThread(() -> {
            String notes = "G".repeat(SessionNotes.MAX_LENGTH);
            CompletedSession session = HistoryFixtures.session("20-10-2026", "14:00", notes,
                    "A".repeat(40), "B".repeat(40));
            Region card = new ClientCard(new ClientBuilder().withHistory(session).build(), 1).getRoot();
            StackPane root = new StackPane(card);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(ClientCard.class.getResource("/view/DarkTheme.css").toExternalForm());
            layout(root, 1000);
            List<Double> wideHeights = historyLabels(card).stream().map(Label::getHeight).toList();
            layout(root, 300);
            List<Label> labels = historyLabels(card);
            assertEquals("Notes: " + notes, labels.get(1).getText());
            for (int i = 0; i < labels.size(); i++) {
                Label label = labels.get(i);
                assertTrue(label.isWrapText());
                assertTrue(label.getHeight() > wideHeights.get(i));
                assertTrue(label.getWidth() <= card.getWidth());
                assertTrue(label.getHeight() >= label.prefHeight(label.getWidth()) - 1);
                assertTrue(card.sceneToLocal(label.localToScene(label.getBoundsInLocal())).getMaxY()
                        <= card.getHeight() + 1);
            }
            return null;
        });
    }

    private static void layout(StackPane root, double width) {
        root.resize(width, 1200);
        root.applyCss();
        root.layout();
    }

    private static List<Label> historyLabels(Node card) {
        VBox history = (VBox) card.lookup("#history");
        return history.getChildren().stream().map(node -> (VBox) node)
                .flatMap(row -> row.getChildren().stream()).map(node -> (Label) node).toList();
    }

    private static List<String> historyTexts(Node card) {
        return historyLabels(card).stream().map(Label::getText).toList();
    }
}
