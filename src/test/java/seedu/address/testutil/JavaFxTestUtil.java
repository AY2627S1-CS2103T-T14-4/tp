package seedu.address.testutil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/**
 * Runs UI assertions on a shared JavaFX toolkit that remains available across test classes.
 */
public final class JavaFxTestUtil {
    private JavaFxTestUtil() {
    }

    /**
     * Starts JavaFX if needed and prevents closing a test window from stopping the toolkit.
     */
    public static void startToolkit() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        try {
            Platform.startup(ready::countDown);
        } catch (IllegalStateException e) {
            // Another UI test has already started the toolkit.
            ready.countDown();
        }
        if (!ready.await(10, TimeUnit.SECONDS)) {
            throw new AssertionError("JavaFX toolkit did not start");
        }
        runAndWait(() -> Platform.setImplicitExit(false));
    }

    /**
     * Runs an action on the JavaFX application thread and propagates failures to the test runner.
     */
    public static void runAndWait(Runnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
