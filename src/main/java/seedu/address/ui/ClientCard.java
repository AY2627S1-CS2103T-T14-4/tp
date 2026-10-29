package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.ClientDisplayMode;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.client.Client;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;

/**
 * A UI component that displays information of a {@code Client}.
 */
public class ClientCard extends UiPart<Region> {

    private static final String FXML = "ClientListCard.fxml";
    private static final int APPOINTMENT_DISPLAY_LIMIT = 3;
    private static final int HISTORY_DISPLAY_LIMIT = 3;

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/pawpals-level4/issues/336">The issue on PawPals level 4</a>
     */

    public final Client client;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label email;
    @FXML
    private FlowPane tags;
    @FXML
    private VBox pets;
    @FXML
    private VBox appointments;
    @FXML
    private VBox upcomingAppointments;
    @FXML
    private VBox history;
    @FXML
    private Label emptyHistory;

    /**
     * Creates a {@code ClientCard} with the given {@code Client} and index to display.
     *
     * @throws NullPointerException if {@code client} is null.
     */
    public ClientCard(Client client, int displayedIndex) {
        this(client, displayedIndex, ClientDisplayMode.NORMAL);
    }

    /**
     * Creates a client card using the requested appointment presentation mode.
     */
    public ClientCard(Client client, int displayedIndex, ClientDisplayMode displayMode) {
        this(client, displayedIndex, displayMode, Clock.systemDefaultZone());
    }

    /** Creates a client card using {@code clock} to determine which appointments are upcoming. */
    ClientCard(Client client, int displayedIndex, ClientDisplayMode displayMode, Clock clock) {
        super(FXML);
        this.client = requireNonNull(client);
        requireNonNull(displayMode);
        requireNonNull(clock);
        LocalDateTime now = LocalDateTime.now(clock);
        id.setText(displayedIndex + ". ");
        name.setText(client.getName().fullName);
        phone.setText(client.getPhone().value);
        address.setText(client.getAddress().value);
        email.setText(client.getEmail().value);
        client.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        if (client.getPets().isEmpty()) {
            pets.getChildren().add(createEmptyLabel("No pets recorded"));
        }
        client.getPets().forEach(pet -> {
            String petText = "Pet: " + pet.getName() + " (" + pet.getSpecies() + ", " + pet.getBreed() + ")";
            VBox petDetails = new VBox();
            petDetails.setMinWidth(0);
            petDetails.getChildren().add(createWrappingLabel(petText, "cell_small_label"));
            addNoteLabels(petDetails, pet);
            pets.getChildren().add(petDetails);
        });
        addAppointments(client, displayMode, now, true, appointments);
        addAppointments(client, displayMode, now, false, upcomingAppointments);
        long historyLimit = displayMode.getType() == ClientDisplayMode.Type.SCHEDULE_CLIENT
                ? Long.MAX_VALUE : HISTORY_DISPLAY_LIMIT;
        client.getHistory().stream().limit(historyLimit).forEach(session -> {
            VBox sessionDetails = new VBox();
            sessionDetails.setMinWidth(0);
            sessionDetails.getChildren().add(createWrappingLabel(
                    formatAppointmentSummary(session.getAppointment()), "cell_small_label"));
            sessionDetails.getChildren().add(createWrappingLabel("Notes: " + session.getNotes(), "cell_small_label"));
            history.getChildren().add(sessionDetails);
        });
        if (displayMode.getType() != ClientDisplayMode.Type.SCHEDULE_CLIENT) {
            addOverflowLabel(history, client.getHistory().size() - HISTORY_DISPLAY_LIMIT, "completed session");
        }
        if (appointments.getChildren().isEmpty()) {
            appointments.getChildren().add(createEmptyLabel("No active appointments"));
        }
        if (upcomingAppointments.getChildren().isEmpty()) {
            upcomingAppointments.getChildren().add(createEmptyLabel("No upcoming appointments"));
        }
        emptyHistory.setVisible(client.getHistory().isEmpty());
        emptyHistory.setManaged(emptyHistory.isVisible());
    }

    private static boolean shouldDisplayAppointment(Appointment appointment, ClientDisplayMode displayMode) {
        return switch (displayMode.getType()) {
            case NORMAL, SCHEDULE_CLIENT -> true;
            case SCHEDULE_DATE -> appointment.getDate().equals(displayMode.getDate().orElseThrow());
        };
    }

