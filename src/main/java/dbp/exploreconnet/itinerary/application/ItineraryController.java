package dbp.exploreconnet.itinerary.application;

import dbp.exploreconnet.itinerary.domain.ItineraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/itineraries")
public class ItineraryController {

    @Autowired
    private ItineraryService itineraryService;

    @PostMapping
    public ResponseEntity<ItineraryResponseDto> createItinerary(@RequestBody ItineraryRequestDto itineraryRequestDto) {
        ItineraryResponseDto savedItinerary = itineraryService.createItinerary(itineraryRequestDto);
        return ResponseEntity.ok(savedItinerary);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItineraryResponseDto> getItineraryById(@PathVariable Long id) {
        ItineraryResponseDto itineraryResponseDto = itineraryService.getItineraryById(id);
        return ResponseEntity.ok(itineraryResponseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItineraryResponseDto> updateItinerary(@PathVariable Long id, @RequestBody ItineraryRequestDto itineraryRequestDto) {
        ItineraryResponseDto updatedItinerary = itineraryService.updateItinerary(id, itineraryRequestDto);
        return ResponseEntity.ok(updatedItinerary);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItinerary(@PathVariable Long id) {
        itineraryService.deleteItinerary(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ItineraryResponseDto>> getAllItineraries() {
        List<ItineraryResponseDto> itineraries = itineraryService.getAllItineraries();
        return ResponseEntity.ok(itineraries);
    }
}