package models.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Builder.Default
    private String firstname = "Default firstname";

    @Builder.Default
    private String lastname = "Default lastname";

    @Builder.Default
    private Integer totalprice = 69;

    @Builder.Default
    private Boolean depositpaid = true;

    @Builder.Default
    private BookingDates bookingdates = BookingDates.builder().build();

    @Builder.Default
    private String additionalneeds = "Default additionalneeds";
}
