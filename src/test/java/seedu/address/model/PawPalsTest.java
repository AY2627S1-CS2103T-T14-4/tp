package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.getTypicalPawPals;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.client.Client;
import seedu.address.model.client.exceptions.DuplicateClientException;
import seedu.address.testutil.ClientBuilder;

public class PawPalsTest {

    private final PawPals pawPals = new PawPals();

    @Test
    public void constructor() {
        assertEquals(List.of(), pawPals.getClientList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> pawPals.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyPawPals_replacesData() {
        PawPals newData = getTypicalPawPals();
        pawPals.resetData(newData);
        assertEquals(newData, pawPals);
    }

    @Test
    public void resetData_withDuplicateClients_throwsDuplicateClientException() {
        // Two clients with the same identity fields
        Client editedAlice = new ClientBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Client> newClients = List.of(ALICE, editedAlice);
        PawPalsStub newData = new PawPalsStub(newClients);

        assertThrows(DuplicateClientException.class, () -> pawPals.resetData(newData));
    }

    @Test
    public void hasClient_nullClient_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> pawPals.hasClient(null));
    }

    @Test
    public void hasClient_clientNotInPawPals_returnsFalse() {
        assertFalse(pawPals.hasClient(ALICE));
    }

    @Test
    public void hasClient_clientInPawPals_returnsTrue() {
        pawPals.addClient(ALICE);
        assertTrue(pawPals.hasClient(ALICE));
    }

    @Test
    public void hasClient_clientWithSameIdentityFieldsInPawPals_returnsTrue() {
        pawPals.addClient(ALICE);
        Client editedAlice = new ClientBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(pawPals.hasClient(editedAlice));
    }

    @Test
    public void getClientList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> pawPals.getClientList().remove(0));
    }

    @Test
    public void toStringMethod() {
        String expected = PawPals.class.getCanonicalName() + "{clients=" + pawPals.getClientList() + "}";
        assertEquals(expected, pawPals.toString());
    }

    /**
     * A stub ReadOnlyPawPals whose clients list can violate interface constraints.
     */
    private static class PawPalsStub implements ReadOnlyPawPals {
        private final ObservableList<Client> clients = FXCollections.observableArrayList();

        PawPalsStub(Collection<Client> clients) {
            this.clients.setAll(clients);
        }

        @Override
        public ObservableList<Client> getClientList() {
            return clients;
        }
    }

}
