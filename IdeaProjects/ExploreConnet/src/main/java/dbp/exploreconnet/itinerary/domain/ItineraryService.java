package dbp.exploreconnet.itinerary.domain;

import dbp.exploreconnet.itinerary.infrastructure.ItineraryRepository;
import dbp.exploreconnet.user.domain.User;
import dbp.exploreconnet.user.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItineraryService {

    @Autowired
    private ItineraryRepository itineraryRepository;

    @Autowired
    private UserRepository userRepository;

    public Itinerary createItinerary(Itinerary itinerary) {
        if (itinerary.getUser() == null || itinerary.getUser().getId() == null) {
            throw new IllegalArgumentException("User must be provided and cannot be null");
        }

        // Verificar si el usuario existe en la base de datos
        User user = userRepository.findById(itinerary.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        itinerary.setUser(user); // Establecer el usuario antes de guardar
        return itineraryRepository.save(itinerary);
    }

    public Itinerary getItineraryById(Long id) {
        return itineraryRepository.findById(id).orElseThrow(() -> new RuntimeException("Itinerary not found"));
    }

    public Itinerary updateItinerary(Long id, Itinerary itinerary) {
        Itinerary existingItinerary = getItineraryById(id);
        existingItinerary.setName(itinerary.getName());
        existingItinerary.setReservations(itinerary.getReservations());
        return itineraryRepository.save(existingItinerary);
    }

    public void deleteItinerary(Long id) {
        itineraryRepository.deleteById(id);
    }

    public List<Itinerary> getAllItineraries() {
        return itineraryRepository.findAll();
    }
}
