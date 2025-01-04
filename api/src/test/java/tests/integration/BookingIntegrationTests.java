package tests.integration;

import static allure.AllureUtils.step;
import allure.annotations.JiraIssue;
import static assertions.BookingAssertions.checkBooking;
import static assertions.BookingAssertions.checkBookingSuccessResponse;
import builders.BookingBuilder;
import builders.RandomBooking;
import builders.RandomBookingResolver;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import models.booking.Booking;
import models.booking.BookingId;
import models.booking.BookingSuccessResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import static properties.PublicProperties.BOOKER_URL;
import tests.BaseTest;
import static utils.AwaitilityUtil.waitUntilAsserted;

@Tag("Integration")
public class BookingIntegrationTests extends BaseTest {

    @BeforeAll
    public static void setSpecification() {
        installSpecification(requestSpec(BOOKER_URL), responseSpecOK200Or201());
    }

    @AfterAll
    public static void removeSpecification() {
        removeSetSpecification();
    }

    @Test
    @JiraIssue("XXXX-1001")
    @DisplayName("Get random Booking")
    @Description("Get random Booking and verify its data")
    @AllureId("10000")
    public void getBookingTest() {
        step("Get all Booking Ids");
        List<BookingId> bookingIds = getAllBookingIds();

        step("Get random booking by its ID and verify data");
        int randomBookingId = bookingIds.get(ThreadLocalRandom.current().nextInt(bookingIds.size())).getBookingid();
        Booking randomBooking = getBooking(randomBookingId);
        checkBooking(randomBooking);
    }

    @Test
    @JiraIssue("XXXX-1001")
    @DisplayName("Get random Booking Id")
    @Description("Get random Booking Id and make sure it is large")
    @AllureId("10001")
    public void getBookingAwaitTest() {
        step("Get all Booking Ids");
        List<BookingId> bookingIds = getAllBookingIds();

        step("Get random Booking Id and make sure it is large");
//        await().untilAsserted(
//                () -> {
//                    int randomBookingId = bookingIds.get(ThreadLocalRandom.current()
//                            .nextInt(bookingIds.size())).getBookingid();
//                    System.out.println("Current randomBookingId is " + randomBookingId);
//                    Assertions.assertTrue(randomBookingId > 1000, "There is no such big BookingId");
//                });
        int finalRandomBookingId = waitUntilAsserted(
                () -> {
                    int randomBookingId = bookingIds.get(ThreadLocalRandom.current()
                            .nextInt(bookingIds.size())).getBookingid();
                    System.out.println("Current randomBookingId is " + randomBookingId);
                    Assertions.assertTrue(randomBookingId > 1000, "There is no such big BookingId");
                    return randomBookingId;
                });
        System.out.println("Final randomBookingId is " + finalRandomBookingId);
    }

    @Test
    @JiraIssue("XXXX-1002")
    @DisplayName("Create successful Booking")
    @Description("Create Booking and verify its data")
    @AllureId("10002")
    public void createBookingTest() {
        String randomIdentifier = RandomStringUtils.randomNumeric(10);

        step("Prepare Booking message");
        Booking booking = BookingBuilder.createDefaultBooking(randomIdentifier);

        step("Send creating Booking message");
        BookingSuccessResponse bookingSuccessResponse = createBooking(booking);
        checkBookingSuccessResponse(bookingSuccessResponse);
        Assertions.assertEquals("TestFirstName-" + randomIdentifier + "-autotests",
                bookingSuccessResponse.getBooking().getFirstname());

        step("Send deleting Booking message");
        removeBooking(bookingSuccessResponse.getBookingid().getBookingid());
    }

    @Test
    @Tag("Smoke")
    @JiraIssue("XXXX-1002")
    @DisplayName("Create successful random Booking")
    @Description("Create random Booking and verify its data")
    @AllureId("10003")
    public void createRandomBookingTest() {
        step("Prepare random Booking message");
        Booking booking = BookingBuilder.createRandomBooking();

        step("Send creating Booking message");
        BookingSuccessResponse bookingSuccessResponse = createBooking(booking);
        checkBookingSuccessResponse(bookingSuccessResponse);
        Assertions.assertEquals(booking.getFirstname(), bookingSuccessResponse.getBooking().getFirstname());

        step("Send deleting Booking message");
        removeBooking(bookingSuccessResponse.getBookingid().getBookingid());
    }

