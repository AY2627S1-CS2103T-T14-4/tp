package seedu.address.testutil;

import seedu.address.model.PawPals;
import seedu.address.model.client.Client;

/**
 * A utility class to help with building PawPals objects.
 * Example usage: <br>
 *     {@code PawPals ab = new PawPalsBuilder().withClient("John", "Doe").build();}
 */
public class PawPalsBuilder {

    private PawPals pawPals;

    public PawPalsBuilder() {
        pawPals = new PawPals();
    }

    public PawPalsBuilder(PawPals pawPals) {
        this.pawPals = pawPals;
    }

    /**
     * Adds a new {@code Client} to the {@code PawPals} that we are building.
     */
    public PawPalsBuilder withClient(Client client) {
        pawPals.addClient(client);
        return this;
    }

    public PawPals build() {
        return pawPals;
    }
}
