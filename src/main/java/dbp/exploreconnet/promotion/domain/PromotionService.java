package dbp.exploreconnet.promotion.domain;


import dbp.exploreconnet.auth.utils.AuthorizationUtils;
import dbp.exploreconnet.exceptions.ResourceNotFoundException;
import dbp.exploreconnet.exceptions.UnauthorizedOperationException;
import dbp.exploreconnet.mediaStorage.domain.MediaStorageService;
import dbp.exploreconnet.place.domain.Place;
import dbp.exploreconnet.place.infrastructure.PlaceRepository;
import dbp.exploreconnet.promotion.dto.NewPromotionDto;
import dbp.exploreconnet.promotion.dto.PromotionResponseDto;
import dbp.exploreconnet.promotion.infrastructure.PromotionRepository;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private AuthorizationUtils authorizationUtils;

    @Autowired
    private MediaStorageService mediaStorageService;


    public PromotionResponseDto createPromotion(NewPromotionDto newPromotionDto) throws FileUploadException {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Place place = placeRepository.findById(newPromotionDto.getPlaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Place not found"));

        // Verificar si el usuario autenticado es el propietario del Place
        if (!place.getOwner().getEmail().equals(userEmail)) {
            throw new UnauthorizedOperationException("You are not allowed to create promotions for this place");
        }

        Promotion promotion = new Promotion();
        promotion.setDescription(newPromotionDto.getDescription());
        promotion.setDiscount(newPromotionDto.getDiscount());
        promotion.setStartDate(newPromotionDto.getStartDate());
        promotion.setEndDate(newPromotionDto.getEndDate());
        promotion.setPlace(place);

        if (newPromotionDto.getImage() != null && !newPromotionDto.getImage().isEmpty()) {
            String imageUrl = mediaStorageService.uploadFile(newPromotionDto.getImage());
            promotion.setImageUrl(imageUrl);
        }

        Promotion savedPromotion = promotionRepository.save(promotion);

        return mapToResponseDto(savedPromotion);
    }



    public PromotionResponseDto getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        return mapToResponseDto(promotion);
    }

    public PromotionResponseDto updatePromotion(Long id, NewPromotionDto newPromotionDto) throws FileUploadException {
        Promotion existingPromotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));

        existingPromotion.setDescription(newPromotionDto.getDescription());
        existingPromotion.setDiscount(newPromotionDto.getDiscount());
        existingPromotion.setStartDate(newPromotionDto.getStartDate());
        existingPromotion.setEndDate(newPromotionDto.getEndDate());

        if (newPromotionDto.getImage() != null && !newPromotionDto.getImage().isEmpty()) {
            String imageUrl = mediaStorageService.uploadFile(newPromotionDto.getImage());
            existingPromotion.setImageUrl(imageUrl);
        }

        Promotion updatedPromotion = promotionRepository.save(existingPromotion);

        return mapToResponseDto(updatedPromotion);
    }


    public void deletePromotion(Long id) {
        promotionRepository.deleteById(id);
    }

    public List<PromotionResponseDto> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<PromotionResponseDto> getPromotionsByPlace(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResourceNotFoundException("Place not found"));

        return promotionRepository.findByPlace(place).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<PromotionResponseDto> getPromotionsByMyPlaces() {
        String currentUserEmail = authorizationUtils.getCurrentUserEmail();
        List<Place> places = placeRepository.findByOwnerEmail(currentUserEmail);
        return places.stream()
                .flatMap(place -> promotionRepository.findByPlace(place).stream())
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }


    private PromotionResponseDto mapToResponseDto(Promotion promotion) {
        PromotionResponseDto responseDto = new PromotionResponseDto();
        responseDto.setId(promotion.getId());
        responseDto.setDescription(promotion.getDescription());
        responseDto.setDiscount(promotion.getDiscount());
        responseDto.setStartDate(promotion.getStartDate());
        responseDto.setEndDate(promotion.getEndDate());
        responseDto.setPlaceName(promotion.getPlace().getName());
        responseDto.setImageUrl(promotion.getImageUrl());

        return responseDto;
    }
}
