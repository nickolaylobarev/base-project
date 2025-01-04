package assertions;

import models.booking.Booking;
import models.booking.BookingSuccessResponse;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookingAssertions {

    public static void checkBooking(Booking booking) {
        Assertions.assertNotNull(booking.getFirstname());
        assertTrue(booking.getFirstname().matches("[0-9a-zA-Z]+([\\s-][0-9a-zA-Z]+)*"),
                "Invalid firstname " + booking.getFirstname());
        Assertions.assertNotNull(booking.getLastname());
        Assertions.assertNotNull(booking.getTotalprice());
        Assertions.assertNotNull(booking.getDepositpaid());
        Assertions.assertNotNull(booking.getBookingdates());
        Assertions.assertNotNull(booking.getAdditionalneeds());
    }

    public static void checkBookingSuccessResponse(BookingSuccessResponse bookingSuccessResponse) {
        Assertions.assertNotNull(bookingSuccessResponse.getBookingid());
        assertTrue(bookingSuccessResponse.getBookingid().getBookingid().toString().matches("[0-9]{1,32}"),
                "Invalid bookingid " + bookingSuccessResponse.getBookingid().getBookingid());
        checkBooking(bookingSuccessResponse.getBooking());
    }
}
