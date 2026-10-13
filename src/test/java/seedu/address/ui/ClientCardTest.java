package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;
import seedu.address.testutil.ClientBuilder;

public class ClientCardTest {
    private static final Pet MILO = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));
    private static final Pet LUNA = new Pet(new PetName("Luna"), new Species("Cat"), new Breed("Persian"));

    @BeforeAll
    public static void setUpJavaFxToolkit() throws InterruptedException {
        CountDownLatch toolkitReady = new CountDownLatch(1);
        try {
            Platform.startup(toolkitReady::countDown);
        } catch (IllegalStateException e) {
            toolkitReady.countDown();
        }
        assertTrue(toolkitReady.await(10, TimeUnit.SECONDS));
    }

    @AfterAll
    public static void tearDownJavaFxToolkit() {
        Platform.exit();
    }

    @Test
    public void constructor_clientWithPets_displaysPetDetailsInOrder() {
        Client client = new ClientBuilder().withPets(MILO, LUNA).build();

        List<String> labels = getSmallLabelTexts(new ClientCard(client, 1).getRoot());

        String miloText = "Pet: Milo (Dog, Poodle)";
        String lunaText = "Pet: Luna (Cat, Persian)";
        assertTrue(labels.contains(miloText));
        assertTrue(labels.contains(lunaText));
        assertTrue(labels.indexOf(miloText) < labels.indexOf(lunaText));
    }

    @Test
    public void constructor_clientWithoutPets_doesNotDisplayPetDetails() {
        Client client = new ClientBuilder().build();

        List<String> labels = getSmallLabelTexts(new ClientCard(client, 1).getRoot());

        assertFalse(labels.stream().anyMatch(label -> label.startsWith("Pet: ")));
    }

    @Test
    public void constructor_differentClients_displayOnlyTheirOwnPets() {
        Client alice = new ClientBuilder().withName("Alice Test").withPets(MILO).build();
        Client bob = new ClientBuilder().withName("Bob Test").withPets(LUNA).build();

        List<String> aliceLabels = getSmallLabelTexts(new ClientCard(alice, 1).getRoot());
        List<String> bobLabels = getSmallLabelTexts(new ClientCard(bob, 2).getRoot());

        assertTrue(aliceLabels.contains("Pet: Milo (Dog, Poodle)"));
        assertFalse(aliceLabels.contains("Pet: Luna (Cat, Persian)"));
        assertTrue(bobLabels.contains("Pet: Luna (Cat, Persian)"));
        assertFalse(bobLabels.contains("Pet: Milo (Dog, Poodle)"));
    }

    private List<String> getSmallLabelTexts(Region root) {
        List<String> labels = new ArrayList<>();
        collectSmallLabelTexts(root, labels);
        return labels;
    }

    private void collectSmallLabelTexts(Node node, List<String> labels) {
        if (node instanceof Label label && label.getStyleClass().contains("cell_small_label")) {
            labels.add(label.getText());
        }
        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> collectSmallLabelTexts(child, labels));
        }
    }
}
