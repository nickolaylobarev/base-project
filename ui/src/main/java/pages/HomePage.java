package pages;

import static allure.AllureUtils.step;
import static com.codeborne.selenide.Selenide.$x;
import com.codeborne.selenide.SelenideElement;

public class HomePage extends BasePage<HomePage> {
    {
        url = "";
    }

    private final SelenideElement apiDocsButton = $x("//a[text()='API Docs']");

    public void goToApiDocsPage() {
        step("Opening ApiDocsPage");
        apiDocsButton.click();
    }
}
