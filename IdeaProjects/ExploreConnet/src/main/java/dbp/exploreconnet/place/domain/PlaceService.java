package dbp.exploreconnet.place.domain;


import dbp.exploreconnet.place.dto.PlaceDTO;
import dbp.exploreconnet.place.dto.PlaceWithReservationsDTO;
import dbp.exploreconnet.place.infrastructure.PlaceRepository;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlaceService {

    @Autowired
    private PlaceRepository placeRepository;

    public Place createPlace(Place place){
        return placeRepository.save(place);
    }

    public Place getPlaceById(Long id) {
        return placeRepository.findById(id).orElseThrow(() -> new RuntimeException("Place not found"));
    }

    public Place updatePlace(Long id, Place place) {
        Place existingPlace = getPlaceById(id);
        existingPlace.setName(place.getName());
        existingPlace.setAddress(place.getAddress());
        existingPlace.setDescription(place.getDescription());
        return placeRepository.save(existingPlace);
    }

    public void deletePlace(Long id) {
        placeRepository.deleteById(id);
    }

    public List<Place> getAllPlaces() {
        return placeRepository.findAll();
    }
    public PlaceWithReservationsDTO getPlaceWithReservations(Place place) {
        return new PlaceWithReservationsDTO(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getReservations()
        );
    }

    // Método que devuelve el Place sin las reservas
    public PlaceDTO getPlaceWithoutReservations(Place place) {
        return new PlaceDTO(
                place.getId(),
                place.getName(),
                place.getAddress()
        );
    }
}
