package dbp.exploreconnet.place.dto;

import dbp.exploreconnet.coordinate.dto.CoordinateDto;
import dbp.exploreconnet.place.domain.PlaceCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PlaceRequestDto {
    private String name;
    private String address;
    private MultipartFile image;
    private String description;
    private PlaceCategory category;
    private String openingHours;
    @NotNull(message = "Coordinate cannot be null")
    private CoordinateDto coordinate;
    private String ownerEmail;
}
