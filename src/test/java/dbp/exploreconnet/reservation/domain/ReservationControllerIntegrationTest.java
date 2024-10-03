package dbp.exploreconnet.reservation.domain;

import dbp.exploreconnet.place.domain.Place;
import dbp.exploreconnet.reservation.application.ReservationController;
import dbp.exploreconnet.reservation.domain.Reservation;
import dbp.exploreconnet.reservation.domain.ReservationService;
import dbp.exploreconnet.reservation.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationControllerIntegrationTest {

    @InjectMocks
    private ReservationController reservationController;

    @Mock
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa los mocks
    }

    @Test
    public void testCreateReservation() {
        // Arrange
        ReservationRequestDto requestDto = new ReservationRequestDto();
        requestDto.setPlaceId(1L);
        requestDto.setDate(LocalDateTime.now());
        requestDto.setNumberOfPeople(4);

        ReservationResponseDto responseDto = new ReservationResponseDto();
        responseDto.setId(1L);
        responseDto.setUserName("John Doe");
        responseDto.setUserEmail("john@example.com");
        responseDto.setPlaceName("Place A");
        responseDto.setDate(LocalDateTime.now());
        responseDto.setNumberOfPeople(4);

        when(reservationService.createReservation(requestDto)).thenReturn(responseDto);

        // Act
        ResponseEntity<ReservationResponseDto> response = reservationController.createReservation(requestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
        verify(reservationService, times(1)).createReservation(requestDto);
    }

    @Test
    public void testGetReservationsByUser() {
        // Arrange
        UserReservationResponseDto reservation1 = new UserReservationResponseDto();
        reservation1.setPlaceName("Place A");
        reservation1.setDate(LocalDateTime.now());
        reservation1.setNumberOfPeople(2);

        UserReservationResponseDto reservation2 = new UserReservationResponseDto();
        reservation2.setPlaceName("Place B");
        reservation2.setDate(LocalDateTime.now());
        reservation2.setNumberOfPeople(5);

        List<UserReservationResponseDto> reservations = Arrays.asList(reservation1, reservation2);

        when(reservationService.getReservationsByUser()).thenReturn(reservations);

        // Act
        ResponseEntity<List<UserReservationResponseDto>> response = reservationController.getReservationsByUser();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reservations, response.getBody());
        verify(reservationService, times(1)).getReservationsByUser();
    }

    @Test
    public void testGetReservationById() {
        // Arrange
        Long reservationId = 1L;
        ReservationSummaryDto summaryDto = new ReservationSummaryDto();
        summaryDto.setReservationId(reservationId);
        summaryDto.setDate(LocalDateTime.now());
        summaryDto.setNumberOfPeople(4);
        summaryDto.setPlaceId(1L);
        summaryDto.setPlaceName("Place A");
        summaryDto.setUserName("John Doe");
        summaryDto.setUserEmail("john@example.com");

        when(reservationService.getReservationById(reservationId)).thenReturn(summaryDto);

        // Act
        ResponseEntity<ReservationSummaryDto> response = reservationController.getReservationById(reservationId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(summaryDto, response.getBody());
        verify(reservationService, times(1)).getReservationById(reservationId);
    }

    @Test
    public void testUpdateReservation() {
        // Arrange
        Long reservationId = 1L;
        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setPlace(new Place());
        reservation.setDate(LocalDateTime.now());
        reservation.setNumberOfPeople(4);

        when(reservationService.updateReservation(reservationId, reservation)).thenReturn(reservation);

        // Act
        ResponseEntity<Reservation> response = reservationController.updateReservation(reservationId, reservation);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reservation, response.getBody());
        verify(reservationService, times(1)).updateReservation(reservationId, reservation);
    }


    @Test
    public void testDeleteReservation() {
        // Arrange
        Long reservationId = 1L;
        doNothing().when(reservationService).deleteReservation(reservationId);

        // Act
        ResponseEntity<Void> response = reservationController.deleteReservation(reservationId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(reservationService, times(1)).deleteReservation(reservationId);
    }

    @Test
    public void testGetAllReservations() {
        // Arrange
        ReservationSummaryDto reservation1 = new ReservationSummaryDto();
        reservation1.setReservationId(1L);
        reservation1.setPlaceId(1L);
        reservation1.setPlaceName("Place A");
        reservation1.setUserName("John Doe");
        reservation1.setUserEmail("john@example.com");
        reservation1.setDate(LocalDateTime.now());
        reservation1.setNumberOfPeople(4);

        ReservationSummaryDto reservation2 = new ReservationSummaryDto();
        reservation2.setReservationId(2L);
        reservation2.setPlaceId(2L);
        reservation2.setPlaceName("Place B");
        reservation2.setUserName("Jane Doe");
        reservation2.setUserEmail("jane@example.com");
        reservation2.setDate(LocalDateTime.now());
        reservation2.setNumberOfPeople(5);

        List<ReservationSummaryDto> reservations = Arrays.asList(reservation1, reservation2);

        when(reservationService.getAllReservations()).thenReturn(reservations);

        // Act
        ResponseEntity<List<ReservationSummaryDto>> response = reservationController.getAllReservations();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reservations, response.getBody());
        verify(reservationService, times(1)).getAllReservations();
    }
}
