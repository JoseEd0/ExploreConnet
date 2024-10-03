package dbp.exploreconnet.reservation.infraestructure;

import dbp.exploreconnet.AbstractContainerBaseTest;
import dbp.exploreconnet.reservation.domain.Reservation;
import dbp.exploreconnet.place.domain.Place;
import dbp.exploreconnet.reservation.infrastructure.ReservationRepository;
import dbp.exploreconnet.user.domain.User;
import dbp.exploreconnet.place.domain.PlaceCategory;
import dbp.exploreconnet.user.domain.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReservationRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User user;
    private Place place;
    private Reservation reservation1;
    private Reservation reservation2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("password123");
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());

        testEntityManager.persist(user);


        place = new Place();
        place.setName("Cafe Central");
        place.setAddress("123 Main St");
        place.setDescription("A cozy coffee shop.");
        place.setCategory(PlaceCategory.CAFETERIA);
        place.setOpeningHours("08:00 - 20:00");
        place.setLatitude(40.7128);
        place.setLongitude(-74.0060);

        testEntityManager.persist(place);

        reservation1 = new Reservation();
        reservation1.setUser(user);
        reservation1.setPlace(place);
        reservation1.setDate(LocalDateTime.now().plusDays(1));
        reservation1.setNumberOfPeople(4);

        reservation2 = new Reservation();
        reservation2.setUser(user);
        reservation2.setPlace(place);
        reservation2.setDate(LocalDateTime.now().plusDays(2));
        reservation2.setNumberOfPeople(2);

        testEntityManager.persist(reservation1);
        testEntityManager.persist(reservation2);

        testEntityManager.flush();
    }

    @Test
    void testSaveReservation() {
        Reservation newReservation = new Reservation();
        newReservation.setUser(user);
        newReservation.setPlace(place);
        newReservation.setDate(LocalDateTime.now().plusDays(3));
        newReservation.setNumberOfPeople(3);

        Reservation savedReservation = reservationRepository.save(newReservation);

        assertNotNull(savedReservation.getId());
        assertEquals(3, savedReservation.getNumberOfPeople());
        assertEquals(user.getId(), savedReservation.getUser().getId());
        assertEquals(place.getId(), savedReservation.getPlace().getId());
    }

    @Test
    void testFindById() {
        Reservation foundReservation = reservationRepository.findById(reservation1.getId()).orElse(null);

        assertNotNull(foundReservation);
        assertEquals(reservation1.getId(), foundReservation.getId());
        assertEquals(reservation1.getDate(), foundReservation.getDate());
        assertEquals(user.getId(), foundReservation.getUser().getId());
    }

    @Test
    void testFindAll() {
        List<Reservation> reservations = reservationRepository.findAll();
        assertEquals(2, reservations.size());
    }

    @Test
    void testFindByUser() {
        List<Reservation> userReservations = reservationRepository.findByUser(user);

        assertEquals(2, userReservations.size());
        assertEquals(user.getId(), userReservations.get(0).getUser().getId());
    }

    @Test
    void testUpdateReservation() {
        reservation1.setNumberOfPeople(5);
        reservationRepository.save(reservation1);

        Reservation updatedReservation = reservationRepository.findById(reservation1.getId()).orElse(null);

        assertNotNull(updatedReservation);
        assertEquals(5, updatedReservation.getNumberOfPeople());
    }

    @Test
    void testDeleteReservation() {
        reservationRepository.delete(reservation1);
        Reservation foundReservation = reservationRepository.findById(reservation1.getId()).orElse(null);

        assertNull(foundReservation);
    }
}
