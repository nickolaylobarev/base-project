package tests.performance;

import static allure.AllureAttachmentUtil.addZipAttachment;
import static allure.AllureUtils.step;
import allure.annotations.JiraIssue;
import builders.BookingBuilder;
import static builders.BookingBuilder.createRandomBooking;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import models.booking.Booking;
import models.booking.BookingSuccessResponse;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static properties.PublicProperties.BOOKER_URL;
import tests.BaseTest;
import static tests.integration.BookingIntegrationTests.createBooking;
import static tests.integration.BookingIntegrationTests.getBooking;
import static tests.integration.BookingIntegrationTests.removeBooking;
import static us.abstracta.jmeter.javadsl.JmeterDsl.htmlReporter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsonAssertion;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsonExtractor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PostProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223Sampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.resultsTreeVisualizer;
import static us.abstracta.jmeter.javadsl.JmeterDsl.rpsThreadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import static us.abstracta.jmeter.javadsl.dashboard.DashboardVisualizer.dashboardVisualizer;
import static utils.JsonMessageTestUtils.toJson;

public class PerformanceTests extends BaseTest {

    private static final int THREADS = 5;
    private static final int ITERATIONS = 10;

    // If the system's average response time is known, calculate MAX_THREADS with a small safety margin
    private static final double RPS = 10.0;
    private static final int MAX_THREADS = 100; // MAX_THREADS = RPS * avgResponseTime
    private static final int RAMP_DURATION = 5;
    private static final int HOLD_DURATION = 10;

    private static final String REPORT_IDENTIFIER = "report-" + LocalDateTime.now();
    private static final String AUTH_HEADER = "Basic " + getToken(BOOKER_URL);

    @BeforeAll
    public static void setSpecification() {
        installSpecification(requestSpec(BOOKER_URL), responseSpecOK200Or201());
    }

    @AfterAll
    public static void removeSpecification() {
        removeSetSpecification();
    }

    @Test
    @JiraIssue("XXXX-4001")
    @DisplayName("Booking stress test")
    @Description("Send create, retrieve, and delete Booking requests, and validate performance statistics")
    @AllureId("40001")
    public void bookingStressTest() throws IOException {
        step("Send Booking messages in " + THREADS + " threads with " + ITERATIONS + " iterations");
        TestPlanStats stats = testPlan(
                threadGroup(THREADS, ITERATIONS,

                        httpSampler(BOOKER_URL + "/booking")
                                .method(HTTPConstants.POST)
                                .header("Accept", "application/json")
                                .header("Authorization", AUTH_HEADER)
                                .contentType(ContentType.APPLICATION_JSON)
                                .post(s -> toJson(createRandomBooking()), ContentType.APPLICATION_JSON)
                                .children(jsonExtractor("bookingidVariable", "bookingid"))
                                .children(jsonAssertion("booking")),

                        httpSampler(BOOKER_URL + "/booking/${bookingidVariable}")
                                .method(HTTPConstants.GET)
                                .header("Accept", "application/json")
                                .header("Authorization", AUTH_HEADER)
                                .contentType(ContentType.APPLICATION_JSON)
                                .children(jsonAssertion("firstname")),

                        httpSampler(BOOKER_URL + "/booking/${bookingidVariable}")
                                .method(HTTPConstants.DELETE)
                                .header("Accept", "application/json")
                                .header("Authorization", AUTH_HEADER)
                                .contentType(ContentType.APPLICATION_JSON)
                                .children(
                                        jsr223PostProcessor(s ->
                                                s.prev.setSuccessful(
                                                        s.prev.getResponseCode().equals("201")
                                                )
                                        )
                                )

                ), htmlReporter(".jmeter-reports", REPORT_IDENTIFIER),
                resultsTreeVisualizer(),
                dashboardVisualizer()
        ).run();

        step("Check performance statistic");
        assertEquals(0, stats.overall().errorsCount());
        assertTrue(stats.overall().sampleTimePercentile99().compareTo(Duration.ofSeconds(5)) < 0);
        addZipAttachment(REPORT_IDENTIFIER);
    }

