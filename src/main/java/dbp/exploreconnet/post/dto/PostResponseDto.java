package dbp.exploreconnet.post.dto;

import dbp.exploreconnet.place.dto.PlaceResponseDto;
import dbp.exploreconnet.place.dto.PlaceResponseForPostDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class PostResponseDto {
    private Long id;
    private String owner;
    private Long ownerId;
    private String profileImage;
    private LocalDateTime CreatedAt;
    private PlaceResponseForPostDto place;
    private Integer likes;
    private String description;
    private String imageUrl;
    private String videoUrl;
    private Set<Long> likedByUserIds;
}
