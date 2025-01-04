package pages;

import com.codeborne.selenide.Condition;
import static com.codeborne.selenide.Selenide.$x;
import com.codeborne.selenide.SelenideElement;

public class ApiDocsPage extends BasePage<ApiDocsPage> {
    {
        url = "/apidoc";
    }

    private final SelenideElement goBackToHomeButton = $x("//div[@id=\"project\"]//h2/a");
    private final SelenideElement footer = $x("//*[@id=\"generator\"]");

    public ApiDocsPage checkGoBackToHomeButton() {
        goBackToHomeButton.shouldHave(Condition.text("Click here to go back to Home"));
        return this;
    }

    public ApiDocsPage checkFooter() {
        footer.shouldBe(Condition.visible);
        return this;
    }
}
