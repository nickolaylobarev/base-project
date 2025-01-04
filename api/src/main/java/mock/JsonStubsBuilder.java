package mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static utils.JsonMessageTestUtils.jsonMessageFromBody;
import static utils.JsonMessageTestUtils.toJson;

/**
 * A utility class for building JSON stubs for WireMock with customizable request and response parameters.
 * It supports creating stubs with URLs, methods, response bodies, headers, and status codes
 */

public class JsonStubsBuilder {

    private static final Map<String, String> DEFAULT_HEADERS = Map.of(
            "Content-Type", "application/json"
    );

    public static String createJsonStub(
            String stubUrl,
            String responseBody,
            Map<String, String> additionalHeaders,
            String method,
            Integer statusCode) {

        if (additionalHeaders == null) {
            additionalHeaders = new HashMap<>();
        }

        Map<String, Object> request = new HashMap<>();
        request.put("method", method);
        request.put("url", stubUrl);

        Map<String, Object> response = new HashMap<>();
        response.put("status", statusCode);
        response.put("jsonBody", jsonMessageFromBody(responseBody, Object.class));
        response.put("headers", mergeHeaders(additionalHeaders));
        response.put("transformers", List.of("response-template"));

        Map<String, Object> stub = new HashMap<>();
        stub.put("request", request);
        stub.put("response", response);

        return toJson(stub);
    }

    public static String createJsonStubWithUrlPattern(
            String stubUrlPattern,
            String responseBody,
            Map<String, String> additionalHeaders,
            String method,
            int statusCode) {

        if (additionalHeaders == null) {
            additionalHeaders = new HashMap<>();
        }

        Map<String, Object> request = new HashMap<>();
        request.put("method", method);
        request.put("urlPathPattern", stubUrlPattern);

        Map<String, Object> response = new HashMap<>();
        response.put("status", statusCode);
        response.put("jsonBody", jsonMessageFromBody(responseBody, Object.class));
        response.put("headers", mergeHeaders(additionalHeaders));
        response.put("transformers", List.of("response-template"));

        Map<String, Object> stub = new HashMap<>();
        stub.put("request", request);
        stub.put("response", response);

        return toJson(stub);
    }

    private static Map<String, String> mergeHeaders(Map<String, String> additionalHeaders) {
        Map<String, String> mergedHeaders = new HashMap<>(DEFAULT_HEADERS);
        mergedHeaders.putAll(additionalHeaders);
        return mergedHeaders;
    }
}
