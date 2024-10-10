package dbp.exploreconnet.comment.domain;

import dbp.exploreconnet.auth.utils.AuthorizationUtils;
import dbp.exploreconnet.comment.dto.CommentRequestDto;
import dbp.exploreconnet.comment.dto.CommentResponseDto;
import dbp.exploreconnet.comment.dto.CommentUpdateDto;
import dbp.exploreconnet.comment.infrastructure.CommentRepository;
import dbp.exploreconnet.exceptions.ResourceNotFoundException;
import dbp.exploreconnet.post.domain.Post;
import dbp.exploreconnet.post.infrastructure.PostRepository;
import dbp.exploreconnet.user.domain.User;
import dbp.exploreconnet.user.infrastructure.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private final  PostRepository postRepository;

    private final UserRepository userRepository;
    private final AuthorizationUtils authorizationUtils;

    public void createComment(CommentRequestDto commentRequestDto) {
        Comment comment = new Comment();
        comment.setContent(commentRequestDto.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        Post post = postRepository.findById(commentRequestDto.getPostId()).
                orElseThrow(() -> new RuntimeException("Post not found"));
        comment.setPost(post);
        User user = userRepository.findById(commentRequestDto.getUserId()).
                orElseThrow(() -> new RuntimeException("User not found"));
        comment.setUser(user);
        commentRepository.save(comment);
    }

    public Page<CommentResponseDto> getCommentsByPostId(Long postId, int page, int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);

        return commentRepository.findByPostId(postId, pageable).map(comment -> {
            CommentResponseDto commentResponseDto = new CommentResponseDto();
            commentResponseDto.setId(comment.getId());
            commentResponseDto.setContent(comment.getContent());
            commentResponseDto.setPostId(comment.getPost().getId());
            commentResponseDto.setUserId(comment.getUser().getId());
            commentResponseDto.setName(comment.getUser().getFullName());
            return commentResponseDto;
        });
    }

    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentUpdateDto commentUpdateDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(commentUpdateDto.getUserId())) {
            throw new RuntimeException("Unauthorized operation: User does not own the comment");
        }

        comment.setContent(commentUpdateDto.getContent());
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);

        CommentResponseDto commentResponseDto = new CommentResponseDto();
        commentResponseDto.setId(comment.getId());
        commentResponseDto.setContent(comment.getContent());
        commentResponseDto.setPostId(comment.getPost().getId());
        commentResponseDto.setUserId(comment.getUser().getId());
        commentResponseDto.setName(comment.getUser().getFullName());

        return commentResponseDto;
    }

    @Transactional
    public void likeComment(Long id) {
        String email = authorizationUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!comment.getLikedBy().contains(user)) {
            comment.getLikedBy().add(user);
            user.getLikedComments().add(comment);
            comment.setLikes(comment.getLikes() + 1);
            commentRepository.save(comment);
            userRepository.save(user);
        }
    }



    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).
                orElseThrow(() -> new RuntimeException("Comment not found"));
        Post post = comment.getPost();
        post.getComments().remove(comment);
        postRepository.save(post);
        User user = comment.getUser();
        user.getComments().remove(comment);
        userRepository.save(user);
        commentRepository.delete(comment);
    }

}
