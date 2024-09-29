package dbp.exploreconnet.place.dto;

import dbp.exploreconnet.place.domain.PlaceCategory;
import lombok.Data;

@Data
public class PlaceRequestDto {
    private String name;
    private String address;
    private String image;
    private String description;
    private PlaceCategory category;
    private String openingHours;
    private Double latitude;
    private Double longitude;
}
