package dbp.exploreconnet.post.application;

import dbp.exploreconnet.post.domain.PostService;
import dbp.exploreconnet.post.dto.PostRequestDto;
import dbp.exploreconnet.post.dto.PostResponseDto;
import dbp.exploreconnet.post.dto.PostUpdateContentDto;
import dbp.exploreconnet.post.dto.PostUpdateDto;
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



    @GetMapping("/place/{placeId}")
    public ResponseEntity<List<PostResponseDto>> getPostsByPlaceId(@PathVariable Long songId) {
        return ResponseEntity.ok(postService.getPostsByPlaceId(songId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<PostResponseDto>> getAllPosts(@RequestParam int page, @RequestParam int size) {
        Page<PostResponseDto> response = postService.getAllPosts(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Page<PostResponseDto>> getPostsByCurrentUser(@RequestParam int page, @RequestParam int size) {
        Page<PostResponseDto> response = postService.getPostsByCurrentUser(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponseDto>> getPostsByUserId(@PathVariable Long userId, @RequestParam int page, @RequestParam int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<PostResponseDto> response = postService.getPostsByUserId(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> createPost(@ModelAttribute PostRequestDto postRequestDto) throws FileUploadException {
        postService.createPost(postRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PostMapping("/many")
    public ResponseEntity<Void> createPosts(@RequestBody List<PostRequestDto> postRequestDtos) throws FileUploadException {
        postService.createPosts(postRequestDtos);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/content/{id}")
    public ResponseEntity<Void> updatePostContent(@PathVariable Long id, @RequestBody PostUpdateContentDto content) {
        postService.changeContent(id, content.getPlaceId());
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/like/{id}")
    public ResponseEntity<Void> likePost(@PathVariable Long id) {
        postService.likePost(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/media/{id}")
    public ResponseEntity<Void> changeMedia(@PathVariable Long id, @RequestBody PostUpdateDto media) {
        postService.changeMedia(id, media);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/shareable-url/{id}")
    public ResponseEntity<String> getShareableUrl(@PathVariable Long id) {
        String url = postService.getShareableUrl(id);
        return ResponseEntity.ok(url);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
