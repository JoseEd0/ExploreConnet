package dbp.exploreconnet.auth.dto;


import dbp.exploreconnet.user.domain.Role;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class SigninDto {

    @NotNull
    private String email;

    @NotNull
    private String name;

    @NotNull
    private String password;

    @NotNull
    private Role role;

}
