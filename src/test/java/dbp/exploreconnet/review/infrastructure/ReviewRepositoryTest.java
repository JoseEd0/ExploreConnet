package dbp.exploreconnet.review.infrastructure;

import dbp.exploreconnet.AbstractContainerBaseTest;
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


}
