package dbp.exploreconnet.reservation.domain;

import dbp.exploreconnet.reservation.infrastructure.ReservationRepository;
import dbp.exploreconnet.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    public Reservation createReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new RuntimeException("Reservation not found"));
    }

    public Reservation updateReservation(Long id, Reservation reservation) {
        Reservation existingReservation = getReservationById(id);
        existingReservation.setDate(reservation.getDate());
        existingReservation.setNumberOfPeople(reservation.getNumberOfPeople());
        return reservationRepository.save(existingReservation);
    }

    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    public List<Reservation> getReservationsForUser(User user) {
        return reservationRepository.findByUser(user);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }
}