    private static void addAppointments(Client client, ClientDisplayMode displayMode, LocalDateTime now,
            boolean active, VBox container) {
        long count = client.getAppointments().stream()
                .filter(appointment -> shouldDisplayAppointment(appointment, displayMode))
                .filter(appointment -> hasStarted(appointment, now) == active)
                .count();
        long limit = displayMode.getType() == ClientDisplayMode.Type.NORMAL
                ? APPOINTMENT_DISPLAY_LIMIT : Long.MAX_VALUE;
        client.getAppointments().stream()
                .filter(appointment -> shouldDisplayAppointment(appointment, displayMode))
                .filter(appointment -> hasStarted(appointment, now) == active)
                .sorted(Comparator.comparing((Appointment appointment) -> appointment.getDate().getValue())
                        .thenComparing(appointment -> appointment.getTime().getValue()))
                .limit(limit)
                .forEach(appointment -> container.getChildren().add(
                        createWrappingLabel(formatAppointment(appointment, displayMode, now), "cell_small_label")));
        if (displayMode.getType() == ClientDisplayMode.Type.NORMAL) {
            addOverflowLabel(container, count - APPOINTMENT_DISPLAY_LIMIT,
                    active ? "active appointment" : "appointment");
        }
    }

    private static boolean hasStarted(Appointment appointment, LocalDateTime now) {
        LocalDateTime start = LocalDateTime.of(appointment.getDate().getValue(), appointment.getTime().getValue());
        return !start.isAfter(now);
    }

    private static void addOverflowLabel(VBox container, long hiddenCount, String itemName) {
        if (hiddenCount <= 0) {
            return;
        }
        String pluralSuffix = hiddenCount == 1 ? "" : "s";
        container.getChildren().add(createWrappingLabel(
                "+" + hiddenCount + " more " + itemName + pluralSuffix, "cell_small_label"));
    }

    /**
     * Returns the booking's times, recorded pet names in order and any nonblank details.
     * Empty pet links are explicit and never inferred from the client's pets.
     */
    private static String formatAppointment(Appointment appointment, ClientDisplayMode displayMode, LocalDateTime now) {
        String details = appointment.getDetails().isBlank() ? "" : " (" + appointment.getDetails() + ")";
        String overdue = displayMode.getType() == ClientDisplayMode.Type.NORMAL && hasStarted(appointment, now)
                ? "Overdue · " : "";
        return overdue + "Appointment: " + formatAppointmentSummary(appointment) + details;
    }

    /**
     * Returns the booking's times and recorded pet names without resolving current pets.
     */
    private static String formatAppointmentSummary(Appointment appointment) {
        String endTime = appointment.getEndTime().map(time -> "–" + time).orElse("");
        String petNames = appointment.getPetNames().isEmpty() ? "No pets linked"
                : appointment.getPetNames().stream().map(Object::toString).collect(Collectors.joining(", "));
        return appointment.getDate() + " " + appointment.getTime() + endTime + " · " + petNames;
    }

    /**
     * Creates a label that wraps within its parent and grows vertically to display all its text.
     */
    private static Label createWrappingLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.getStyleClass().add(styleClass);
        return label;
    }

    private static void addNoteLabels(VBox petDetails, Pet pet) {
        if (pet.getNotes().isEmpty()) {
            petDetails.getChildren().add(createEmptyLabel("No notes recorded"));
        }
        addNoteLabel(petDetails, pet, NoteType.PREF, "Pref: ", false);
        addNoteLabel(petDetails, pet, NoteType.SENS, "⚠ Sens: ", true);
        addNoteLabel(petDetails, pet, NoteType.TEMP, "⚠ Temp: ", true);
    }

    private static void addNoteLabel(VBox petDetails, Pet pet, NoteType type, String prefix,
                                     boolean safetyNote) {
        pet.getNote(type).ifPresent(note -> {
            Label noteLabel = createWrappingLabel(prefix + note, "cell_note_label");
            if (safetyNote) {
                noteLabel.getStyleClass().add("cell_safety_note_label");
            }
            petDetails.getChildren().add(noteLabel);
        });
    }

    /**
     * Creates a subdued empty-state label that can wrap without truncating its text vertically.
     */
    private static Label createEmptyLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.getStyleClass().add("empty-state");
        return label;
    }
}
