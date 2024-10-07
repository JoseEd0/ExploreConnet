package dbp.exploreconnet.coordinate.infrastructure;

import dbp.exploreconnet.coordinate.domain.Coordinate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoordinateRepository extends JpaRepository<Coordinate, Long> {
    Optional<Coordinate> findByLatitudeAndLongitude(Double latitude, Double longitude);
}