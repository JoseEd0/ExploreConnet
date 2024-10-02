package dbp.exploreconnet.auth.dto;

import lombok.Data;

@Data
public class UserPasswordVerificationRequestDto {
    private String email;
    private String password;

}