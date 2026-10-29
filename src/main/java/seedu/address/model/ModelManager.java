package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentConflictChecker;
import seedu.address.model.appointment.AppointmentConflictChecker.Conflict;
import seedu.address.model.client.Client;

/**
 * Represents the in-memory model of the PawPals data data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final PawPals pawPals;
    private final UserPrefs userPrefs;
    private final FilteredList<Client> filteredClients;
    private final ObjectProperty<ClientDisplayMode> clientDisplayMode =
            new SimpleObjectProperty<>(ClientDisplayMode.NORMAL);

    /**
     * Initializes a ModelManager with the given pawPals and userPrefs.
     */
    public ModelManager(ReadOnlyPawPals pawPals, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(pawPals, userPrefs);

        logger.fine("Initializing with client data: " + pawPals + " and user prefs " + userPrefs);

        this.pawPals = new PawPals(pawPals);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredClients = new FilteredList<>(this.pawPals.getClientList());
    }

    public ModelManager() {
        this(new PawPals(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== PawPals ================================================================================

    @Override
    public void setPawPals(ReadOnlyPawPals pawPals) {
        this.pawPals.resetData(pawPals);
    }

    @Override
    public ReadOnlyPawPals getPawPals() {
        return pawPals;
    }

    @Override
    public boolean hasClient(Client client) {
        requireNonNull(client);
        return pawPals.hasClient(client);
    }

    @Override
    public void deleteClient(Client target) {
        pawPals.removeClient(target);
    }

    @Override
    public void addClient(Client client) {
        pawPals.addClient(client);
        updateFilteredClientList(PREDICATE_SHOW_ALL_CLIENTS);
    }

    @Override
    public void setClient(Client target, Client editedClient) {
        requireAllNonNull(target, editedClient);

        pawPals.setClient(target, editedClient);
    }

    //=========== Appointment Conflict Checks ================================================================

    @Override
    public Optional<Conflict> findAppointmentConflict(Appointment candidate) {
        return AppointmentConflictChecker.findConflict(pawPals, candidate);
    }

    @Override
    public Optional<Conflict> findAppointmentConflict(Appointment candidate, Client excludedClient,
            Appointment excludedAppointment) {
        return AppointmentConflictChecker.findConflict(pawPals, candidate, excludedClient, excludedAppointment);
    }

    //=========== Filtered Client List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Client} backed by the internal list of
     * {@code pawPals}
     */
    @Override
    public ObservableList<Client> getFilteredClientList() {
        return filteredClients;
    }

    @Override
    public void updateFilteredClientList(Predicate<Client> predicate) {
        requireNonNull(predicate);
        filteredClients.setPredicate(predicate);
    }

    @Override
    public ObservableValue<ClientDisplayMode> clientDisplayModeProperty() {
        return clientDisplayMode;
    }

    @Override
    public void setClientDisplayMode(ClientDisplayMode mode) {
        clientDisplayMode.set(requireNonNull(mode));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return pawPals.equals(otherModelManager.pawPals)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredClients.equals(otherModelManager.filteredClients);
    }

}
