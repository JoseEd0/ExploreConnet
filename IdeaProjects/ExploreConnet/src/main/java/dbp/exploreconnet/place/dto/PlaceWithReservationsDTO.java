package dbp.exploreconnet.place.dto;

import dbp.exploreconnet.reservation.domain.Reservation;

import java.util.List;

public class PlaceWithReservationsDto extends PlaceDto {
    private List<Reservation> reservations;

    public PlaceWithReservationsDto(Long id, String name, String address, List<Reservation> reservations) {
        super(id, name, address);
        this.reservations = reservations;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
}
