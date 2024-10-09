package dbp.exploreconnet.post.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PostRequestDto {
    @NotNull
    private Long userId;

    @NotNull
    private Long placeId;

    @Size(max = 500)
    private String description;

    private MultipartFile image;
    private MultipartFile video;
}

