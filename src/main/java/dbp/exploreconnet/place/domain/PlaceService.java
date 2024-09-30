package dbp.exploreconnet.place.domain;


import dbp.exploreconnet.exceptions.ResourceNotFoundException;
import dbp.exploreconnet.place.dto.PlaceRequestDto;
import dbp.exploreconnet.place.dto.PlaceResponseDto;
import dbp.exploreconnet.place.infrastructure.PlaceRepository;
import dbp.exploreconnet.promotion.dto.PromotionResponseDto;
import dbp.exploreconnet.review.dto.NewReviewDto;
import dbp.exploreconnet.review.dto.ReviewResponseDto;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaceService {

    @Autowired
    private PlaceRepository placeRepository;

    public PlaceResponseDto createPlace(PlaceRequestDto placeRequestDto) {
        Place place = new Place();
        place.setName(placeRequestDto.getName());
        place.setAddress(placeRequestDto.getAddress());
        place.setImage(placeRequestDto.getImage());
        place.setDescription(placeRequestDto.getDescription());
        place.setCategory(placeRequestDto.getCategory());
        place.setOpeningHours(placeRequestDto.getOpeningHours());
        place.setLatitude(placeRequestDto.getLatitude());
        place.setLongitude(placeRequestDto.getLongitude());

        Place savedPlace = placeRepository.save(place);
        return mapToResponseDto(savedPlace);
    }

    public PlaceResponseDto getPlaceById(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Place not found"));
        return mapToResponseDto(place);
    }

    public PlaceResponseDto updatePlace(Long id, PlaceRequestDto placeRequestDto) {
        Place existingPlace = placeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Place not found"));

        existingPlace.setName(placeRequestDto.getName());
        existingPlace.setAddress(placeRequestDto.getAddress());
        existingPlace.setImage(placeRequestDto.getImage());
        existingPlace.setDescription(placeRequestDto.getDescription());
        existingPlace.setCategory(placeRequestDto.getCategory());
        existingPlace.setOpeningHours(placeRequestDto.getOpeningHours());
        existingPlace.setLatitude(placeRequestDto.getLatitude());
        existingPlace.setLongitude(placeRequestDto.getLongitude());

        Place updatedPlace = placeRepository.save(existingPlace);
        return mapToResponseDto(updatedPlace);
    }

    public void deletePlace(Long id) {
        placeRepository.deleteById(id);
    }

    public List<PlaceResponseDto> getAllPlaces() {
        return placeRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private PlaceResponseDto mapToResponseDto(Place place) {
        PlaceResponseDto responseDto = new PlaceResponseDto();
        responseDto.setId(place.getId());
        responseDto.setName(place.getName());
        responseDto.setAddress(place.getAddress());
        responseDto.setImage(place.getImage());
        responseDto.setDescription(place.getDescription());
        responseDto.setCategory(place.getCategory());
        responseDto.setOpeningHours(place.getOpeningHours());

        // Asignar una lista vacía si las reviews son null
        List<ReviewResponseDto> reviews = place.getReviews() != null
                ? place.getReviews().stream()
                .map(review -> {
                    ReviewResponseDto reviewDto = new ReviewResponseDto();
                    reviewDto.setId(review.getId());
                    reviewDto.setComment(review.getComment());
                    reviewDto.setRating(review.getRating());
                    return reviewDto;
                })
                .collect(Collectors.toList())
                : Collections.emptyList();

        responseDto.setReviews(reviews);

        // Asignar una lista vacía si las promotions son null
        List<PromotionResponseDto> promotions = place.getPromotions() != null
                ? place.getPromotions().stream()
                .map(promotion -> {
                    PromotionResponseDto promotionDto = new PromotionResponseDto();
                    promotionDto.setId(promotion.getId());
                    promotionDto.setDescription(promotion.getDescription());
                    promotionDto.setDiscount(promotion.getDiscount());
                    promotionDto.setStartDate(promotion.getStartDate());
                    promotionDto.setEndDate(promotion.getEndDate());
                    return promotionDto;
                })
                .collect(Collectors.toList())
                : Collections.emptyList();

        responseDto.setPromotions(promotions);

        return responseDto;
    }
}
