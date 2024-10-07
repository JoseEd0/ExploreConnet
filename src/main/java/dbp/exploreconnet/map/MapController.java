package dbp.exploreconnet.map;

import dbp.exploreconnet.place.domain.PlaceService;
import dbp.exploreconnet.place.dto.PlaceResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/map")
public class MapController {

    @Autowired
    private PlaceService placeService;

    @GetMapping("/places")
    public ResponseEntity<List<PlaceResponseDto>> getAllPlacesForMap() {
        return ResponseEntity.ok(placeService.getAllPlaces());
    }
}
