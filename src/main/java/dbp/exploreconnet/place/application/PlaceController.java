package dbp.exploreconnet.place.application;

import dbp.exploreconnet.place.domain.Place;
import dbp.exploreconnet.place.domain.PlaceService;
import dbp.exploreconnet.place.dto.PlaceRequestDto;
import dbp.exploreconnet.place.dto.PlaceResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/places")
public class PlaceController {

    @Autowired
    private PlaceService placeService;

    @PreAuthorize("hasAuthority('OWNER')")
    @PostMapping
    public ResponseEntity<PlaceResponseDto> createPlace(@RequestBody PlaceRequestDto placeRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String ownerEmail = authentication.getName();
        return ResponseEntity.ok(placeService.createPlace(placeRequestDto, ownerEmail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceResponseDto> getPlaceById(@PathVariable Long id) {
        return ResponseEntity.ok(placeService.getPlaceById(id));
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @PutMapping("/{id}")
    public ResponseEntity<PlaceResponseDto> updatePlace(@PathVariable Long id, @RequestBody PlaceRequestDto placeRequestDto) {
        return ResponseEntity.ok(placeService.updatePlace(id, placeRequestDto));
    }


    @PreAuthorize("hasAuthority('OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlace(@PathVariable Long id) {
        placeService.deletePlace(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER','GUEST')")
    @GetMapping
    public ResponseEntity<List<PlaceResponseDto>> getAllPlaces() {
        return ResponseEntity.ok(placeService.getAllPlaces());
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER','GUEST')")
    @GetMapping("/name/{name}")
    public ResponseEntity<PlaceResponseDto> getPlaceByName(@PathVariable String name) {
        return ResponseEntity.ok(placeService.getPlaceByName(name));
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @GetMapping("/me")
    public ResponseEntity<List<PlaceResponseDto>> getMyPlaces() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return ResponseEntity.ok(placeService.getMyPlaces(email));
    }

}
