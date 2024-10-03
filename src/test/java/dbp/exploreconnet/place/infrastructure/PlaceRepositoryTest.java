package dbp.exploreconnet.place.infrastructure;

import dbp.exploreconnet.AbstractContainerBaseTest;
import dbp.exploreconnet.place.domain.Place;
import dbp.exploreconnet.place.domain.PlaceCategory;
import dbp.exploreconnet.promotion.domain.Promotion;
import dbp.exploreconnet.review.domain.Review;
import dbp.exploreconnet.reservation.domain.Reservation;
import dbp.exploreconnet.user.domain.User;
import dbp.exploreconnet.user.domain.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PlaceRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User owner;
    private Place place;
    private Review review;
    private Reservation reservation;
    private Promotion promotion;

    @BeforeEach
    void setUp() {
        // Crear un User como Owner del Place
        owner = new User();
        owner.setFullName("Owner John");
        owner.setEmail("owner@example.com");
        owner.setPassword("password123");
        owner.setRole(Role.OWNER);
        owner.setCreatedAt(LocalDateTime.now());

        testEntityManager.persist(owner);

        // Crear un Place
        place = new Place();
        place.setName("Amazing Restaurant");
        place.setAddress("123 Main Street");
        place.setDescription("A top-class restaurant with gourmet meals.");
        place.setCategory(PlaceCategory.RESTAURANT);
        place.setOpeningHours("09:00 - 22:00");
        place.setLatitude(40.7128);
        place.setLongitude(-74.0060);
        place.setOwner(owner);

        testEntityManager.persist(place);

        // Crear un Review
        review = new Review();
        review.setUser(owner);
        review.setPlace(place);
        review.setComment("Excellent place, great service!");
        review.setRating(5);

        testEntityManager.persist(review);

        // Crear una Reservation
        reservation = new Reservation();
        reservation.setUser(owner);
        reservation.setPlace(place);
        reservation.setDate(LocalDateTime.now().plusDays(1));
        reservation.setNumberOfPeople(4);

        testEntityManager.persist(reservation);

        // Crear una Promotion
        promotion = new Promotion();
        promotion.setDescription("10% discount for lunch meals");
        promotion.setDiscount(10.0);
        promotion.setStartDate(LocalDateTime.now());
        promotion.setEndDate(LocalDateTime.now().plusMonths(1));
        promotion.setPlace(place);

        testEntityManager.persist(promotion);

        testEntityManager.flush();
    }

    @Test
    void testSavePlace() {
        // Crear un nuevo User como Owner
        User newOwner = new User();
        newOwner.setFullName("Jane Doe");
        newOwner.setEmail("jane@example.com");
        newOwner.setPassword("password456");
        newOwner.setRole(Role.OWNER);
        newOwner.setCreatedAt(LocalDateTime.now());

        testEntityManager.persist(newOwner);

        // Crear un nuevo Place con el nuevo Owner
        Place newPlace = new Place();
        newPlace.setName("Cafe Delights");
        newPlace.setAddress("456 Another Street");
        newPlace.setDescription("A cozy cafe with excellent pastries.");
        newPlace.setCategory(PlaceCategory.CAFETERIA);
        newPlace.setOpeningHours("08:00 - 18:00");
        newPlace.setLatitude(40.7308);
        newPlace.setLongitude(-73.9975);
        newPlace.setOwner(newOwner);

        Place savedPlace = placeRepository.save(newPlace);

        assertNotNull(savedPlace.getId());
        assertEquals("Cafe Delights", savedPlace.getName());
        assertEquals(newOwner.getId(), savedPlace.getOwner().getId());
    }

    @Test
    void testFindById() {
        Place foundPlace = placeRepository.findById(place.getId()).orElse(null);

        assertNotNull(foundPlace);
        assertEquals(place.getName(), foundPlace.getName());
        assertEquals(place.getCategory(), foundPlace.getCategory());
        assertEquals(owner.getId(), foundPlace.getOwner().getId());
    }

    @Test
    void testFindAll() {
        List<Place> places = placeRepository.findAll();
        assertEquals(1, places.size());
    }

    @Test
    void testUpdatePlace() {
        place.setName("Updated Restaurant Name");
        place.setDescription("Updated description for the restaurant.");
        placeRepository.save(place);

        Place updatedPlace = placeRepository.findById(place.getId()).orElse(null);

        assertNotNull(updatedPlace);
        assertEquals("Updated Restaurant Name", updatedPlace.getName());
        assertEquals("Updated description for the restaurant.", updatedPlace.getDescription());
    }

    @Test
    void testDeletePlace() {
        placeRepository.delete(place);
        Place foundPlace = placeRepository.findById(place.getId()).orElse(null);

        assertNull(foundPlace);
    }

    @Test
    void testPlaceWithRelations() {
        testEntityManager.refresh(place);
        Place foundPlace = placeRepository.findById(place.getId()).orElse(null);

        assertNotNull(foundPlace);
        assertEquals(1, foundPlace.getReviews().size());     // Debe haber 1 Review
        assertEquals(1, foundPlace.getReservations().size()); // Debe haber 1 Reservation
        assertEquals(1, foundPlace.getPromotions().size());  // Debe haber 1 Promotion
    }

}
