package mock;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

/**
 * A client for interacting with a WireMock server, providing methods to manage stubs and monitor server health.
 * It supports creating stubs, retrieving request logs, cleaning requests, and checking server health
 */

@RequiredArgsConstructor
public class WireMockClient {
    private final String wireMockUrl;

    public Response sendCreateStubRequest(String body) {
        String url = wireMockUrl + "/__admin/mappings";
        return sendPostRequest(url, body);
    }

    public Response sendGetRequestsRequest() {
        String url = wireMockUrl + "/__admin/requests";
        return sendGetRequest(url);
    }

    public Response sendCleanRequestsRequest() {
        String url = wireMockUrl + "/__admin/requests";
        return sendDeleteRequest(url);
    }

    public Response sendIsWireMockHealthyRequest() {
        String url = wireMockUrl + "/__admin/mappings";
        return sendGetRequest(url);
    }

    public Response sendAreWiremockAndTunnelHealthyRequest(String sshTunnelUrl) {
        String url = sshTunnelUrl + "/__admin/mappings";
        return sendGetRequest(url);
    }

    private Response sendGetRequest(String url) {
        return RestAssured.given()
                .relaxedHTTPSValidation() // Отключает проверку SSL
                .when()
                .get(url)
                .then()
                .extract()
                .response();
    }

    private Response sendPostRequest(String url, String body) {
        return RestAssured.given()
                .relaxedHTTPSValidation()
                .contentType("application/json")
                .body(body)
                .when()
                .post(url)
                .then()
                .extract()
                .response();
    }

    private Response sendDeleteRequest(String url) {
        return RestAssured.given()
                .relaxedHTTPSValidation()
                .when()
                .delete(url)
                .then()
                .extract()
                .response();
    }
}
