package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.EmptyStateFixtures.CLIENT_WITH_PET;
import static seedu.address.testutil.EmptyStateFixtures.EMPTY_CLIENT;
import static seedu.address.testutil.JavaFxTestUtil.initializeToolkit;
import static seedu.address.testutil.JavaFxTestUtil.runOnFxThread;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.client.Client;
import seedu.address.testutil.ClientBuilder;

public class ClientListPanelTest {
    @BeforeAll
    public static void setUpJavaFxToolkit() throws InterruptedException {
        initializeToolkit();
    }

    @Test
    public void constructor_singleList_tracksStoredClients() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Client> clients = FXCollections.observableArrayList();
            ClientListPanel panel = new ClientListPanel(clients);
            new Scene(panel.getRoot(), 340, 350);
            assertPlaceholder(panel, "No clients yet. Use addc to create a client.");
            clients.add(EMPTY_CLIENT);
            layout(panel);
            assertEquals(clients, listView(panel).getItems());
            assertFalse(isAttached(panel, listView(panel).getPlaceholder()));
            clients.clear();
            assertPlaceholder(panel, "No clients yet. Use addc to create a client.");
            assertThrows(NullPointerException.class, () -> new ClientListPanel(null));
            return null;
        });
    }

    @Test
    public void constructor_nullList_throwsNullPointerException() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Client> clients = FXCollections.observableArrayList();
            assertThrows(NullPointerException.class, () -> new ClientListPanel(null, clients));
            assertThrows(NullPointerException.class, () -> new ClientListPanel(clients, null));
            return null;
        });
    }

    @Test
    public void placeholder_listAndFilterChanges_displaysCorrectEmptyState() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Client> clients = FXCollections.observableArrayList();
            FilteredList<Client> filtered = new FilteredList<>(clients);
            ClientListPanel panel = new ClientListPanel(filtered, clients);
            new Scene(panel.getRoot(), 340, 350);

            assertPlaceholder(panel, "No clients yet. Use addc to create a client.");
            clients.add(EMPTY_CLIENT);
            layout(panel);
            assertFalse(isAttached(panel, listView(panel).getPlaceholder()));

            filtered.setPredicate(unused -> false);
            assertPlaceholder(panel, "No matching clients. Try another search or use list.");
            clients.clear();
            assertPlaceholder(panel, "No clients yet. Use addc to create a client.");
            clients.add(CLIENT_WITH_PET);
            assertPlaceholder(panel, "No matching clients. Try another search or use list.");

            filtered.setPredicate(unused -> true);
            layout(panel);
            assertFalse(isAttached(panel, listView(panel).getPlaceholder()));
            clients.clear();
            assertPlaceholder(panel, "No clients yet. Use addc to create a client.");
            return null;
        });
    }

    @Test
    public void placeholder_narrowPanel_wrapsInsideListView() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Client> clients = FXCollections.observableArrayList(EMPTY_CLIENT);
            FilteredList<Client> filtered = new FilteredList<>(clients, unused -> false);
            ClientListPanel panel = new ClientListPanel(filtered, clients);
            new Scene(panel.getRoot(), 220, 350);
            panel.getRoot().resize(220, 350);
            layout(panel);

            Label placeholder = (Label) listView(panel).getPlaceholder();
            assertTrue(placeholder.isWrapText());
            assertTrue(placeholder.getWidth() <= listView(panel).getWidth());
            assertTrue(placeholder.getHeight() >= placeholder.prefHeight(placeholder.getWidth()));
            assertTrue(placeholder.getHeight() > placeholder.prefHeight(600));
            return null;
        });
    }

    @Test
    public void updateItem_reusedCell_removesPreviousClientAndEmptyState() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Client> clients = FXCollections.observableArrayList(EMPTY_CLIENT, CLIENT_WITH_PET);
            ClientListPanel panel = new ClientListPanel(clients, clients);
            ClientListPanel.ClientListViewCell cell = panel.new ClientListViewCell();
            cell.updateItem(EMPTY_CLIENT, false);
            Region previousCard = (Region) cell.getGraphic();
            assertEquals(1, previousCard.lookupAll("#pets .empty-state").size());

            cell.updateItem(CLIENT_WITH_PET, false);
            Region currentCard = (Region) cell.getGraphic();
            assertFalse(previousCard == currentCard);
            assertTrue(currentCard.lookupAll("#pets .empty-state").stream()
                    .map(node -> ((Label) node).getText()).noneMatch("No pets recorded"::equals));
            cell.updateItem(null, true);
            assertNull(cell.getGraphic());
            assertNull(cell.getText());
            return null;
        });
    }

    @Test
    public void clientFields_narrowThenWidePanel_wrapAndReflowWithoutClipping() throws Exception {
        runOnFxThread(() -> {
            // Equivalence Partition: text whose rendered width exceeds the available label width.
            String address = "Block 123 Clementi Avenue 4, Tower B, Level 12, Unit 1234, Singapore 120123, "
                    + "entrance beside the community centre, use the rear lift lobby";
            // Boundary Value Analysis: upper boundary of valid name length (60 characters).
            String name = "W".repeat(60);
            String email = "alexandra.catherine.elizabeth.victoria.charlotte".repeat(2) + "@example.com";
            String longTag = "LongTag".repeat(15);
            Client client = new ClientBuilder().withName(name).withEmail(email).withAddress(address)
                    .withTags(longTag, "VIP", "Regular").build();
            Map<String, String> fields = Map.of("#name", name, "#email", email, "#address", address,
                    "#tags .label", longTag);
            ClientListPanel panel = new ClientListPanel(FXCollections.observableArrayList(client));
            Scene scene = new Scene(panel.getRoot(), 420, 600);
            scene.getStylesheets().addAll(
                    getClass().getResource("/view/DarkTheme.css").toExternalForm(),
                    getClass().getResource("/view/Extensions.css").toExternalForm());
            resizeAndLayout(panel, 420);

            Map<String, Double> narrowHeights = new HashMap<>();
            fields.forEach((selector, text) -> {
                Label label = assertReadableLabel(panel, selector, text);
                assertTrue(label.getHeight() > label.prefHeight(1800), text);
                narrowHeights.put(selector, label.getHeight());
            });
            Label index = (Label) panel.getRoot().lookup("#id");
            assertEquals("1. ", index.getText());
            assertTrue(index.getWidth() >= index.prefWidth(-1));
            // Equivalence Partition: tags whose rendered width fits within the available label width.
            for (String tag : new String[] {"VIP", "Regular"}) {
                Label label = assertReadableLabel(panel, "#tags .label", tag);
                assertEquals(label.prefWidth(-1), label.getWidth(), 1);
                assertEquals(label.prefHeight(1800), label.getHeight(), 1);
            }

            resizeAndLayout(panel, 1800);
            fields.forEach((selector, text) -> {
                Label label = assertReadableLabel(panel, selector, text);
                assertTrue(label.getHeight() < narrowHeights.get(selector), text);
            });
            return null;
        });
    }

    /**
     * Resizes the panel and completes both the width-binding and wrapped-height layout passes.
     */
    private static void resizeAndLayout(ClientListPanel panel, double width) {
        panel.getRoot().resize(width, 600);
        layout(panel);
        // A second pass measures wrapped tag heights after the container's width binding updates.
        layout(panel);
    }

    /**
     * Finds the expected label among selector matches and checks wrapping, allocated height, and parent containment.
     * Returns the label for assertions specific to the current panel width.
     */
    private static Label assertReadableLabel(ClientListPanel panel, String selector, String expectedText) {
        Label label = panel.getRoot().lookupAll(selector).stream()
                .map(node -> (Label) node)
                .filter(candidate -> candidate.getText().equals(expectedText))
                .findFirst().orElseThrow();
        assertTrue(label.isWrapText(), expectedText);
        assertTrue(label.getWidth() > 0, expectedText);
        assertTrue(label.getHeight() >= label.prefHeight(label.getWidth()), expectedText);
        assertTrue(label.getBoundsInParent().getMinX() >= 0, expectedText);
        assertTrue(label.getBoundsInParent().getMaxX() <= label.getParent().getLayoutBounds().getWidth() + 1,
                expectedText);
        assertTrue(label.getBoundsInParent().getMaxY() <= label.getParent().getLayoutBounds().getHeight() + 1,
                expectedText + ": bottom=" + label.getBoundsInParent().getMaxY()
                        + ", parent height=" + label.getParent().getLayoutBounds().getHeight());
        return label;
    }

    /**
     * Checks the placeholder text and that its visible parent chain reaches the panel root after layout.
     */
    private static void assertPlaceholder(ClientListPanel panel, String expected) {
        layout(panel);
        assertEquals(expected, ((Label) listView(panel).getPlaceholder()).getText());
        assertTrue(isAttached(panel, listView(panel).getPlaceholder()));
    }

    private static void layout(ClientListPanel panel) {
        panel.getRoot().applyCss();
        panel.getRoot().layout();
    }

    private static ListView<?> listView(ClientListPanel panel) {
        return (ListView<?>) panel.getRoot().lookup("#clientListView");
    }

    /**
     * Returns whether the node is connected to the panel root through visible ancestors.
     */
    private static boolean isAttached(ClientListPanel panel, Node node) {
        if (node == panel.getRoot()) {
            return true;
        }
        return node.getParent() != null && node.isVisible() && isAttached(panel, node.getParent());
    }
}
