package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.EmptyStateFixtures.CLIENT_WITH_PET;
import static seedu.address.testutil.EmptyStateFixtures.EMPTY_CLIENT;
import static seedu.address.testutil.JavaFxTestUtil.initializeToolkit;
import static seedu.address.testutil.JavaFxTestUtil.runOnFxThread;

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

public class ClientListPanelTest {
    @BeforeAll
    public static void setUpJavaFxToolkit() throws InterruptedException {
        initializeToolkit();
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

    private static boolean isAttached(ClientListPanel panel, Node node) {
        if (node == panel.getRoot()) {
            return true;
        }
        return node.getParent() != null && node.isVisible() && isAttached(panel, node.getParent());
    }
}
