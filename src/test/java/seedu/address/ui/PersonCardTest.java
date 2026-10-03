package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    @BeforeAll
    public static void startJavaFx() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            latch.countDown();
        }

        latch.await();
    }

    @AfterAll
    public static void stopJavaFx() {
        Platform.exit();
    }

    @Test
    public void constructor_personWithRemark_displaysRemark() throws Exception {
        Person person = new PersonBuilder()
                .withRemark("Likes baseball")
                .build();

        AtomicReference<String> displayedRemark = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                PersonCard personCard = new PersonCard(person, 1);

                Label remarkLabel =
                        (Label) personCard.getRoot().lookup("#remark");

                displayedRemark.set(remarkLabel.getText());
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        latch.await();

        if (error.get() != null) {
            throw new AssertionError(error.get());
        }

        assertEquals("Likes baseball", displayedRemark.get());
    }
}
