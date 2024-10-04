package dbp.exploreconnet.coordinate.domain;

import dbp.exploreconnet.place.domain.Place;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Coordinate {
    public Coordinate(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @OneToMany(mappedBy = "coordinate", orphanRemoval = true)
    private List<Place> places = new ArrayList<>();

    public Coordinate() {}
}
