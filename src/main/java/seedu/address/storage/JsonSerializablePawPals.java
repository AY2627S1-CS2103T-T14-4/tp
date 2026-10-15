package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.PawPals;
import seedu.address.model.ReadOnlyPawPals;
import seedu.address.model.client.Client;

/**
 * An Immutable PawPals that is serializable to JSON format.
 */
@JsonRootName(value = "pawpals")
class JsonSerializablePawPals {

    public static final String MESSAGE_DUPLICATE_CLIENT = "Clients list contains duplicate client(s).";

    private final List<JsonAdaptedClient> clients = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializablePawPals} with the given clients.
     */
    @JsonCreator
    public JsonSerializablePawPals(@JsonProperty("clients") List<JsonAdaptedClient> clients) {
        this.clients.addAll(clients);
    }

    /**
     * Converts a given {@code ReadOnlyPawPals} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializablePawPals}.
     */
    public JsonSerializablePawPals(ReadOnlyPawPals source) {
        clients.addAll(source.getClientList().stream().map(JsonAdaptedClient::new).collect(Collectors.toList()));
    }

    /**
     * Converts this PawPals data into the model's {@code PawPals} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public PawPals toModelType() throws IllegalValueException {
        PawPals pawPals = new PawPals();
        for (JsonAdaptedClient jsonAdaptedClient : clients) {
            Client client = jsonAdaptedClient.toModelType();
            if (pawPals.hasClient(client)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_CLIENT);
            }
            pawPals.addClient(client);
        }
        return pawPals;
    }

}
