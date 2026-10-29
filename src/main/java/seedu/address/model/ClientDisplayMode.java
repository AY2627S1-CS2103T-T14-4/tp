package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.model.appointment.AppointmentDate;

/** Describes how client cards should present appointments and completed-session history. */
public final class ClientDisplayMode {
    /** Supported client-card presentation modes. */
    public enum Type {
        NORMAL,
        SCHEDULE_DATE,
        SCHEDULE_CLIENT
    }

    public static final ClientDisplayMode NORMAL = new ClientDisplayMode(Type.NORMAL, null);
    public static final ClientDisplayMode SCHEDULE_CLIENT = new ClientDisplayMode(Type.SCHEDULE_CLIENT, null);

    private final Type type;
    private final AppointmentDate date;

    private ClientDisplayMode(Type type, AppointmentDate date) {
        this.type = requireNonNull(type);
        this.date = date;
    }

    /** Returns a mode that shows active appointments on {@code date}. */
    public static ClientDisplayMode scheduleDate(AppointmentDate date) {
        return new ClientDisplayMode(Type.SCHEDULE_DATE, requireNonNull(date));
    }

    public Type getType() {
        return type;
    }

    public Optional<AppointmentDate> getDate() {
        return Optional.ofNullable(date);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof ClientDisplayMode otherMode
                && type == otherMode.type && Objects.equals(date, otherMode.date));
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, date);
    }
}
