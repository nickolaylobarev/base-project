package tests;

import allure.annotations.JiraIssue;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import static utils.RetryUtil.withRetry;

public class ApiDocsTest extends BaseTest {

    @Test
    @JiraIssue("XXXX-3001")
    @DisplayName("Check that there is a 'Back to Home' button on the ApiDocs page")
    @Description("Open ApiDocs page and check that there is a 'Back to Home' button")
    @AllureId("30001")
    public void goBackToHomeButtonTest() {
        homePage.open()
                .goToApiDocsPage();
        apiDocsPage.checkGoBackToHomeButton();
    }

    @Test
    @JiraIssue("XXXX-3002")
    @DisplayName("Verify the screenshot of the ApiDocs page")
    @Description("Open the ApiDocs page and make sure it matches the existing screenshot")
    @AllureId("30001")
    public void apiDocsScreenshotTest(TestInfo testInfo) {
        apiDocsPage.open()
                .checkFooter();
        withRetry(() -> assertScreenshots(testInfo));
    }
}
