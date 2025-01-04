package tests.performance;

import static allure.AllureAttachmentUtil.addZipAttachment;
//import com.github.tomakehurst.wiremock.WireMockServer;
//import com.github.tomakehurst.wiremock.client.WireMock;
//import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import mock.MockServer;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static us.abstracta.jmeter.javadsl.JmeterDsl.autoStop;
import static us.abstracta.jmeter.javadsl.JmeterDsl.htmlReporter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PostProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jtlWriter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.responseAssertion;
import static us.abstracta.jmeter.javadsl.JmeterDsl.responseFileSaver;
import static us.abstracta.jmeter.javadsl.JmeterDsl.resultsTreeVisualizer;
import static us.abstracta.jmeter.javadsl.JmeterDsl.rpsThreadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import static us.abstracta.jmeter.javadsl.core.listeners.AutoStopListener.AutoStopCondition.errors;
import us.abstracta.jmeter.javadsl.core.listeners.JtlWriter;
import static us.abstracta.jmeter.javadsl.dashboard.DashboardVisualizer.dashboardVisualizer;

/**
 * A class containing various performance test scenarios using JMeter DSL.
 * <p>
 * It defines multiple tests for different load conditions, including ramping up threads,
 * handling requests per second (RPS), and generating reports in different formats (HTML, JTL, Allure).
 * <p>
 * It uses a mock server for testing purposes, simulating HTTP POST requests to the "/default" endpoint
 */

@Slf4j
public class DraftPerformanceTests {
    private static final int THREADS = 5;
    private static final int ITERATIONS = 10;

    private static final double RPS = 10.0;
    private static final int MAX_THREADS = 100;
    private static final int RAMP_DURATION = 5;
    private static final int HOLD_DURATION = 10;

    private static final String REPORT_IDENTIFIER = "report-" + LocalDateTime.now();

    private MockServer mockServer;

//    private final WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(80));

    @BeforeAll
    public void setUp() {
//        wireMockServer.stubFor(
//                WireMock.post("/foo")
//                        .willReturn(
//                                WireMock.okJson(
//                                        """
//                                                {
//                                                    "success": true
//                                                }
//                                                """
//                                )
//                        )
//        );
//
//        wireMockServer.start();
        mockServer = new MockServer();
    }

    @AfterAll
    public void tearDown() {
//        wireMockServer.stop();
        mockServer.close();
    }

    @Test
    public void durationAndAutoStopTest() throws IOException {
        testPlan(
                threadGroup(THREADS, Duration.ofSeconds(HOLD_DURATION),
                        httpSampler(mockServer.getPublicUrl() + "/default")
                        // httpSampler(wireMockServer.baseUrl() + "/foo")
                                .post("{}", ContentType.APPLICATION_JSON)
                ),
                autoStop()
                        .when(errors().total().greaterThan(0L)),
                resultsTreeVisualizer(),
                dashboardVisualizer()
        ).run();
    }

    @Test
    public void rampToTest() throws IOException {
        testPlan(
                threadGroup()
                        .rampTo(THREADS, Duration.ofSeconds(RAMP_DURATION))
                        .holdIterating(ITERATIONS)
                        .children(
                                httpSampler(mockServer.getPublicUrl() + "/default")
                                        .post("{}", ContentType.APPLICATION_JSON)
                        ),
                resultsTreeVisualizer(),
                dashboardVisualizer()
        ).run();
    }

    @Test
    public void rampAndHoldTest() throws IOException {
        testPlan(
                threadGroup()
                        .rampToAndHold(THREADS, Duration.ofSeconds(RAMP_DURATION), Duration.ofSeconds(HOLD_DURATION))
                        .children(
                                httpSampler(mockServer.getPublicUrl() + "/default")
                                        .post("{}", ContentType.APPLICATION_JSON)
                        ),
                resultsTreeVisualizer(),
                dashboardVisualizer()
        ).run();
    }

