package dbp.exploreconnet.promotion.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class NewPromotionDto {
    private String description;
    private Double discount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long placeId;
    private MultipartFile image;
}
