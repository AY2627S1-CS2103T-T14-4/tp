package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.client.Client;
import seedu.address.model.client.UniqueClientList;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSameClient comparison).
 */
public class PawPals implements ReadOnlyPawPals {

    private final UniqueClientList clients = new UniqueClientList();

    public PawPals() {}

    /**
     * Creates an PawPals using the Clients in the {@code toBeCopied}
     */
    public PawPals(ReadOnlyPawPals toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the client list with {@code clients}.
     * {@code clients} must not contain duplicate clients.
     */
    public void setClients(List<Client> clients) {
        this.clients.setClients(clients);
    }

    /**
     * Resets the existing data of this {@code PawPals} with {@code newData}.
     */
    public void resetData(ReadOnlyPawPals newData) {
        requireNonNull(newData);

        setClients(newData.getClientList());
    }

    //// client-level operations

    /**
     * Returns true if a client with the same identity as {@code client} exists in the PawPals data.
     */
    public boolean hasClient(Client client) {
        requireNonNull(client);
        return clients.contains(client);
    }

    /**
     * Adds a client to the PawPals data.
     * The client must not already exist in the PawPals data.
     */
    public void addClient(Client p) {
        clients.add(p);
    }

    /**
     * Replaces the given client {@code target} in the list with {@code editedClient}.
     * {@code target} must exist in the PawPals data.
     * The client identity of {@code editedClient} must not be the same as another existing client in the PawPals data.
     */
    public void setClient(Client target, Client editedClient) {
        requireNonNull(editedClient);

        clients.setClient(target, editedClient);
    }

    /**
     * Removes {@code key} from this {@code PawPals}.
     * {@code key} must exist in the PawPals data.
     */
    public void removeClient(Client key) {
        clients.remove(key);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("clients", clients)
                .toString();
    }

    @Override
    public ObservableList<Client> getClientList() {
        return clients.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof PawPals otherPawPals)) {
            return false;
        }

        return clients.equals(otherPawPals.clients);
    }

    @Override
    public int hashCode() {
        return clients.hashCode();
    }
}
