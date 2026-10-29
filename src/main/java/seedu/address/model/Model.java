package seedu.address.model;

import java.util.Optional;
import java.util.function.Predicate;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentConflictChecker.Conflict;
import seedu.address.model.client.Client;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Client> PREDICATE_SHOW_ALL_CLIENTS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces PawPals data data with the data in {@code pawPals}.
     */
    void setPawPals(ReadOnlyPawPals pawPals);

    /** Returns the PawPals */
    ReadOnlyPawPals getPawPals();

    /**
     * Returns true if a client with the same identity as {@code client} exists in the PawPals data.
     */
    boolean hasClient(Client client);

    /**
     * Deletes the given client.
     * The client must exist in the PawPals data.
     */
    void deleteClient(Client target);

    /**
     * Adds the given client.
     * {@code client} must not already exist in the PawPals data.
     */
    void addClient(Client client);

    /**
     * Replaces the given client {@code target} with {@code editedClient}.
     * {@code target} must exist in the PawPals data.
     * The client identity of {@code editedClient} must not be the same as another existing client in the PawPals data.
     */
    void setClient(Client target, Client editedClient);

    /**
     * Returns the first active booking that overlaps {@code candidate}, across all clients regardless of filtering.
     * Completed-session history is ignored. Does not change data or the current filter.
     *
     * @throws NullPointerException if {@code candidate} is null.
     */
    Optional<Conflict> findAppointmentConflict(Appointment candidate);

    /**
     * Checks a reschedule, excluding only the original client's appointment identified by its date and start time.
     * All other active bookings are checked, regardless of filtering; completed-session history is ignored.
     * The caller must separately validate that the original client and appointment exist before updating them.
     * Does not change data or the current filter.
     *
     * @throws NullPointerException if any argument is null.
     */
    Optional<Conflict> findAppointmentConflict(Appointment candidate, Client excludedClient,
            Appointment excludedAppointment);

    /** Returns an unmodifiable view of the filtered client list */
    ObservableList<Client> getFilteredClientList();

    /**
     * Updates the filter of the filtered client list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredClientList(Predicate<Client> predicate);

    /** Returns an observable view of the current client-card presentation mode. */
    ObservableValue<ClientDisplayMode> clientDisplayModeProperty();

    /** Changes how client cards present appointments and history. */
    void setClientDisplayMode(ClientDisplayMode mode);
}
