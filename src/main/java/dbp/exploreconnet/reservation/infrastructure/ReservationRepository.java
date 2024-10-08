package dbp.exploreconnet.reservation.infrastructure;

import dbp.exploreconnet.place.domain.Place;
import dbp.exploreconnet.reservation.domain.Reservation;
import dbp.exploreconnet.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation,Long> {
    List<Reservation> findByUser(User user);
    List<Reservation> findByPlace(Place place);
}
