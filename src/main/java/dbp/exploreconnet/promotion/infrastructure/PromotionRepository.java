package dbp.exploreconnet.promotion.infrastructure;

import dbp.exploreconnet.place.domain.Place;
import dbp.exploreconnet.promotion.domain.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion,Long> {
    List<Promotion> findByPlace(Place place);
}
