package dbp.exploreconnet.place.dto;

import dbp.exploreconnet.coordinate.dto.CoordinateDto;
import dbp.exploreconnet.place.domain.PlaceCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceRequestDto {
    private String name;
    private String address;
    private String image;
    private String description;
    private PlaceCategory category;
    private String openingHours;
    @NotNull(message = "Coordinate cannot be null")
    private CoordinateDto coordinate;
}
