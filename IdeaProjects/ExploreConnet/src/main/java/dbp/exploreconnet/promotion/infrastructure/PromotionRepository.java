package dbp.exploreconnet.promotion.infrastructure;

import dbp.exploreconnet.promotion.domain.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion,Long> {
}
