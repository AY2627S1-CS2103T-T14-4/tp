package seedu.address.ui;

import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.client.Client;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;

/**
 * A UI component that displays information of a {@code Client}.
 */
public class ClientCard extends UiPart<Region> {

    private static final String FXML = "ClientListCard.fxml";

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
    private Label emptyHistory;

    /**
     * Creates a {@code ClientCard} with the given {@code Client} and index to display.
     */
    public ClientCard(Client client, int displayedIndex) {
        super(FXML);
        this.client = client;
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
            Label label = new Label(petText);
            label.getStyleClass().add("cell_small_label");
            petDetails.getChildren().add(label);
            addNoteLabels(petDetails, pet);
            pets.getChildren().add(petDetails);
        });
        client.getAppointments().forEach(appointment -> {
            String appointmentText = "Appointment: " + appointment.getDate() + " " + appointment.getTime();
            if (!appointment.getDetails().isBlank()) {
                appointmentText += " (" + appointment.getDetails() + ")";
            }
            Label label = new Label(appointmentText);
            label.getStyleClass().add("cell_small_label");
            appointments.getChildren().add(label);
        });
        if (client.getAppointments().isEmpty()) {
            appointments.getChildren().add(createEmptyLabel("No appointments scheduled"));
        }
        emptyHistory.setVisible(client.getHistory().isEmpty());
        emptyHistory.setManaged(emptyHistory.isVisible());
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
            Label noteLabel = new Label(prefix + note);
            noteLabel.setWrapText(true);
            noteLabel.setMaxWidth(Double.MAX_VALUE);
            noteLabel.getStyleClass().add("cell_note_label");
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
