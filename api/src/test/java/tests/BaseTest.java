package tests;

import decorators.TestLoggerExtension;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.extension.ExtendWith;
import static properties.PrivateProperties.password;
import static properties.PrivateProperties.username;
import static allure.AllureUtils.step;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ExtendWith(TestLoggerExtension.class)
abstract public class BaseTest {

    public static RequestSpecification requestSpec(String url) {
        return new RequestSpecBuilder()
                .setBaseUri(url)
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "Basic " + getToken(url))
                .addHeader("Accept", "application/json")
                .build();
    }

    public static ResponseSpecification responseSpecOK200() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .build();
    }

    public static ResponseSpecification responseSpecOK200or201() {
        return new ResponseSpecBuilder()
                .expectStatusCode(anyOf(equalTo(200), equalTo(201)))
                .build();
    }

    public static ResponseSpecification responseSpecError400() {
        return new ResponseSpecBuilder()
                .expectStatusCode(400)
                .build();
    }

    public static ResponseSpecification responseSpec(int status) {
        return new ResponseSpecBuilder()
                .expectStatusCode(status)
                .build();
    }

    public static void installSpecification(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        step("Setting up specification");
        RestAssured.requestSpecification = requestSpec;
        RestAssured.responseSpecification = responseSpec;
    }

    public static void removeSetSpecification() {
        step("Removing specification");
        RestAssured.requestSpecification = null;
        RestAssured.responseSpecification = null;
    }

    public static void installSpecification(RequestSpecification requestSpec) {
        RestAssured.requestSpecification = requestSpec;
    }

    public static void installSpecification(ResponseSpecification responseSpec) {
        RestAssured.responseSpecification = responseSpec;
    }

    public static String getToken(String url) {
        log.info("Getting an authorization token");
        responseSpecOK200();
        Map<String, String> user = new HashMap<>();
        user.put("username", username);
        user.put("password", password);
        Response response = given()
                .body(user)
                .when()
                .contentType(ContentType.JSON)
                .post(url + "/auth")
                .then()
                .extract().response();

        JsonPath jsonPath = response.jsonPath();
        return response.statusCode() == 200 ? "YWRtaW46cGFzc3dvcmQxMjM=" : jsonPath.get("token").toString(); // todo remove hardcode
    }
}
