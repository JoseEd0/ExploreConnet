package dbp.exploreconnet.place.dto;

import dbp.exploreconnet.coordinate.dto.CoordinateDto;
import dbp.exploreconnet.place.domain.PlaceCategory;
import dbp.exploreconnet.promotion.dto.PromotionResponseDto;
import dbp.exploreconnet.review.dto.NewReviewDto;
import dbp.exploreconnet.review.dto.ReviewResponseDto;
import lombok.Data;

import java.util.List;

@Data
public class PlaceResponseDto {
    private Long id;
    private String name;
    private String address;
    private String image;
    private Integer likes;
    private String description;
    private PlaceCategory category;
    private String openingHours;
    private CoordinateDto coordinate;
    private List<ReviewResponseDto> reviews;
    private List<PromotionResponseDto> promotions;
}
