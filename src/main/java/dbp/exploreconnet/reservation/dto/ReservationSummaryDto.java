package dbp.exploreconnet.reservation.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationSummaryDto {
    private Long reservationId;
    private LocalDateTime date;
    private Integer numberOfPeople;
    private Long placeId;
    private String placeName;
    private String userName;
    private String userEmail;
}
