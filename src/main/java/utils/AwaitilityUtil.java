package utils;

import java.time.Duration;
import java.util.function.Supplier;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;

/**
 * Class provides methods to handle asynchronous waiting with configurable timeouts and polling intervals.
 * It supports waiting for conditions to be met and asserting values retrieved from a supplier,
 * with default and custom timeout/poll interval options
 */

public class AwaitilityUtil {
    public static ConditionFactory await(Duration timeout, Duration pollInterval) {
        return Awaitility.await()
                .pollInterval(pollInterval)
                .timeout(timeout)
                .ignoreExceptions();
    }

    public static ConditionFactory await() {
        return await(Duration.ofSeconds(30), Duration.ofSeconds(3));
    }

    @SuppressWarnings("unchecked")
    public static <T> T waitUntilAsserted(Duration timeout, Duration pollInterval, Supplier<T> supplier) {
        final Object[] result = new Object[1];

        Awaitility.await()
                .pollInterval(pollInterval)
                .timeout(timeout)
                .untilAsserted(() -> result[0] = supplier.get());

        return (T) result[0];
    }

    public static <T> T waitUntilAsserted(Supplier<T> supplier) {
        return waitUntilAsserted(Duration.ofSeconds(30), Duration.ofSeconds(3), supplier);
    }
}
