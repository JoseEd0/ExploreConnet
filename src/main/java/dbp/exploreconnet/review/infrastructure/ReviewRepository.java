package dbp.exploreconnet.review.infrastructure;

import dbp.exploreconnet.place.domain.Place;
import dbp.exploreconnet.review.domain.Review;
import dbp.exploreconnet.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review,Long> {
    List<Review> findByUser(User user);
    List<Review> findByPlace(Place place);
}