    @Test
    public void complexShapeTest() throws IOException {
        testPlan(
                threadGroup()
                        .rampToAndHold(THREADS, Duration.ofSeconds(RAMP_DURATION), Duration.ofSeconds(HOLD_DURATION))
                        .rampToAndHold(THREADS * 2, Duration.ofSeconds(RAMP_DURATION),
                                Duration.ofSeconds(HOLD_DURATION))
                        .rampTo(THREADS * 5, Duration.ofSeconds(RAMP_DURATION))
                        .rampToAndHold(THREADS * 2, Duration.ofSeconds(RAMP_DURATION),
                                Duration.ofSeconds(HOLD_DURATION))
                        .rampTo(0, Duration.ofSeconds(RAMP_DURATION))
                        .children(
                                httpSampler(mockServer.getPublicUrl() + "/default")
                                        .post("{}", ContentType.APPLICATION_JSON)
                        ),
                resultsTreeVisualizer(),
                dashboardVisualizer()
        ).run();
    }

    @Test
    public void rpsTest() throws IOException {
        testPlan(
                rpsThreadGroup()
                        .maxThreads(MAX_THREADS)
                        .rampTo(RPS, Duration.ofSeconds(RAMP_DURATION))
                        .rampTo(RPS * 5, Duration.ofSeconds(RAMP_DURATION))
                        .rampToAndHold(RPS * 10, Duration.ofSeconds(RAMP_DURATION), Duration.ofSeconds(HOLD_DURATION))
                        .children(
                                httpSampler(mockServer.getPublicUrl() + "/default")
                                        .post("{}", ContentType.APPLICATION_JSON)
                        ),
                resultsTreeVisualizer(),
                dashboardVisualizer()
        ).run();
    }

    @Test
    public void assertionsTest() throws IOException {
        testPlan(
                threadGroup(THREADS, Duration.ofSeconds(HOLD_DURATION),
                        httpSampler(mockServer.getPublicUrl() + "/default")
                                .post("{}", ContentType.APPLICATION_JSON)
                                .children(
                                        responseAssertion().containsSubstrings("true"),
                                        jsr223PostProcessor(s -> {
                                            s.prev.setSuccessful("200".equals(s.prev.getResponseCode()) &&
                                                    s.prev.getResponseDataAsString().contains("true"));
                                        })
                                )
                ),
                resultsTreeVisualizer(),
                dashboardVisualizer()
        ).run();
    }

    @Test
    public void jtlsReportsTest() throws IOException {
        testPlan(
                threadGroup(THREADS, ITERATIONS,
                        httpSampler(mockServer.getPublicUrl() + "/default")
                                .post("{}", ContentType.APPLICATION_JSON)
                ),
                responseFileSaver(Instant.now().toString().replace(":", "-") + "-response"),
                jtlWriter(".jmeter-reports/jtls")
                        .withAllFields(true),
                jtlWriter(".jmeter-reports/jtls/success")
                        .logOnly(JtlWriter.SampleStatus.SUCCESS),
                jtlWriter(".jmeter-reports/jtls/error")
                        .logOnly(JtlWriter.SampleStatus.ERROR)
        ).run();
    }

    @Test
    public void htmlReportsTest() throws IOException {
        testPlan(
                threadGroup(THREADS, ITERATIONS,
                        httpSampler(mockServer.getPublicUrl() + "/default")
                                .post("{}", ContentType.APPLICATION_JSON)
                ),
                htmlReporter(".jmeter-reports", REPORT_IDENTIFIER)
        ).run();
    }

    @Test
    public void reportsInAllureTest() throws IOException {
        testPlan(
                threadGroup(THREADS, ITERATIONS,
                        httpSampler(mockServer.getPublicUrl() + "/default")
                                .post("{}", ContentType.APPLICATION_JSON)
                ),
                htmlReporter(".jmeter-reports", REPORT_IDENTIFIER)
        ).run();

        addZipAttachment(REPORT_IDENTIFIER);
    }

    @Test
    public void jsrTeamCityLogsTest() throws IOException {
        TestPlanStats stats =
                testPlan(
                        threadGroup(THREADS, ITERATIONS,
                                httpSampler(mockServer.getPublicUrl() + "/default")
                                        .post("{}", ContentType.APPLICATION_JSON)
                                        .children(
                                                jsr223PostProcessor(c ->
                                                        log.info("Get metrics: {}", c.prevMetrics())
                                                )
                                        )
                        )
                ).run();

        System.out.println(
                "teamcity[buildStatisticValue key='percentile99' value='" +
                        stats.overall().sampleTimePercentile99().toMillis() + "']"
        );
        System.out.println("teamcity[buildStatisticValue key='errors' value='" + stats.overall().errorsCount() + "']");
    }
}
