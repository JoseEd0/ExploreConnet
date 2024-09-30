package dbp.exploreconnet.reservation.domain;

import dbp.exploreconnet.auth.utils.AuthorizationUtils;
import dbp.exploreconnet.email.domain.EmailService;
import dbp.exploreconnet.events.SignIn.QRCodeService;
import dbp.exploreconnet.exceptions.ResourceNotFoundException;
import dbp.exploreconnet.place.domain.Place;
import dbp.exploreconnet.place.infrastructure.PlaceRepository;
import dbp.exploreconnet.reservation.dto.ReservationRequestDto;
import dbp.exploreconnet.reservation.dto.ReservationResponseDto;
import dbp.exploreconnet.reservation.dto.UserReservationResponseDto;
import dbp.exploreconnet.reservation.infrastructure.ReservationRepository;
import dbp.exploreconnet.user.domain.User;
import dbp.exploreconnet.user.infrastructure.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private UserRepository<User> userRepository;
    @Autowired
    private AuthorizationUtils authorizationUtils;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private EmailService emailService;

    public ReservationResponseDto createReservation(ReservationRequestDto reservationRequest) {
        String currentUserEmail = authorizationUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Place place = placeRepository.findById(reservationRequest.getPlaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Place not found"));

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setPlace(place);
        reservation.setDate(reservationRequest.getDate());
        reservation.setNumberOfPeople(reservationRequest.getNumberOfPeople());

        Reservation savedReservation = reservationRepository.save(reservation);

        String qrCodeUrl = qrCodeService.generateQRCodeUrl(
                savedReservation.getId(),
                savedReservation.getDate().toString(),
                savedReservation.getNumberOfPeople(),
                savedReservation.getPlace().getName(),  // Acceder al nombre del lugar
                savedReservation.getUser().getFullName()    // Acceder al nombre del usuario
        );


        try {
            emailService.sendReservationQRCode(user.getEmail(), user.getFullName(), savedReservation, qrCodeUrl);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending email with QR code", e);
        }

        ReservationResponseDto responseDto = new ReservationResponseDto();
        responseDto.setId(savedReservation.getId());
        responseDto.setUserName(user.getFullName());
        responseDto.setUserEmail(user.getEmail());
        responseDto.setPlaceName(place.getName());
        responseDto.setDate(savedReservation.getDate());
        responseDto.setNumberOfPeople(savedReservation.getNumberOfPeople());

        return responseDto;
    }



    public List<UserReservationResponseDto> getReservationsByUser() {
        String currentUserEmail = authorizationUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return reservationRepository.findByUser(user).stream().map(reservation -> {
            UserReservationResponseDto responseDto = new UserReservationResponseDto();
            responseDto.setPlaceName(reservation.getPlace().getName());
            responseDto.setDate(reservation.getDate());
            responseDto.setNumberOfPeople(reservation.getNumberOfPeople());
            return responseDto;
        }).collect(Collectors.toList());
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

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

}
