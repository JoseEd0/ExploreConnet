package dbp.exploreconnet.promotion.infrastructure;

import dbp.exploreconnet.AbstractContainerBaseTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;



@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PromotionRepositoryTest extends AbstractContainerBaseTest {


}
