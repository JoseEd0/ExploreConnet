package dbp.exploreconnet.promotion.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PromotionResponseDto {
    private Long id;
    private String description;
    private Double discount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String placeName;
    private String imageUrl;
}
