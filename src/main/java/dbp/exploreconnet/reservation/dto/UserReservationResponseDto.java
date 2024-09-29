package dbp.exploreconnet.reservation.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserReservationResponseDto {
    private String placeName;
    private LocalDateTime date;
    private Integer numberOfPeople;
}
