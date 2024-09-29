package dbp.exploreconnet.reservation.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationResponseDto {
    private Long id;
    private String userName;
    private String userEmail;
    private String placeName;
    private LocalDateTime date;
    private Integer numberOfPeople;
}
