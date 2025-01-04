package models.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDates {

    @Builder.Default
    private String checkin = LocalDate.now().toString();

    @Builder.Default
    private String checkout = LocalDate.now().plusDays(10).toString();
}
