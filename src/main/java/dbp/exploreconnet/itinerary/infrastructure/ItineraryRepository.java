package dbp.exploreconnet.itinerary.infrastructure;

import dbp.exploreconnet.itinerary.domain.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItineraryRepository extends JpaRepository<Itinerary,Long> {
}
