package dbp.exploreconnet.comment.dto;


import lombok.Data;

@Data
public class CommentResponseDto {
    private Long id;
    private String content;
    private Long userId;
    private String name;
    private Long postId;
}
