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

}
