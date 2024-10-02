package dbp.exploreconnet.reservation.application;


import dbp.exploreconnet.reservation.domain.Reservation;
import dbp.exploreconnet.reservation.domain.ReservationService;
import dbp.exploreconnet.reservation.dto.ReservationRequestDto;
import dbp.exploreconnet.reservation.dto.ReservationResponseDto;
import dbp.exploreconnet.reservation.dto.UserReservationResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping
    public ResponseEntity<ReservationResponseDto> createReservation(@RequestBody ReservationRequestDto reservationRequest) {
        ReservationResponseDto response = reservationService.createReservation(reservationRequest);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @GetMapping("/user")
    public ResponseEntity<List<UserReservationResponseDto>> getReservationsByUser() {
        List<UserReservationResponseDto> reservations = reservationService.getReservationsByUser();
        return ResponseEntity.ok(reservations);
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @PreAuthorize("hasAuthority('USER')")
    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(@PathVariable Long id, @RequestBody Reservation reservation) {
        return ResponseEntity.ok(reservationService.updateReservation(id, reservation));
    }

    @PreAuthorize("hasAuthority('USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

}
