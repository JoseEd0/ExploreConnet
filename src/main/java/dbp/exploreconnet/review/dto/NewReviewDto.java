package dbp.exploreconnet.review.dto;

import lombok.Data;

@Data
public class NewReviewDto {
    private String comment;
    private Integer rating;
    private Long placeId;
}
