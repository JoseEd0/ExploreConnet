package dbp.exploreconnet.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class GoogleAuthResponseDto {
    private boolean isValid;
}