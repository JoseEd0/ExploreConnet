package dbp.exploreconnet.comment.domain;

import lombok.Data;

@Data
public class CommentUpdateDto {
    private Long userId;
    private String content;
}



