package tests.e2e;

import static allure.AllureUtils.step;
import allure.annotations.JiraIssue;
import static assertions.BookingAssertions.checkBookingSuccessResponse;
import builders.BookingBuilder;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import models.booking.Booking;
import models.booking.BookingId;
import models.booking.BookingSuccessResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static properties.PublicProperties.BOOKER_URL;
import tests.BaseTest;
import static tests.integration.BookingIntegrationTests.createBooking;
import static tests.integration.BookingIntegrationTests.getAllBookingIds;
import static tests.integration.BookingIntegrationTests.getBooking;
import static tests.integration.BookingIntegrationTests.removeBooking;

@Tag("E2E")
public class BookingE2ETests extends BaseTest {

    @BeforeAll
    public static void setSpecification() {
        installSpecification(requestSpec(BOOKER_URL), responseSpecOK200Or201());
    }

    @AfterAll
    public static void removeSpecification() {
        removeSetSpecification();
    }

    @Test
    @JiraIssue("XXXX-2001")
    @DisplayName("Create successful Booking based on existing data and remove it")
    @Description("Create Booking based on existing data, remove it and verify that it has been removed")
    @AllureId("20001")
    public void createBookingBasedOnExistingDataTest() {
        step("Get all Booking Ids");
        List<BookingId> bookingIds = getAllBookingIds();

        step("Get random booking by its ID and verify data");
        int randomBookingId = bookingIds.get(ThreadLocalRandom.current().nextInt(bookingIds.size())).getBookingid();
        Booking randomBooking = getBooking(randomBookingId);

        step("Prepare Booking message");
        String randomIdentifier = RandomStringUtils.randomNumeric(10);
        Booking booking = BookingBuilder.createDefaultBooking(randomIdentifier);
        booking.setAdditionalneeds(randomBooking.getAdditionalneeds());

        step("Send creating Booking message");
        BookingSuccessResponse bookingSuccessResponse = createBooking(booking);
        checkBookingSuccessResponse(bookingSuccessResponse);
        Assertions.assertEquals("TestFirstName-" + randomIdentifier + "-autotests",
                bookingSuccessResponse.getBooking().getFirstname());

        step("Send deleting Booking message");
        removeBooking(bookingSuccessResponse.getBookingid().getBookingid());

        step("Send getting Booking message to make sure it has been removed");
        AssertionError error = assertThrows(AssertionError.class, () ->
                getBooking(bookingSuccessResponse.getBookingid().getBookingid()));

        Assertions.assertTrue(error.getMessage().contains("Expected status code (<200> or <201>) but was <404>"));
    }
}
