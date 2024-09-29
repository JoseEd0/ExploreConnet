package dbp.exploreconnet.auth.dto;

import lombok.Data;

@Data
public class UserPasswordVerificationRequestDto {
    private Long userId;
    private String password;
}