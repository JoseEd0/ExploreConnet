package dbp.exploreconnet.place.infrastructure;

import dbp.exploreconnet.place.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place,Long> {
    Optional<Place> findByName(String name);
    List<Place> findByOwnerEmail(String email);
}
