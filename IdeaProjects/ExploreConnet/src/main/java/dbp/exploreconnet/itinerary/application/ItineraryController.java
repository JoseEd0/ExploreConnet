package dbp.exploreconnet.itinerary.application;

import dbp.exploreconnet.itinerary.domain.Itinerary;
import dbp.exploreconnet.itinerary.domain.ItineraryService;
import dbp.exploreconnet.itinerary.dto.ItineraryRequestDto;
import dbp.exploreconnet.itinerary.dto.ItineraryResponseDto;
import dbp.exploreconnet.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/itineraries")
public class ItineraryController {

    @Autowired
    private ItineraryService itineraryService;

    @PostMapping
    public ResponseEntity<ItineraryResponseDto> createItinerary(@RequestBody ItineraryRequestDto itineraryRequestDto) {
        Itinerary itinerary = new Itinerary();
        itinerary.setName(itineraryRequestDto.getName());
        itinerary.setUser(new User());
        itinerary.setReservations(itineraryRequestDto.getReservations());
        Itinerary savedItinerary = itineraryService.createItinerary(itinerary);
        return ResponseEntity.ok(convertToResponseDto(savedItinerary));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItineraryResponseDto> getItineraryById(@PathVariable Long id) {
        Itinerary itinerary = itineraryService.getItineraryById(id);
        return ResponseEntity.ok(convertToResponseDto(itinerary));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItineraryResponseDto> updateItinerary(@PathVariable Long id, @RequestBody ItineraryRequestDto itineraryRequestDto) {
        Itinerary itinerary = new Itinerary();
        itinerary.setName(itineraryRequestDto.getName());
        itinerary.setUser(new User());
        itinerary.setReservations(itineraryRequestDto.getReservations());
        Itinerary updatedItinerary = itineraryService.updateItinerary(id, itinerary);
        return ResponseEntity.ok(convertToResponseDto(updatedItinerary));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItinerary(@PathVariable Long id) {
        itineraryService.deleteItinerary(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ItineraryResponseDto>> getAllItineraries() {
        List<Itinerary> itineraries = itineraryService.getAllItineraries();
        List<ItineraryResponseDto> responseDtos = itineraries.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    private ItineraryResponseDto convertToResponseDto(Itinerary itinerary) {
        ItineraryResponseDto responseDto = new ItineraryResponseDto();
        responseDto.setId(itinerary.getId());
        responseDto.setName(itinerary.getName());
        responseDto.setUserId(itinerary.getUser().getId());
        responseDto.setReservations(itinerary.getReservations());
        return responseDto;
    }
}