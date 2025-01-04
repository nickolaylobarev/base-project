package allure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;

/**
 * Client for interacting with Allure TestOps API
 *
 * <p>Provides methods to upload attachments to test cases and retrieve existing attachments,
 * using authentication via a project-specific API token</p>
 */

public class AllureClient {
    private static final String URL = "https://{projectName}.testops.cloud";
    private static final String USER_TOKEN = "{allureToken}";
    private static final String TOKEN = getToken();

    public static void uploadTestCaseTextAttachment(String testCaseId, String attachmentName, String attachmentBody) {
        List<Content> attachments = getTestCaseAttachments(testCaseId);
        if (attachments == null || attachments.stream()
                .noneMatch(content -> content.getName().equals(attachmentName))) {
            String boundary = "boundary";

            Response response = RestAssured.given()
                    .contentType("multipart/form-data; boundary=" + boundary)
                    .header("Authorization", "Bearer " + TOKEN)
                    .multiPart("file", attachmentName, attachmentBody, String.valueOf(ContentType.TEXT))
                    .param("testCaseId", testCaseId)
                    .when()
                    .post(URL + "/api/rs/testcase/attachment");

            if (response.getStatusCode() != 200) {
                System.out.println("Error uploading attachment: " + response.getStatusLine());
            }
        }
    }

    private static List<Content> getTestCaseAttachments(String testCaseId) {
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + TOKEN)
                .param("testCaseId", testCaseId)
                .when()
                .get(URL + "/api/rs/testcase/attachment");

        if (response.getStatusCode() == 200) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Attachment attachment = objectMapper.readValue(response.getBody().asString(), Attachment.class);
                return attachment.getTotalElements() == 0 ? null : attachment.getContent();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String getToken() {
        Response response = RestAssured.given()
                .contentType(ContentType.URLENC)
                .formParam("grant_type", "apitoken")
                .formParam("scope", "openId")
                .formParam("token", USER_TOKEN)
                .when()
                .post(URL + "/api/uaa/oauth/token");

        if (response.getStatusCode() == 200) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                AuthResponse tokenResponse = objectMapper.readValue(response.getBody().asString(), AuthResponse.class);
                return tokenResponse.getAccessToken();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static class AuthResponse {
        private String scope;
        private String accessToken;
        private String jti;
        private String expiresIn;
        private String tokenType;

        public String getAccessToken() {
            return accessToken;
        }
    }

    private static class Attachment {
        private List<Content> content;
        private int totalElements;

        public List<Content> getContent() {
            return content;
        }

        public int getTotalElements() {
            return totalElements;
        }
    }

    private static class Content {
        private Integer id;
        private String name;
        private String contentType;
        private Integer contentLength;
        private Boolean missed;

        public String getName() {
            return name;
        }
    }
}
