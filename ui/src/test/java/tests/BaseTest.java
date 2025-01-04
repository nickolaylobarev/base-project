package tests;

import static allure.AllureUtils.step;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.github.romankh3.image.comparison.ImageComparison;
import static com.github.romankh3.image.comparison.ImageComparisonUtil.readImageFromResources;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import decorators.TestLoggerExtension;
import io.qameta.allure.Attachment;
import io.qameta.allure.selenide.AllureSelenide;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.OutputType;
import pages.ApiDocsPage;
import pages.HomePage;
import static properties.PublicProperties.BOOKER_URL;

/**
 * Screenshot filenames are based on the test method name.
 * <p>
 * If the `UPDATE_SCREENSHOTS` flag is set to true, the actual screenshot is saved as the expected screenshot,
 * overwriting the previous one in the specified folder.
 * If the flag is false, the actual screenshot is compared to the expected screenshot using an image comparison tool.
 * <p>
 * If there is a mismatch, the actual, expected screenshots, and a diff image are attached to the Allure report
 */

@ExtendWith(TestLoggerExtension.class)
public class BaseTest {
    private static final boolean UPDATE_SCREENSHOTS = false;
    private static final String SCREENSHOTS_SAVE_FOLDER = "src/test/resources/expectedScreenshots/";

    protected HomePage homePage = new HomePage();
    protected ApiDocsPage apiDocsPage = new ApiDocsPage();

    @BeforeAll
    public static void setUp() {
        step("Setting up configuration");
        Configuration.browser = System.getProperty("browser");
        Configuration.baseUrl = BOOKER_URL;
        Configuration.browserSize = "1920x1080";
        Configuration.headless = true; // set to "false" to open browser during local testing
        Configuration.timeout = 10000;
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide());
    }

    @AfterEach
    public void clearCookiesAndLocalStorage() {
        Selenide.clearBrowserCookies();
        Selenide.clearBrowserLocalStorage();
    }

    @AfterAll
    public static void tearDown() {
        step("Closing WebDriver", Selenide::closeWebDriver);
    }

    public void assertScreenshots(TestInfo testInfo) {
        String expectedFileName = testInfo.getTestMethod().get().getName() + ".png";
        File actualScreenshot = Selenide.screenshot(OutputType.FILE);
        File expectedScreenshot = new File(SCREENSHOTS_SAVE_FOLDER, expectedFileName);

        if (UPDATE_SCREENSHOTS) {
            try {
                Files.move(actualScreenshot.toPath(), Path.of(SCREENSHOTS_SAVE_FOLDER, expectedFileName),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        BufferedImage expectedImage = readImageFromResources(expectedScreenshot.toPath().toString());
        BufferedImage actualImage = readImageFromResources(actualScreenshot.toPath().toString());

        Path resultDestination = Path.of(".diff", "diff_" + expectedFileName);

        ImageComparisonResult imageComparisonResult =
                new ImageComparison(expectedImage, actualImage, resultDestination.toFile()).compareImages();

        if (imageComparisonResult.getImageComparisonState() != ImageComparisonState.MATCH) {
            attachScreenshotToAllureReport("Actual screenshot", actualScreenshot);
            attachScreenshotToAllureReport("Expected screenshot", expectedScreenshot);
            attachScreenshotToAllureReport("Difference", resultDestination.toFile());
        }
        assertEquals(ImageComparisonState.MATCH, imageComparisonResult.getImageComparisonState());
    }

    private void attachScreenshotToAllureReport(String name, File file) {
        try {
            byte[] image = Files.readAllBytes(file.toPath());
            saveScreenshot(name, image);
        } catch (IOException e) {
            throw new RuntimeException("Error while attaching screenshot to Allure report", e);
        }
    }

    @Attachment(value = "{name}", type = "image/png")
    private static byte[] saveScreenshot(String name, byte[] screenshot) {
        return screenshot;
    }
}
