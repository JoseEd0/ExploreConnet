package dbp.exploreconnet.place.dto;

import dbp.exploreconnet.place.domain.PlaceCategory;
import lombok.Data;

import java.util.List;

@Data
public class PlaceResponseForPostDto {
    private Long id;
    private String name;
    private String image;

    private String description;
    private PlaceCategory category;
    private String openingHours;
}
