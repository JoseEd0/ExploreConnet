package dbp.exploreconnet.user.dto;

import dbp.exploreconnet.user.domain.User;
import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private String role;
}
