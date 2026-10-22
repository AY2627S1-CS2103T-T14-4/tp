package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.client.Client;

/**
 * Panel containing the list of clients.
 */
public class ClientListPanel extends UiPart<Region> {
    private static final String FXML = "ClientListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(ClientListPanel.class);

    @FXML
    private ListView<Client> clientListView;

    @FXML
    private Label emptyListLabel;

    /**
     * Creates a panel for a stored client list that is also the displayed list.
     *
     * @throws NullPointerException if {@code clientList} is null.
     */
    public ClientListPanel(ObservableList<Client> clientList) {
        this(clientList, clientList);
    }

    /**
     * Creates a panel showing {@code filteredClients}, using {@code allClients} to distinguish an empty
     * address book from a search without matches.
     *
     * @throws NullPointerException if either list is null.
     */
    public ClientListPanel(ObservableList<Client> filteredClients, ObservableList<Client> allClients) {
        super(FXML);
        requireAllNonNull(filteredClients, allClients);
        clientListView.setItems(filteredClients);
        emptyListLabel.textProperty().bind(Bindings.when(Bindings.isEmpty(allClients))
                .then("No clients yet. Use addc to create a client.")
                .otherwise("No matching clients. Try another search or use list."));
        emptyListLabel.maxWidthProperty().bind(Bindings.max(0, clientListView.widthProperty().subtract(40)));
        clientListView.setCellFactory(listView -> new ClientListViewCell());
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Client} using a {@code ClientCard}.
     */
    class ClientListViewCell extends ListCell<Client> {
        @Override
        protected void updateItem(Client client, boolean empty) {
            super.updateItem(client, empty);

            if (getGraphic() instanceof Region previousCard) {
                previousCard.prefWidthProperty().unbind();
                previousCard.maxWidthProperty().unbind();
            }
            setText(null);
            if (empty || client == null) {
                setGraphic(null);
            } else {
                Region card = new ClientCard(client, getIndex() + 1).getRoot();
                card.prefWidthProperty().bind(Bindings.createDoubleBinding(() -> Math.max(0,
                        getWidth() - getInsets().getLeft() - getInsets().getRight()),
                        widthProperty(), insetsProperty()));
                card.maxWidthProperty().bind(card.prefWidthProperty());
                setGraphic(card);
            }
        }
    }

}