    @Test
    @JiraIssue("XXXX-4001")
    @DisplayName("Booking load test with http Sampler")
    @Description("Send create, retrieve, and delete Booking requests, and validate performance statistics")
    @AllureId("40002")
    public void bookingLoadTest() throws IOException {
        step("Send Booking messages in up to " + MAX_THREADS + " threads at " + RPS + " rps, with a " +
                RAMP_DURATION + "-seconds ramp-up and " + HOLD_DURATION + "-seconds hold");
        TestPlanStats stats = testPlan(
                rpsThreadGroup()
                        .maxThreads(MAX_THREADS)
                        .rampToAndHold(RPS, Duration.ofSeconds(RAMP_DURATION), Duration.ofSeconds(HOLD_DURATION))
                        .children(

                                httpSampler(BOOKER_URL + "/booking")
                                        .method(HTTPConstants.POST)
                                        .header("Accept", "application/json")
                                        .header("Authorization", AUTH_HEADER)
                                        .contentType(ContentType.APPLICATION_JSON)
                                        .post(s -> toJson(createRandomBooking()), ContentType.APPLICATION_JSON)
                                        .children(jsonExtractor("bookingidVariable", "bookingid"))
                                        .children(jsonAssertion("booking")),

                                httpSampler(BOOKER_URL + "/booking/${bookingidVariable}")
                                        .method(HTTPConstants.GET)
                                        .header("Accept", "application/json")
                                        .header("Authorization", AUTH_HEADER)
                                        .contentType(ContentType.APPLICATION_JSON)
                                        .children(jsonAssertion("firstname")),

                                httpSampler(BOOKER_URL + "/booking/${bookingidVariable}")
                                        .method(HTTPConstants.DELETE)
                                        .header("Accept", "application/json")
                                        .header("Authorization", AUTH_HEADER)
                                        .contentType(ContentType.APPLICATION_JSON)
                                        .children(
                                                jsr223PostProcessor(s ->
                                                        s.prev.setSuccessful(
                                                                s.prev.getResponseCode().equals("201")
                                                        )
                                                )
                                        )

                        ), htmlReporter(".jmeter-reports", REPORT_IDENTIFIER),
                resultsTreeVisualizer(),
                dashboardVisualizer()
        ).run();

        step("Check performance statistic");
        assertEquals(0, stats.overall().errorsCount());
        assertTrue(stats.overall().sampleTimePercentile99().compareTo(Duration.ofSeconds(5)) < 0);
    }

    @Test
    @JiraIssue("XXXX-4001")
    @DisplayName("Booking load test with jsr Sampler")
    @Description("Send create, retrieve, and delete Booking requests, and validate performance statistics")
    @AllureId("40003")
    public void bookingLoadJsrTest() throws IOException {
        step("Send Booking messages in up to " + MAX_THREADS + " threads at " + RPS + " rps, with a " +
                RAMP_DURATION + "-seconds ramp-up and " + HOLD_DURATION + "-seconds hold");
        TestPlanStats stats = testPlan(
                rpsThreadGroup()
                        .maxThreads(MAX_THREADS)
                        .rampToAndHold(RPS, Duration.ofSeconds(RAMP_DURATION), Duration.ofSeconds(HOLD_DURATION))
                        .children(

                                jsr223Sampler(sampler -> {

                                    step("Prepare random Booking message");
                                    Booking booking = BookingBuilder.createRandomBooking();

                                    step("Send creating Booking message");
                                    BookingSuccessResponse bookingSuccessResponse = createBooking(booking);

                                    step("Send deleting Booking message");
                                    removeBooking(bookingSuccessResponse.getBookingid().getBookingid());

                                    step("Send getting Booking message to make sure it has been removed");
                                    AssertionError error = assertThrows(AssertionError.class, () ->
                                            getBooking(bookingSuccessResponse.getBookingid().getBookingid()));

                                    boolean isSuccessful = error.getMessage()
                                            .contains("Expected status code (<200> or <201>) but was <404>");
                                    SampleResult result = sampler.sampleResult;
                                    result.setSuccessful(isSuccessful);

                                })

                        ), htmlReporter(".jmeter-reports", REPORT_IDENTIFIER),
                resultsTreeVisualizer(),
                dashboardVisualizer()
        ).run();

        step("Check performance statistic");
        assertEquals(0, stats.overall().errorsCount());
        assertTrue(stats.overall().sampleTimePercentile99().compareTo(Duration.ofSeconds(5)) < 0);
    }
}
