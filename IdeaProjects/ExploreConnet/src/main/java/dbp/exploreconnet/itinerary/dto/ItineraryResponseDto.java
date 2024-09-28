package dbp.exploreconnet.itinerary.dto;

import dbp.exploreconnet.reservation.domain.Reservation;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ItineraryResponseDto {
    private Long id;
    private String name;
    private Long userId;
    private List<Reservation> reservations;
}
