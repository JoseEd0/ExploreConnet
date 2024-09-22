package dbp.exploreconnet.review.infrastructure;

import dbp.exploreconnet.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review,Long> {
}
