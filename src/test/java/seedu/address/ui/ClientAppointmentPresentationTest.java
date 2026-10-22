package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import seedu.address.model.ModelManager;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.JavaFxTestUtil;

public class ClientAppointmentPresentationTest {
    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
    private static final Pet LUNA = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));
    private static final AppointmentDate DATE = new AppointmentDate("12-12-2026");
    private static final AppointmentTime START = new AppointmentTime("14:00");
    private static final String START_TEXT = "Appointment: 12-12-2026 14:00";

    @BeforeAll
    public static void setUpJavaFxToolkit() throws Exception {
        JavaFxTestUtil.startToolkit();
    }

    @Test
    public void constructor_petAndEndTimePartitions_displaysRecordedNamesInOrder() throws Exception {
        JavaFxTestUtil.runAndWait(() -> {
            List<List<PetName>> links = List.of(List.of(), List.of(MILO.getName()),
                    List.of(LUNA.getName(), MILO.getName()));
            List<String> expectedPets = List.of("No pets linked", "Milo", "Luna, Milo");
            for (int i = 0; i < links.size(); i++) {
                for (boolean hasEndTime : List.of(false, true)) {
                    Appointment appointment = new Appointment(DATE, START, "", new LinkedHashSet<>(links.get(i)));
                    if (hasEndTime) {
                        appointment = appointment.withEndTime(new AppointmentTime("15:30"));
                    }
                    Client client = new ClientBuilder().withPets(MILO, LUNA)
                            .withAppointments(appointment).build();
                    String expected = START_TEXT + (hasEndTime ? "–15:30" : "") + " · " + expectedPets.get(i);
                    assertEquals(List.of(expected), appointmentTexts(new ClientCard(client, 1).getRoot()));
                }
            }
        });
    }

    @Test
    public void constructor_detailsPartitions_preservesOnlyNonblankDetails() throws Exception {
        JavaFxTestUtil.runAndWait(() -> {
            for (String details : List.of("", " \t", "  Full groom (gentle shampoo)  ")) {
                Appointment appointment = new Appointment(DATE, START, details);
                Client client = new ClientBuilder().withAppointments(appointment).build();
                String expected = START_TEXT + " · No pets linked"
                        + (details.isBlank() ? "" : " (  Full groom (gentle shampoo)  )");
                assertEquals(List.of(expected), appointmentTexts(new ClientCard(client, 1).getRoot()));
            }
        });
    }

    @Test
    public void constructor_multipleAppointments_preservesOrderAndUnresolvedRecordedNames() throws Exception {
        JavaFxTestUtil.runAndWait(() -> {
            Appointment first = new Appointment(DATE, START, "Legacy booking", Set.of(new PetName("Missing")));
            Appointment second = new Appointment(DATE, new AppointmentTime("09:00"));
            Client client = new ClientBuilder().withPets(MILO).withAppointments(first, second).build();
            assertEquals(List.of(START_TEXT + " · Missing (Legacy booking)",
                    "Appointment: 12-12-2026 09:00 · No pets linked"),
                    appointmentTexts(new ClientCard(client, 1).getRoot()));
            assertTrue(appointmentTexts(new ClientCard(new ClientBuilder().build(), 1).getRoot()).isEmpty());
        });
    }

    @Test
    public void layout_longAppointmentAndPetName_wrapsAndGrowsAtNarrowWidths() throws Exception {
        JavaFxTestUtil.runAndWait(() -> {
            Pet longNamedPet = new Pet(new PetName("A".repeat(40)), new Species("Dog"), new Breed("Poodle"));
            String details = "Full groom with gentle shampoo and careful trimming. ".repeat(5);
            Appointment appointment = new Appointment(DATE, START, details,
                    new LinkedHashSet<>(List.of(longNamedPet.getName(), MILO.getName())))
                    .withEndTime(new AppointmentTime("15:30"));
            Client client = new ClientBuilder().withPets(longNamedPet, MILO).withAppointments(appointment).build();
            Region card = new ClientCard(client, 1).getRoot();
            StackPane root = sceneRoot(card);
            Label row = appointmentLabels(card).getFirst();
            VBox petDetails = (VBox) ((VBox) card.lookup("#pets")).getChildren().getFirst();
            Label petLabel = (Label) petDetails.getChildren().getFirst();
            String expected = START_TEXT + "–15:30 · " + "A".repeat(40) + ", Milo (" + details + ")";

            layout(root, 1000, 1200);
            double wideRowHeight = row.getHeight();
            double widePetHeight = petLabel.getHeight();
            layout(root, 300, 1200);
            assertEquals(expected, row.getText());
            assertTrue(row.isWrapText());
            assertTrue(petLabel.isWrapText());
            assertTrue(row.getHeight() > wideRowHeight);
            assertTrue(petLabel.getHeight() > widePetHeight);
            assertFits(row, card);
            assertFits(petLabel, card);

            // A width just below a one-line boundary must grow the label, rather than clip it.
            double oneLineWidth = petLabel.prefWidth(-1);
            layout(root, oneLineWidth + 30, 1200);
            double oneLineHeight = petLabel.getHeight();
            layout(root, oneLineWidth + 10, 1200);
            assertTrue(petLabel.getHeight() > oneLineHeight);
            assertFits(petLabel, card);
        });
    }

    @Test
    public void modelUpdates_existingList_refreshesAppointmentRows() throws Exception {
        JavaFxTestUtil.runAndWait(() -> {
            ModelManager model = new ModelManager();
            Client initial = new ClientBuilder().withPets(MILO).build();
            model.addClient(initial);
            ClientListPanel panel = new ClientListPanel(model.getFilteredClientList());
            StackPane root = sceneRoot(panel.getRoot());
            assertDisplayedAppointments(root, List.of());

            Appointment booking = new Appointment(DATE, START, "Gentle shampoo", Set.of(MILO.getName()));
            Client added = initial.copyWithAppointment(booking);
            model.setClient(initial, added);
            assertDisplayedAppointments(root, List.of(START_TEXT + " · Milo (Gentle shampoo)"));

            Appointment editedBooking = booking.withEndTime(new AppointmentTime("15:30"));
            Client edited = added.withAppointmentReplaced(DATE, START, editedBooking);
            model.setClient(added, edited);
            assertDisplayedAppointments(root, List.of(START_TEXT + "–15:30 · Milo (Gentle shampoo)"));

            Client morePets = edited.withPet(LUNA);
            model.setClient(edited, morePets);
            assertDisplayedAppointments(root, List.of(START_TEXT + "–15:30 · Milo (Gentle shampoo)"));

            Pet renamedPet = new Pet(new PetName("Milo Junior"), MILO.getSpecies(), MILO.getBreed());
            Client renamed = morePets.withPetReplaced(MILO.getName(), renamedPet);
            model.setClient(morePets, renamed);
            assertDisplayedAppointments(root, List.of(START_TEXT + "–15:30 · Milo Junior (Gentle shampoo)"));

            Client unlinked = renamed.withoutPet(renamedPet.getName());
            model.setClient(renamed, unlinked);
            assertDisplayedAppointments(root, List.of(START_TEXT + "–15:30 · No pets linked (Gentle shampoo)"));

            Client cancelled = unlinked.withoutAppointment(DATE, START);
            model.setClient(unlinked, cancelled);
            assertDisplayedAppointments(root, List.of());
        });
    }

    @Test
    public void layout_longRowsInScrollableList_wrapsWithinViewportAfterResizeAndScroll() throws Exception {
        JavaFxTestUtil.runAndWait(() -> {
            ModelManager model = new ModelManager();
            Pet longNamedPet = new Pet(new PetName("A".repeat(40)), new Species("Dog"), new Breed("Poodle"));
            for (int i = 0; i < 15; i++) {
                Appointment appointment = new Appointment(DATE, START, "Gentle shampoo and careful trim. ".repeat(4),
                        Set.of(longNamedPet.getName()));
                model.addClient(new ClientBuilder().withName("Client " + (char) ('A' + i))
                        .withPhone("900000" + (10 + i))
                        .withPets(longNamedPet).withAppointments(appointment).build());
            }
            ClientListPanel panel = new ClientListPanel(model.getFilteredClientList());
            StackPane root = sceneRoot(panel.getRoot());
            layout(root, 700, 600);
            ListView<?> list = (ListView<?>) root.lookup("#clientListView");
            layout(root, 320, 600);
            list.scrollTo(14);
            layout(root, 320, 600);
            List<? extends ListCell<?>> visibleCells = list.lookupAll(".list-cell").stream()
                    .filter(node -> node instanceof ListCell<?> cell && !cell.isEmpty() && cell.isVisible())
                    .map(node -> (ListCell<?>) node).toList();
            assertTrue(visibleCells.stream().anyMatch(cell -> cell.getIndex() == 14));
            visibleCells.forEach(cell -> {
                Region card = (Region) cell.getGraphic();
                assertTrue(card.getWidth() <= list.getWidth(), "Card " + card.getWidth()
                        + ", cell " + cell.getWidth() + ", list " + list.getWidth() + ", index " + cell.getIndex());
                appointmentLabels(card).forEach(label -> assertFits(label, card));
            });
            list.lookupAll(".scroll-bar").stream().map(node -> (ScrollBar) node)
                    .filter(bar -> bar.getOrientation() == Orientation.HORIZONTAL)
                    .forEach(bar -> assertFalse(bar.isVisible()));
        });
    }

    @Test
    public void updateItem_reusedCell_releasesBindingsAndClearsOldRows() throws Exception {
        JavaFxTestUtil.runAndWait(() -> {
            ModelManager model = new ModelManager();
            Client first = new ClientBuilder().withPets(MILO)
                    .withAppointments(new Appointment(DATE, START, "", Set.of(MILO.getName()))).build();
            Client second = new ClientBuilder().withName("Another Client").build();
            ClientListPanel panel = new ClientListPanel(model.getFilteredClientList());
            ClientListPanel.ClientListViewCell cell = panel.new ClientListViewCell();
            cell.updateIndex(0);
            cell.resize(400, 500);
            cell.updateItem(first, false);
            Region oldCard = (Region) cell.getGraphic();
            assertTrue(oldCard.prefWidthProperty().isBound());
            assertEquals(List.of(START_TEXT + " · Milo"), appointmentTexts(oldCard));
            cell.updateItem(second, false);
            assertFalse(oldCard.prefWidthProperty().isBound());
            assertFalse(oldCard.maxWidthProperty().isBound());
            assertTrue(appointmentTexts(cell.getGraphic()).isEmpty());
            Region nextCard = (Region) cell.getGraphic();
            cell.updateItem(null, true);
            assertNull(cell.getGraphic());
            assertNull(cell.getText());
            assertFalse(nextCard.prefWidthProperty().isBound());
            assertFalse(nextCard.maxWidthProperty().isBound());
        });
    }

    private static StackPane sceneRoot(Region content) {
        StackPane root = new StackPane(content);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ClientCard.class.getResource("/view/DarkTheme.css").toExternalForm());
        return root;
    }

    private static void layout(StackPane root, double width, double height) {
        root.resize(width, height);
        root.applyCss();
        root.layout();
    }

    private static List<Label> appointmentLabels(Node card) {
        VBox appointments = (VBox) card.lookup("#appointments");
        return appointments.getChildren().stream().map(node -> (Label) node).toList();
    }

    private static List<String> appointmentTexts(Node card) {
        return appointmentLabels(card).stream().map(Label::getText).toList();
    }

    private static void assertFits(Label label, Region card) {
        assertTrue(label.getWidth() <= card.getWidth());
        assertTrue(label.getHeight() >= label.prefHeight(label.getWidth()) - 1);
        assertTrue(card.sceneToLocal(label.localToScene(label.getBoundsInLocal())).getMaxY()
                <= card.getHeight() + 1);
    }

    private static void assertDisplayedAppointments(StackPane root, List<String> expected) {
        layout(root, 600, 900);
        ListView<?> list = (ListView<?>) root.lookup("#clientListView");
        ListCell<?> cell = list.lookupAll(".list-cell").stream()
                .filter(node -> node instanceof ListCell<?> candidate && !candidate.isEmpty()
                        && candidate.getIndex() == 0)
                .map(node -> (ListCell<?>) node).findFirst().orElseThrow();
        assertEquals(expected, appointmentTexts(cell.getGraphic()));
        Region card = (Region) cell.getGraphic();
        assertTrue(card.getWidth() <= cell.getWidth());
        appointmentLabels(card).forEach(label -> assertFits(label, card));
    }
}
