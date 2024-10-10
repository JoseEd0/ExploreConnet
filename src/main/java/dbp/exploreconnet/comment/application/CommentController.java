package dbp.exploreconnet.comment.application;

import dbp.exploreconnet.comment.domain.CommentService;
import dbp.exploreconnet.comment.dto.CommentUpdateDto;
import dbp.exploreconnet.comment.dto.CommentRequestDto;
import dbp.exploreconnet.comment.dto.CommentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER','GUEST')")
    @GetMapping("/post/{postId}")
    public ResponseEntity<Page<CommentResponseDto>> getCommentsByPostId(@PathVariable Long postId, @RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId, page, size));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','USER', 'OWNER')")
    @PostMapping
    public ResponseEntity<Void> createComment(@RequestBody CommentRequestDto commentRequestDto) {
        commentService.createComment(commentRequestDto);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(@PathVariable Long commentId, @RequestBody CommentUpdateDto commentUpdateDto) {
        return ResponseEntity.ok(commentService.updateComment(commentId, commentUpdateDto));
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @PatchMapping("/like/{id}")
    public ResponseEntity<Void> likeComment(@PathVariable Long id) {
        commentService.likeComment(id);
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
