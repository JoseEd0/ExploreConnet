package dbp.exploreconnet.user.dto;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserProfilePhotoUpdateDto {
    private MultipartFile profilePhoto;
}
