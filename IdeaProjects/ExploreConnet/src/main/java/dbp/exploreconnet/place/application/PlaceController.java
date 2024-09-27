package dbp.exploreconnet.place.application;

import dbp.exploreconnet.place.domain.Place;
import dbp.exploreconnet.place.domain.PlaceService;
import dbp.exploreconnet.place.dto.PlaceDTO;
import dbp.exploreconnet.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/places")
public class PlaceController {

    @Autowired
    private PlaceService placeService;

    @PostMapping
    public ResponseEntity<Place> createPlace(@RequestBody Place place) {
        return ResponseEntity.ok(placeService.createPlace(place));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceDTO> getPlaceDetails(@PathVariable Long id, User user) {
        Place place = placeService.getPlaceById(id);
        if (place.getOwner().equals(user)) {
            return ResponseEntity.ok(placeService.getPlaceWithReservations(place));
        } else {
            return ResponseEntity.ok(placeService.getPlaceWithoutReservations(place));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Place> updatePlace(@PathVariable Long id, @RequestBody Place place) {
        return ResponseEntity.ok(placeService.updatePlace(id, place));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlace(@PathVariable Long id) {
        placeService.deletePlace(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Place>> getAllPlaces() {
        return ResponseEntity.ok(placeService.getAllPlaces());
    }

}
