package builders;

import io.qameta.allure.Step;
import java.io.IOException;
import java.time.LocalDate;
import models.booking.Booking;
import models.booking.BookingDates;
import net.datafaker.Faker;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static utils.JsonMessageTestUtils.jsonMessage;

public class BookingBuilder {

    @Step("Creating default Booking")
    public static Booking createDefaultBooking(String randomIdentifier) {
        randomIdentifier = randomIdentifier == null || randomIdentifier.isEmpty() ?
                randomNumeric(10) : randomIdentifier;
        Booking booking = createBooking(null);
        return BookingBuilder.changeFirstName(booking, "TestFirstName-" + randomIdentifier + "-autotests");
    }

    public static Booking createBooking(String filePath) {
        filePath = filePath == null || filePath.isEmpty() ? "/test-data/booking-template.json" : filePath;
        try {
            return jsonMessage(filePath, Booking.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Booking from file: " + filePath, e);
        }
    }

    private static Booking changeFirstName(Booking booking, String firstName) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }

        return new Booking(
                firstName,
                booking.getLastname(),
                booking.getTotalprice(),
                booking.getDepositpaid(),
                booking.getBookingdates(),
                booking.getAdditionalneeds()
        );
    }

    public static Booking createRandomBooking() {
        Faker faker = new Faker();
        LocalDate checkIn = LocalDate.now().minusDays(faker.random().nextLong(0, 365));

        return Booking.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .totalprice(faker.random().nextInt(50, 5000))
                .depositpaid(faker.random().nextBoolean())
                .bookingdates(
                        new BookingDates(
                                checkIn.toString(),
                                checkIn.plusDays(faker.random().nextLong(1, 30)).toString()
                        )
                )
                .additionalneeds(faker.food().dish())
                .build();
    }
}
