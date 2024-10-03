package dbp.exploreconnet.promotion.infrastructure;

import dbp.exploreconnet.AbstractContainerBaseTest;
import dbp.exploreconnet.promotion.domain.Promotion;
import dbp.exploreconnet.place.domain.Place;
import dbp.exploreconnet.place.domain.PlaceCategory;
import dbp.exploreconnet.user.domain.User;
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
public class PromotionRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User owner;
    private Place place;
    private Promotion promotion1;
    private Promotion promotion2;

    @BeforeEach
    void setUp() {
        // Crear un User como Owner del Place
        owner = new User();
        owner.setFullName("John Doe");
        owner.setEmail("john@example.com");
        owner.setPassword("password123");
        owner.setRole(Role.OWNER);
        owner.setCreatedAt(LocalDateTime.now());

        testEntityManager.persist(owner);

        // Crear un Place
        place = new Place();
        place.setName("Gourmet Restaurant");
        place.setAddress("123 Main Street");
        place.setDescription("A high-class restaurant.");
        place.setCategory(PlaceCategory.RESTAURANT);
        place.setOpeningHours("08:00 - 22:00");
        place.setLatitude(40.7128);
        place.setLongitude(-74.0060);
        place.setOwner(owner);

        testEntityManager.persist(place);

        // Crear dos promociones
        promotion1 = new Promotion();
        promotion1.setDescription("20% discount on dinner");
        promotion1.setDiscount(20.0);
        promotion1.setStartDate(LocalDateTime.now());
        promotion1.setEndDate(LocalDateTime.now().plusMonths(1));
        promotion1.setPlace(place);

        promotion2 = new Promotion();
        promotion2.setDescription("10% discount on breakfast");
        promotion2.setDiscount(10.0);
        promotion2.setStartDate(LocalDateTime.now().plusDays(1));
        promotion2.setEndDate(LocalDateTime.now().plusMonths(2));
        promotion2.setPlace(place);

        testEntityManager.persist(promotion1);
        testEntityManager.persist(promotion2);

        testEntityManager.flush();
    }

    @Test
    void testSavePromotion() {
        Promotion newPromotion = new Promotion();
        newPromotion.setDescription("15% discount on lunch");
        newPromotion.setDiscount(15.0);
        newPromotion.setStartDate(LocalDateTime.now());
        newPromotion.setEndDate(LocalDateTime.now().plusMonths(3));
        newPromotion.setPlace(place);

        Promotion savedPromotion = promotionRepository.save(newPromotion);

        assertNotNull(savedPromotion.getId());
        assertEquals("15% discount on lunch", savedPromotion.getDescription());
        assertEquals(15.0, savedPromotion.getDiscount());
        assertEquals(place.getId(), savedPromotion.getPlace().getId());
    }

    @Test
    void testFindById() {
        Promotion foundPromotion = promotionRepository.findById(promotion1.getId()).orElse(null);

        assertNotNull(foundPromotion);
        assertEquals(promotion1.getDescription(), foundPromotion.getDescription());
        assertEquals(promotion1.getDiscount(), foundPromotion.getDiscount());
        assertEquals(place.getId(), foundPromotion.getPlace().getId());
    }

    @Test
    void testFindAll() {
        List<Promotion> promotions = promotionRepository.findAll();
        assertEquals(2, promotions.size());
    }

    @Test
    void testUpdatePromotion() {
        promotion1.setDescription("Updated promotion for dinner");
        promotion1.setDiscount(25.0);
        promotionRepository.save(promotion1);

        Promotion updatedPromotion = promotionRepository.findById(promotion1.getId()).orElse(null);

        assertNotNull(updatedPromotion);
        assertEquals("Updated promotion for dinner", updatedPromotion.getDescription());
        assertEquals(25.0, updatedPromotion.getDiscount());
    }

    @Test
    void testDeletePromotion() {
        promotionRepository.delete(promotion1);
        Promotion foundPromotion = promotionRepository.findById(promotion1.getId()).orElse(null);

        assertNull(foundPromotion);
    }

    @Test
    void testFindByPlace() {
        List<Promotion> promotionsByPlace = promotionRepository.findByPlace(place);

        assertEquals(2, promotionsByPlace.size());
        assertTrue(promotionsByPlace.stream().anyMatch(p -> p.getDescription().equals("20% discount on dinner")));
        assertTrue(promotionsByPlace.stream().anyMatch(p -> p.getDescription().equals("10% discount on breakfast")));
    }
}
