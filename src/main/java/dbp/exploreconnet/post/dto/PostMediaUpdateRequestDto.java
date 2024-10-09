package dbp.exploreconnet.post.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PostMediaUpdateRequestDto {
    private MultipartFile image;
    private MultipartFile video;
    private String description;
}