    @Test
    @JiraIssue("XXXX-1002")
    @DisplayName("Create successful random Booking")
    @Description("Create random Booking and verify its data")
    @AllureId("10004")
    @ExtendWith(RandomBookingResolver.class)
    public void createExtendedRandomBookingTest(@RandomBooking Booking booking) {
        step("Send creating Booking message");
        BookingSuccessResponse bookingSuccessResponse = createBooking(booking);
        checkBookingSuccessResponse(bookingSuccessResponse);
        Assertions.assertEquals(booking.getFirstname(), bookingSuccessResponse.getBooking().getFirstname());

        step("Send deleting Booking message");
        removeBooking(bookingSuccessResponse.getBookingid().getBookingid());
    }

    @Test
    @JiraIssue("XXXX-1002")
    @DisplayName("Create successful default Booking")
    @Description("Create default Booking and verify its data")
    @AllureId("10004")
    public void createDefaultBookingTest() {
        step("Prepare default Booking message");
        Booking booking = new Booking();

        step("Send creating Booking message");
        BookingSuccessResponse bookingSuccessResponse = createBooking(booking);
        checkBookingSuccessResponse(bookingSuccessResponse);
        Assertions.assertEquals(booking.getFirstname(), bookingSuccessResponse.getBooking().getFirstname());

        step("Send deleting Booking message");
        removeBooking(bookingSuccessResponse.getBookingid().getBookingid());
    }

    @ParameterizedTest
    @CsvSource({"firstname1, lastname1", "firstname2 ,lastname2", " firstname3 , lastname3 "})
    @JiraIssue("XXXX-1002")
    @DisplayName("Create successful csv parameterized Booking")
    @Description("Create csv parameterized Booking and verify its data")
    @AllureId("10005")
    public void createCsvParameterizedBookingTest(String firstname, String lastname) {
        step("Prepare csv parameterized Booking message");
        Booking booking = Booking.builder().firstname(firstname).lastname(lastname).build();

        step("Send creating Booking message");
        BookingSuccessResponse bookingSuccessResponse = createBooking(booking);
        checkBookingSuccessResponse(bookingSuccessResponse);
        Assertions.assertEquals(booking.getFirstname(), bookingSuccessResponse.getBooking().getFirstname());

        step("Send deleting Booking message");
        removeBooking(bookingSuccessResponse.getBookingid().getBookingid());
    }

    @ParameterizedTest
    @MethodSource(value = "testBookings")
    @JiraIssue("XXXX-1002")
    @DisplayName("Create successful method parameterized Booking")
    @Description("Create method parameterized Booking and verify its data")
    @AllureId("10006")
    public void createMethodParameterizedBookingTest(Booking booking) {
        step("Send creating Booking message");
        BookingSuccessResponse bookingSuccessResponse = createBooking(booking);
        checkBookingSuccessResponse(bookingSuccessResponse);
        Assertions.assertEquals(booking.getFirstname(), bookingSuccessResponse.getBooking().getFirstname());

        step("Send deleting Booking message");
        removeBooking(bookingSuccessResponse.getBookingid().getBookingid());
    }

    public static List<BookingId> getAllBookingIds() {
        return given()
                .when()
                .get("/booking")
                .then()
                .extract().body().jsonPath().getList(".", BookingId.class);
    }

    public static Booking getBooking(int id) {
        return given()
                .when()
                .get("/booking/" + id)
                .then()
                .extract().as(Booking.class);
    }

    public static BookingSuccessResponse createBooking(Booking booking) {
        return given()
                .body(booking)
                .when()
                .post("/booking")
                .then()
                .log().all()
                .extract().as(BookingSuccessResponse.class);
    }

    public static void removeBooking(int id) {
        Response response = given()
                .when()
                .delete("/booking/" + id)
                .then()
                .extract().response();
        Assertions.assertEquals(201, response.statusCode());
    }

    private static List<Booking> testBookings() {
        return List.of(
                Booking.builder().firstname("Parameterized firstname1").lastname("Parameterized lastname2").build(),
                Booking.builder().totalprice(42).depositpaid(true).build(),
                Booking.builder().depositpaid(false).additionalneeds("").build()
        );
    }
}
