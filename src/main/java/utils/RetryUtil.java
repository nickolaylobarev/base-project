package utils;

import lombok.extern.slf4j.Slf4j;

/**
 * Class provides a mechanism to retry a given task up to a maximum number of attempts in case of a specific error.
 * It handles retries with a delay between attempts and throws an error if all attempts fail
 */

@Slf4j
public class RetryUtil {
    private static final int MAX_ATTEMPT = 5;

    public static void withRetry(Runnable func) {
        for (int attempt = 1; attempt <= MAX_ATTEMPT; attempt++) {
            try {
                func.run();
                return;
            } catch (AssertionError e) {
                if (attempt == MAX_ATTEMPT) {
                    log.warn("All retry attempts are spent");
                    throw e;
                }
                if (e.getMessage().equals("expected: <MATCH> but was: <MISMATCH>")) {
                    log.info("Retrying after AssertionError. Attempt №{}", attempt);
                    sleep(3000);
                } else {
                    throw e;
                }
            }
        }
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread was interrupted during sleep", e);
        }
    }
}
