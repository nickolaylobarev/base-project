package pages;

import static allure.AllureUtils.step;
import com.codeborne.selenide.Selenide;

/**
 * Class provides common functionality for all pages and supports method chaining using the Fluent Interface pattern.
 * <p>
 * Generics are used to ensure that methods return the specific subclass type, which allows for fluent method chaining.
 *
 * @param <T> The subclass type extending BasePage (e.g., HomePage, ApiDocsPage)
 */

public class BasePage<T extends BasePage<T>> {
    protected String url;

    @SuppressWarnings("unchecked") // Type casting is required to support the Fluent Interface pattern
    public T open() {
        step("Opening " + getClass().getSimpleName(), () -> Selenide.open(url));
        return (T) this;
    }
}
