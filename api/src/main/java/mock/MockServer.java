package mock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.admin.model.GetServeEventsResult;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import io.restassured.response.Response;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static properties.PrivateProperties.START_WIREMOCK_IN_DOCKER;
import utils.AwaitilityUtil;
import static utils.JsonMessageTestUtils.objectMapper;

/**
 * A class for managing a WireMock server with SSH tunneling support.
 * It starts and stops WireMock (either locally or in Docker), creates and manages stubs,
 * and provides an accessible public URL for the mock server through an SSH tunnel
 */

@Slf4j
public class MockServer implements Closeable {
    private static final int MAX_ATTEMPTS_TO_START_SSH_TUNNEL = 5;
    private int wireMockPort;
    private String dockerContainerId;
    private WireMockServer wireMockServer;
    private SshClient sshClient;
    private WireMockClient wireMockClient;

    public MockServer() {
        startWireMock();
        tryToStartSshTunnel();
        prepareStubs();
    }

    public String getPublicUrl() {
        return sshClient.getSshServerUrl();
    }

    public void manageMockEndpoint(String stub) {
        WireMockClient wireMockClient = new WireMockClient("http://localhost:" + wireMockPort);
        wireMockClient.sendCreateStubRequest(stub);
    }

    public List<ServeEvent> getAllEvents() {
        String response = wireMockClient.sendGetRequestsRequest().getBody().asString();
        try {
            return objectMapper.readValue(response, GetServeEventsResult.class).getServeEvents();
        } catch (IOException e) {
            return null;
        }
    }

    public void cleanAllEvents() {
        wireMockClient.sendCleanRequestsRequest();
    }

    public List<String> getEndpointEvents(String endpoint) {
        return AwaitilityUtil.waitUntilAsserted(() -> {
            List<ServeEvent> events = getAllEvents();
            List<String> requests = events.stream()
                    .filter(event -> event.getRequest().getUrl().contains(endpoint))
                    .map(event -> event.getRequest().getBodyAsString())
                    .collect(Collectors.toList());

            assertFalse(requests.isEmpty());
            return requests;
        });
    }

    @Override
    public void close() {
        log.info("Stopping SSH tunnel");
        sshClient.close();
        stopWireMock();
    }

    private void tryToStartSshTunnel() {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS_TO_START_SSH_TUNNEL; attempt++) {
            try {
                log.info("Starting SSH Tunnel, attempt: {}", attempt);
                sshClient = new SshClient(wireMockPort);
                Response response = wireMockClient.sendAreWiremockAndTunnelHealthyRequest(sshClient.getSshServerUrl());

                log.info("Checking SSH tunnel availability on {}", sshClient.getSshServerUrl());
                if (response.getStatusCode() != 200) {
                    log.info("Failed to connect SSH tunnel, status code: {}, message: {}",
                            response.getStatusCode(), response.asString());
                    throw new ConnectException("Status code of health endpoint is " + response.getStatusCode());
                }

                log.info("SSH tunnel is available");
                return;

            } catch (IOException e) {
                log.info("SSH tunnel is unavailable. Stopping it. Error: {}", e.getMessage());
                sshClient.close();

                if (attempt == MAX_ATTEMPTS_TO_START_SSH_TUNNEL) {
                    stopWireMock();
                    throw new RuntimeException("All attempts to start SSH tunnel are spent.");
                }
            }
        }
    }

    private void startWireMock() {
        wireMockPort = getAvailablePort();
        wireMockClient = new WireMockClient("http://localhost:" + wireMockPort);

        if (Boolean.parseBoolean(START_WIREMOCK_IN_DOCKER)) {
            log.info("Starting WireMock in Docker");
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "docker", "run", "-d", "--rm", "-p", wireMockPort + ":8080", "wiremock/wiremock:latest");
                Process process = processBuilder.start();
                process.waitFor(2, TimeUnit.MINUTES);
                dockerContainerId = new String(process.getInputStream().readAllBytes()).trim();
                process.destroyForcibly().waitFor();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Failed to start WireMock in Docker", e);
            }
            waitDockerStarted();
            wireMockServer = new WireMockServer(wireMockPort);
        } else {
            log.info("Starting local WireMock");
            wireMockServer = new WireMockServer(
                    WireMockConfiguration.wireMockConfig()
                            .port(wireMockPort)
                            .globalTemplating(true)
            );
            wireMockServer.start();
        }
    }

    private void stopWireMock() {
        if (Boolean.parseBoolean(START_WIREMOCK_IN_DOCKER)) {
            log.info("Stopping WireMock in Docker");
            try {
                Process dockerStopProcess = new ProcessBuilder("docker", "stop", dockerContainerId)
                        .redirectErrorStream(true)
                        .start();

                try (BufferedReader reader = new BufferedReader(dockerStopProcess.inputReader())) {
                    reader.lines().forEach(log::info);
                }

                dockerStopProcess.waitFor();
            } catch (IOException | InterruptedException e) {
                log.error("Failed to stop WireMock in Docker", e);
            }
        } else {
            log.info("Stopping local WireMock");
            if (wireMockServer.isRunning()) {
                wireMockServer.stop();
                wireMockServer.shutdownServer();
            }
        }
    }

    private void prepareStubs() {
        log.info("Preparing stubs for WireMock");

        createDefaultStub();
        wireMockServer.stubFor(WireMock.post("/foo").willReturn(WireMock.ok()));
        wireMockServer.stubFor(WireMock.post(WireMock.urlMatching("/bar.*")).willReturn(WireMock.ok()));

        log.info("All stubs for WireMock prepared");
    }

    private int getAvailablePort() {
        int port = ThreadLocalRandom.current().nextInt(8000, 9000);
        try (ServerSocket socket = new ServerSocket(port)) {
            return port;
        } catch (IOException e) {
            return getAvailablePort();
        }
    }

    private void waitDockerStarted() {
        log.info("Waiting for WireMock started in Docker");
        Awaitility.await()
                .pollInterval(Durations.TEN_SECONDS)
                .timeout(1, TimeUnit.MINUTES)
                .untilAsserted(() ->
                        Assertions.assertEquals(200, wireMockClient.sendIsWireMockHealthyRequest().getStatusCode()));
        log.info("WireMock is available in Docker");
    }

    private void createDefaultStub() {
        String body = JsonStubsBuilder.createJsonStub(
                "/default",
                """
                {
                  "default": true,
                  "success": true
                }
                """,
                null,
                "POST",
                200);
        wireMockClient.sendCreateStubRequest(body);
    }
}
