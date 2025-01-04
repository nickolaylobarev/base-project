package allure;

import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for reusing Allure steps with added logging
 */

@Slf4j
public class AllureUtils {
    public static void step(String name) {
        log.info(name);
        Allure.step(name);
    }

    public static void step(String name, Allure.ThrowableRunnableVoid runnable) {
        log.info(name);
        Allure.step(name, () -> {
            runnable.run();
            return null;
        });
    }
}
