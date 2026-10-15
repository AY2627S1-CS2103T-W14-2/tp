package seedu.address.testutil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/** Runs UI assertions on the JavaFX thread without timing-dependent sleeps. */
public final class FxTestUtil {
    private static boolean started;

    private FxTestUtil() {}

    /** Executes assertions on the JavaFX thread, propagating failures to the calling test. */
    public static synchronized void run(Runnable assertions) throws Exception {
        if (!started) {
            CountDownLatch ready = new CountDownLatch(1);
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                ready.countDown();
            });
            if (!ready.await(30, TimeUnit.SECONDS)) {
                throw new AssertionError("JavaFX startup timed out");
            }
            started = true;
        }
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(30, TimeUnit.SECONDS);
    }
}
