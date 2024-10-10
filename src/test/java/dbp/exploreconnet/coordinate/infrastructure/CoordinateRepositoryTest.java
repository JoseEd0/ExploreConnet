package dbp.exploreconnet.coordinate.infrastructure;

import dbp.exploreconnet.AbstractContainerBaseTest;
import dbp.exploreconnet.coordinate.domain.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CoordinateRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private CoordinateRepository coordinateRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Coordinate coordinate;

    @BeforeEach
    void setUp() {
        // Crear un Coordinate
        coordinate = new Coordinate();
        coordinate.setLatitude(40.7128);
        coordinate.setLongitude(-74.0060);
        testEntityManager.persist(coordinate);
        testEntityManager.flush();
    }

    @Test
    void testSaveCoordinate() {
        // Crear y guardar un nuevo Coordinate
        Coordinate newCoordinate = new Coordinate();
        newCoordinate.setLatitude(34.0522);
        newCoordinate.setLongitude(-118.2437);
        Coordinate savedCoordinate = coordinateRepository.save(newCoordinate);

        assertNotNull(savedCoordinate.getId());
        assertEquals(34.0522, savedCoordinate.getLatitude());
        assertEquals(-118.2437, savedCoordinate.getLongitude());
    }

    @Test
    void testFindById() {
        Optional<Coordinate> foundCoordinate = coordinateRepository.findById(coordinate.getId());
        assertTrue(foundCoordinate.isPresent());
        assertEquals(coordinate.getLatitude(), foundCoordinate.get().getLatitude());
        assertEquals(coordinate.getLongitude(), foundCoordinate.get().getLongitude());
    }

    @Test
    void testFindAll() {
        // Agregar otro Coordinate para asegurarnos de que findAll funcione con múltiples registros
        Coordinate anotherCoordinate = new Coordinate();
        anotherCoordinate.setLatitude(51.5074);
        anotherCoordinate.setLongitude(-0.1278);
        testEntityManager.persist(anotherCoordinate);
        testEntityManager.flush();

        var coordinates = coordinateRepository.findAll();
        assertEquals(2, coordinates.size());
    }

    @Test
    void testFindByLatitudeAndLongitude() {
        Optional<Coordinate> foundCoordinate = coordinateRepository.findByLatitudeAndLongitude(40.7128, -74.0060);
        assertTrue(foundCoordinate.isPresent());
        assertEquals(coordinate.getId(), foundCoordinate.get().getId());
    }

    @Test
    void testUpdateCoordinate() {
        coordinate.setLatitude(41.8781);
        coordinate.setLongitude(-87.6298);
        coordinateRepository.save(coordinate);

        Coordinate updatedCoordinate = coordinateRepository.findById(coordinate.getId()).orElse(null);
        assertNotNull(updatedCoordinate);
        assertEquals(41.8781, updatedCoordinate.getLatitude());
        assertEquals(-87.6298, updatedCoordinate.getLongitude());
    }

    @Test
    void testDeleteCoordinate() {
        coordinateRepository.delete(coordinate);
        Optional<Coordinate> foundCoordinate = coordinateRepository.findById(coordinate.getId());
        assertFalse(foundCoordinate.isPresent());
    }
}
