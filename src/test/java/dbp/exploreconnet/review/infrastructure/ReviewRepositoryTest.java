package dbp.exploreconnet.review.infrastructure;
import dbp.exploreconnet.AbstractContainerBaseTest;
import dbp.exploreconnet.coordinate.domain.Coordinate;
import dbp.exploreconnet.review.domain.Review;
import dbp.exploreconnet.place.domain.Place;
import dbp.exploreconnet.user.domain.User;
import dbp.exploreconnet.place.domain.PlaceCategory;
import dbp.exploreconnet.user.domain.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReviewRepositoryTest extends AbstractContainerBaseTest {
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private TestEntityManager testEntityManager;
    private User user;
    private Place place;
    private Review review1;
    private Review review2;
    @BeforeEach
    void setUp() {
        // Crear un User
        user = new User();
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("password123");
        user.setRole(Role.USER);
        user.setCreatedAt(java.time.LocalDateTime.now());
        testEntityManager.persist(user);

        Coordinate coordinate = new Coordinate();
        coordinate.setLatitude(40.7308);
        coordinate.setLongitude(-73.9975);
        testEntityManager.persist(coordinate);

        // Crear un Place
        place = new Place();
        place.setName("Cafe Central");
        place.setAddress("123 Main St");
        place.setDescription("A cozy coffee shop.");
        place.setCategory(PlaceCategory.CAFETERIA);
        place.setOpeningHours("08:00 - 20:00");
        place.setCoordinate(coordinate);
        place.setOwner(user); // Asignar el owner al Place
        testEntityManager.persist(place);

        // Crear dos Reviews
        review1 = new Review();
        review1.setUser(user);
        review1.setPlace(place);
        review1.setComment("Great place!");
        review1.setRating(5);

        review2 = new Review();
        review2.setUser(user);
        review2.setPlace(place);
        review2.setComment("Not bad.");
        review2.setRating(3);

        testEntityManager.persist(review1);
        testEntityManager.persist(review2);
        testEntityManager.flush();
    }
    @Test
    void testSaveReview() {
        Review newReview = new Review();
        newReview.setUser(user);
        newReview.setPlace(place);
        newReview.setComment("Excellent service.");
        newReview.setRating(4);
        Review savedReview = reviewRepository.save(newReview);
        assertNotNull(savedReview.getId());
        assertEquals(4, savedReview.getRating());
        assertEquals("Excellent service.", savedReview.getComment());
        assertEquals(user.getId(), savedReview.getUser().getId());
        assertEquals(place.getId(), savedReview.getPlace().getId());
    }
    @Test
    void testFindById() {
        Review foundReview = reviewRepository.findById(review1.getId()).orElse(null);
        assertNotNull(foundReview);
        assertEquals(review1.getId(), foundReview.getId());
        assertEquals("Great place!", foundReview.getComment());
        assertEquals(5, foundReview.getRating());
        assertEquals(user.getId(), foundReview.getUser().getId());
    }
    @Test
    void testFindAll() {
        List<Review> reviews = reviewRepository.findAll();
        assertEquals(2, reviews.size());
    }
    @Test
    void testUpdateReview() {
        review1.setComment("Amazing atmosphere!");
        review1.setRating(4);
        reviewRepository.save(review1);
        Review updatedReview = reviewRepository.findById(review1.getId()).orElse(null);
        assertNotNull(updatedReview);
        assertEquals("Amazing atmosphere!", updatedReview.getComment());
        assertEquals(4, updatedReview.getRating());
    }
    @Test
    void testDeleteReview() {
        reviewRepository.delete(review1);
        Review foundReview = reviewRepository.findById(review1.getId()).orElse(null);
        assertNull(foundReview);
    }
}