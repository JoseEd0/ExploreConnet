package dbp.exploreconnet.reservation.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationRequestDto {
    private Long placeId;
    private LocalDateTime date;
    private Integer numberOfPeople;
}
