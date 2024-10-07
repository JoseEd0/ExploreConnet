package dbp.exploreconnet.coordinate.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CoordinateDto {
    @DecimalMin(value = "-90")
    @DecimalMax(value = "90")
    @NotNull
    private Double latitude;

    @DecimalMin(value = "-180")
    @DecimalMax(value = "180")
    @NotNull
    private Double longitude;
}
