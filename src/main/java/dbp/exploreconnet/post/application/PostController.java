package dbp.exploreconnet.post.application;

import dbp.exploreconnet.post.domain.PostService;
import dbp.exploreconnet.post.dto.*;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {
    private final PostService postService;



    @PreAuthorize("hasAnyAuthority('USER', 'OWNER','GUEST')")
    @GetMapping("/place/{placeId}")
    public ResponseEntity<List<PostResponseDto>> getPostsByPlaceId(@PathVariable Long placeId) {
        return ResponseEntity.ok(postService.getPostsByPlaceId(placeId));
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER','GUEST')")
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER','GUEST')")
    @GetMapping("/all")
    public ResponseEntity<Page<PostResponseDto>> getAllPosts(@RequestParam int page, @RequestParam int size) {
        Page<PostResponseDto> response = postService.getAllPosts(page, size);
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @GetMapping("/me")
    public ResponseEntity<Page<PostResponseDto>> getPostsByCurrentUser(@RequestParam int page, @RequestParam int size) {
        Page<PostResponseDto> response = postService.getPostsByCurrentUser(page, size);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponseDto>> getPostsByUserId(@PathVariable Long userId, @RequestParam int page, @RequestParam int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<PostResponseDto> response = postService.getPostsByUserId(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @PostMapping
    public ResponseEntity<Void> createPost(@ModelAttribute PostRequestDto postRequestDto) throws FileUploadException {
        postService.createPost(postRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @PatchMapping("/content/{id}")
    public ResponseEntity<Void> changeContent(@PathVariable Long id, @RequestBody PostUpdateContentDto content) {
        postService.changeContent(id, content.getPlaceId());
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @PatchMapping("/like/{id}")
    public ResponseEntity<Void> likePost(@PathVariable Long id) {
        postService.likePost(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @PatchMapping("/media/{id}")
    public ResponseEntity<Void> changeMedia( @PathVariable Long id, @ModelAttribute PostMediaUpdateRequestDto mediaRequestDto) throws FileUploadException {
        postService.changeMedia(id, mediaRequestDto);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @GetMapping("/share/{id}")
    public ResponseEntity<String> getShareableUrl(@PathVariable Long id) {
        String url = postService.getShareableUrl(id);
        return ResponseEntity.ok(url);
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
