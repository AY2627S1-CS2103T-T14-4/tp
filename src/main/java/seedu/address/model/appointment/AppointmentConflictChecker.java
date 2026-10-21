package seedu.address.model.appointment;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Optional;

import seedu.address.model.ReadOnlyPawPals;
import seedu.address.model.client.Client;

/**
 * Finds overlaps in all clients' active appointments, independently of the displayed client filter.
 * This check is read-only and deliberately ignores completed-session history.
 */
public final class AppointmentConflictChecker {
    private AppointmentConflictChecker() {
    }

    /**
     * Returns the first conflicting active appointment, in client-list and appointment-list order.
     */
    public static Optional<Conflict> findConflict(ReadOnlyPawPals pawPals, Appointment candidate) {
        return findConflict(pawPals, candidate, Optional.empty());
    }

    /**
     * Checks a proposed reschedule while excluding the original appointment belonging to its owner.
     * Exclusion uses client identity and appointment date/start time, rather than object identity or details.
     * Other appointments, including another client's appointment at the original slot, are still checked.
     * The caller must separately validate that the original client and appointment exist before updating them.
     */
    public static Optional<Conflict> findConflict(ReadOnlyPawPals pawPals, Appointment candidate,
            Client excludedClient, Appointment excludedAppointment) {
        return findConflict(pawPals, candidate, Optional.of(new Conflict(excludedClient, excludedAppointment)));
    }

    private static Optional<Conflict> findConflict(ReadOnlyPawPals pawPals, Appointment candidate,
            Optional<Conflict> excluded) {
        requireAllNonNull(pawPals, candidate);
        for (Client client : pawPals.getClientList()) {
            for (Appointment existing : client.getAppointments()) {
                if (excluded.isPresent() && excluded.get().client().isSameClient(client)
                        && excluded.get().appointment().hasSameDateTime(existing)) {
                    continue;
                }
                if (candidate.overlaps(existing)) {
                    return Optional.of(new Conflict(client, existing));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * An active booking and its owner, used for conflict feedback and rescheduling exclusions.
     *
     * @param client the appointment's owner.
     * @param appointment the active appointment.
     */
    public record Conflict(Client client, Appointment appointment) {
        public Conflict {
            requireAllNonNull(client, appointment);
        }
    }
}
