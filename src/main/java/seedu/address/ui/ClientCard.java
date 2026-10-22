package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.client.Client;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;

/**
 * A UI component that displays information of a {@code Client}.
 */
public class ClientCard extends UiPart<Region> {

    private static final String FXML = "ClientListCard.fxml";
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
    private VBox history;
    @FXML
    private Label emptyHistory;

    /**
     * Creates a {@code ClientCard} with the given {@code Client} and index to display.
     *
     * @throws NullPointerException if {@code client} is null.
     */
    public ClientCard(Client client, int displayedIndex) {
        super(FXML);
        this.client = requireNonNull(client);
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
        client.getAppointments().forEach(appointment -> appointments.getChildren().add(
                createWrappingLabel(formatAppointment(appointment), "cell_small_label")));
        client.getHistory().stream().limit(HISTORY_DISPLAY_LIMIT).forEach(session -> {
            VBox sessionDetails = new VBox();
            sessionDetails.setMinWidth(0);
            sessionDetails.getChildren().add(createWrappingLabel(
                    formatAppointmentSummary(session.getAppointment()), "cell_small_label"));
            sessionDetails.getChildren().add(createWrappingLabel("Notes: " + session.getNotes(), "cell_small_label"));
            history.getChildren().add(sessionDetails);
        });
        if (client.getAppointments().isEmpty()) {
            appointments.getChildren().add(createEmptyLabel("No appointments scheduled"));
        }
        emptyHistory.setVisible(client.getHistory().isEmpty());
        emptyHistory.setManaged(emptyHistory.isVisible());
    }

    /**
     * Returns the booking's times, recorded pet names in order and any nonblank details.
     * Empty pet links are explicit and never inferred from the client's pets.
     */
    private static String formatAppointment(Appointment appointment) {
        String details = appointment.getDetails().isBlank() ? "" : " (" + appointment.getDetails() + ")";
        return "Appointment: " + formatAppointmentSummary(appointment) + details;
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
