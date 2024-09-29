package dbp.exploreconnet.auth.dto;

import lombok.Data;

@Data
public class JwtAuthResponseDto {
    private String token;
    private String message;
    }
