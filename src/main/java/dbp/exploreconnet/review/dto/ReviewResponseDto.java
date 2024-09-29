package dbp.exploreconnet.review.dto;

import lombok.Data;

@Data
public class ReviewResponseDto {
    private Long id;
    private String comment;
    private Integer rating;
}
