package dbp.exploreconnet.place.domain;


import dbp.exploreconnet.coordinate.domain.Coordinate;
import dbp.exploreconnet.coordinate.dto.CoordinateDto;
import dbp.exploreconnet.coordinate.infrastructure.CoordinateRepository;
import dbp.exploreconnet.exceptions.ResourceNotFoundException;
import dbp.exploreconnet.mediaStorage.domain.MediaStorageService;
import dbp.exploreconnet.place.dto.PlaceRequestDto;
import dbp.exploreconnet.place.dto.PlaceResponseDto;
import dbp.exploreconnet.place.infrastructure.PlaceRepository;
import dbp.exploreconnet.promotion.dto.PromotionResponseDto;
import dbp.exploreconnet.review.dto.NewReviewDto;
import dbp.exploreconnet.review.dto.ReviewResponseDto;
import dbp.exploreconnet.user.domain.User;
import dbp.exploreconnet.user.infrastructure.UserRepository;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
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

    @Autowired
    private CoordinateRepository coordinateRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MediaStorageService mediaStorageService;


    public PlaceResponseDto createPlace(PlaceRequestDto placeRequestDto, String ownerEmail) throws FileUploadException {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        Place place = new Place();
        place.setName(placeRequestDto.getName());
        place.setAddress(placeRequestDto.getAddress());

        if (placeRequestDto.getImage() != null && !placeRequestDto.getImage().isEmpty()) {
            String imageUrl = mediaStorageService.uploadFile(placeRequestDto.getImage());
            place.setImageUrl(imageUrl);
        }

        place.setDescription(placeRequestDto.getDescription());
        place.setCategory(placeRequestDto.getCategory());
        place.setOpeningHours(placeRequestDto.getOpeningHours());
        place.setOwner(owner);

        Coordinate coordinate = new Coordinate();
        coordinate.setLatitude(placeRequestDto.getCoordinate().getLatitude());
        coordinate.setLongitude(placeRequestDto.getCoordinate().getLongitude());
        coordinate = coordinateRepository.save(coordinate);
        place.setCoordinate(coordinate);

        Place savedPlace = placeRepository.save(place);

        return mapToResponseDto(savedPlace);
    }



    public PlaceResponseDto getPlaceById(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Place not found"));
        return mapToResponseDto(place);
    }

    public PlaceResponseDto updatePlace(Long id, PlaceRequestDto placeRequestDto) throws FileUploadException {
        Place existingPlace = placeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Place not found"));

        existingPlace.setName(placeRequestDto.getName());
        existingPlace.setAddress(placeRequestDto.getAddress());

        if (placeRequestDto.getImage() != null && !placeRequestDto.getImage().isEmpty()) {
            String imageUrl = mediaStorageService.uploadFile(placeRequestDto.getImage());
            existingPlace.setImageUrl(imageUrl);
        }

        existingPlace.setDescription(placeRequestDto.getDescription());
        existingPlace.setCategory(placeRequestDto.getCategory());
        existingPlace.setOpeningHours(placeRequestDto.getOpeningHours());

        Coordinate coordinate = existingPlace.getCoordinate();
        coordinate.setLatitude(placeRequestDto.getCoordinate().getLatitude());
        coordinate.setLongitude(placeRequestDto.getCoordinate().getLongitude());
        existingPlace.setCoordinate(coordinate);

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

    public PlaceResponseDto getPlaceByName(String name) {
        Place place = placeRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Place not found"));
        return mapToResponseDto(place);
    }

    public List<PlaceResponseDto> getMyPlaces(String email) {
        List<Place> places = placeRepository.findByOwnerEmail(email);
        return places.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }


    private PlaceResponseDto mapToResponseDto(Place place) {
        PlaceResponseDto responseDto = new PlaceResponseDto();
        responseDto.setId(place.getId());
        responseDto.setName(place.getName());
        responseDto.setAddress(place.getAddress());
        responseDto.setImageUrl(place.getImageUrl());
        responseDto.setDescription(place.getDescription());
        responseDto.setCategory(place.getCategory());
        responseDto.setOpeningHours(place.getOpeningHours());

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

        CoordinateDto coordinateDto = new CoordinateDto();
        coordinateDto.setLatitude(place.getCoordinate().getLatitude());
        coordinateDto.setLongitude(place.getCoordinate().getLongitude());
        responseDto.setCoordinate(coordinateDto);

        return responseDto;
    }
}
