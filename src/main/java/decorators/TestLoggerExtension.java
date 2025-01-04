package decorators;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


/**
 * A custom JUnit extension that logs the start and completion of each test execution.
 * Implements BeforeTestExecutionCallback and AfterTestExecutionCallback to handle test lifecycle events.
 * Logs the test name, including method name, parameters, and index (if available)
 */

@Slf4j
public class TestLoggerExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        String testName = buildTestName(context);
        log.info("RUNNING TEST: {}", testName);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        String testName = buildTestName(context);
        log.info("TEST COMPLETED: {}", testName);
    }

    private String buildTestName(ExtensionContext context) {
        String methodName = context.getTestMethod().map(Method::getName).orElse("UnknownTest");
        String displayName = context.getDisplayName();
        String index = displayName.startsWith("[") ? extractIndex(displayName) : "";
        String parameters = extractParameters(displayName);

        return index.isEmpty() ?
                String.format("%s %s", methodName, parameters) :
                String.format("%s [%s] %s", methodName, index, parameters);
    }

    private String extractIndex(String displayName) {
        int closingBracketIndex = displayName.indexOf(']');
        return (closingBracketIndex > 0) ? displayName.substring(1, closingBracketIndex) : "";
    }

    private String extractParameters(String displayName) {
        if (displayName.contains("[") && displayName.contains("]")) {
            String parameters = displayName.substring(displayName.indexOf(']') + 1).trim();
            return parameters.isEmpty() ? "" : parameters;
        }
        return "";
    }
}
