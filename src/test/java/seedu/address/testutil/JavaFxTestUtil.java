package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/**
 * Shares one JavaFX toolkit across UI test classes in a test worker and runs assertions on its application thread.
 * Individual classes must not call Platform.exit(), because JavaFX cannot be restarted in the same worker.
 */
public final class JavaFxTestUtil {
    private static final long TIMEOUT_SECONDS = 10;

    private JavaFxTestUtil() {
    }

    /**
     * Starts the toolkit or waits for the existing toolkit to process the initialization callback.
     */
    public static void initializeToolkit() throws InterruptedException {
        CountDownLatch toolkitReady = new CountDownLatch(1);
        Runnable initialize = () -> {
            Platform.setImplicitExit(false);
            toolkitReady.countDown();
        };
        try {
            Platform.startup(initialize);
        } catch (IllegalStateException e) {
            Platform.runLater(initialize);
        }
        assertTrue(toolkitReady.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "JavaFX toolkit did not initialize");
    }

    /**
     * Runs a task on the initialized JavaFX application thread, propagating failures to the calling test.
     */
    public static <T> T runOnFxThread(Callable<T> task) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return task.call();
        }
        FutureTask<T> future = new FutureTask<>(task);
        Platform.runLater(future);
        return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
