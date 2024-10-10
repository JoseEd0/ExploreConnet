package dbp.exploreconnet.post.domain;

import dbp.exploreconnet.auth.utils.AuthorizationUtils;
import dbp.exploreconnet.exceptions.ResourceNotFoundException;
import dbp.exploreconnet.exceptions.UnauthorizedOperationException;
import dbp.exploreconnet.mediaStorage.domain.MediaStorageService;
import dbp.exploreconnet.place.domain.Place;
import dbp.exploreconnet.place.dto.PlaceResponseForPostDto;
import dbp.exploreconnet.place.infrastructure.PlaceRepository;
import dbp.exploreconnet.post.dto.PostMediaUpdateRequestDto;
import dbp.exploreconnet.post.dto.PostRequestDto;
import dbp.exploreconnet.post.dto.PostResponseDto;
import dbp.exploreconnet.post.dto.PostUpdateDto;
import dbp.exploreconnet.post.infrastructure.PostRepository;
import dbp.exploreconnet.user.domain.User;
import dbp.exploreconnet.user.infrastructure.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;


    private final AuthorizationUtils authorizationUtils;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final MediaStorageService mediaStorageService;

    private final PlaceRepository placeRepository;


    private PostResponseDto getPostResponseDto(Post post) {
        PostResponseDto postResponseDTO = modelMapper.map(post, PostResponseDto.class);
        postResponseDTO.setOwner(post.getUser().getFullName());
        postResponseDTO.setOwnerId(post.getUser().getId());
        postResponseDTO.setProfileImage(post.getUser().getProfileImageUrl());
        postResponseDTO.setLikedByUserIds(post.getLikedBy().stream().map(User::getId).collect(Collectors.toSet()));
        postResponseDTO.setCreatedAt(post.getCreatedAt());

        if (post.getPlace() != null) {
            PlaceResponseForPostDto placeDto = new PlaceResponseForPostDto();
            placeDto.setId(post.getPlace().getId());
            placeDto.setName(post.getPlace().getName());
            placeDto.setImage(post.getPlace().getImageUrl());
            placeDto.setDescription(post.getPlace().getDescription());
            placeDto.setCategory(post.getPlace().getCategory());
            placeDto.setOpeningHours(post.getPlace().getOpeningHours());
            postResponseDTO.setPlace(placeDto);

        }
        return postResponseDTO;

    }


    public PostResponseDto getPostById(Long id) {

        Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return getPostResponseDto(post);
    }

    public Page<PostResponseDto> getPostsByUserId(Long userId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserId(userId, pageable);
        return posts.map(this::getPostResponseDto);
    }

    public Page<PostResponseDto> getPostsByCurrentUser(int page, int size) {
        String email = authorizationUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        return posts.map(this::getPostResponseDto);
    }

    public Page<PostResponseDto> getAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        return posts.map(this::getPostResponseDto);
    }

    public List<PostResponseDto> getPostsByPlaceId(Long placeId) {

        List<Post> posts = postRepository.findByPlaceId(placeId);
        return posts.stream().map(this::getPostResponseDto).collect(Collectors.toList());
    }


    @Transactional
    public void createPost(PostRequestDto postRequestDto) throws FileUploadException {
        Place place = placeRepository.findById(postRequestDto.getPlaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Place not found"));
        User user = userRepository.findById(postRequestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post post = new Post();
        post.setDescription(postRequestDto.getDescription());
        post.setPlace(place);
        post.setUser(user);
        post.setCreatedAt(LocalDateTime.now());

        if (postRequestDto.getImage() != null && !postRequestDto.getImage().isEmpty()) {
            post.setImageUrl(mediaStorageService.uploadFile(postRequestDto.getImage()));
        }
        if (postRequestDto.getVideo() != null && !postRequestDto.getVideo().isEmpty()) {
            post.setVideoUrl(mediaStorageService.uploadFile(postRequestDto.getVideo()));
        }

        postRepository.save(post);
        post.setShareableUrl("https://exploreconnect.com/foro/post/" + post.getId());
        postRepository.save(post);

        // Añadir el post al usuario y guardar el usuario actualizado
        user.getPosts().add(post);
        userRepository.save(user);
    }




    public String getShareableUrl(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return post.getShareableUrl();
    }



    @Transactional
    public void changeMedia(Long id, PostMediaUpdateRequestDto mediaRequestDto) throws FileUploadException {
        String email = authorizationUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verificar que el usuario sea el propietario o administrador
        if (!authorizationUtils.isAdminOrResourceOwner(user.getId())) {
            throw new UnauthorizedOperationException("Only the owner can change the media of this post");
        }

        // Obtener el post
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        try {
            // Procesar la imagen si se proporciona
            if (mediaRequestDto.getImage() != null && !mediaRequestDto.getImage().isEmpty()) {
                String imageUrl = mediaStorageService.uploadFile(mediaRequestDto.getImage());
                post.setImageUrl(imageUrl);
            }

            // Procesar el video si se proporciona
            if (mediaRequestDto.getVideo() != null && !mediaRequestDto.getVideo().isEmpty()) {
                String videoUrl = mediaStorageService.uploadFile(mediaRequestDto.getVideo());
                post.setVideoUrl(videoUrl);
            }

        } catch (IOException e) {
            throw new FileUploadException("Failed to upload file", e);
        }

        // Actualizar la descripción si se proporciona
        if (mediaRequestDto.getDescription() != null && !mediaRequestDto.getDescription().isEmpty()) {
            post.setDescription(mediaRequestDto.getDescription());
        }

        postRepository.save(post);
    }



    public void changeContent(Long id, Long placeId) {
        String email = authorizationUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!authorizationUtils.isAdminOrResourceOwner(user.getId())) {
            throw new UnauthorizedOperationException("Only the owner can change the content of this post");
        }
        Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if (placeId != null) {
            Place place = placeRepository.findById(placeId).orElseThrow(() -> new ResourceNotFoundException("Place not found"));
            post.setPlace(place);
        }

        postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id) {
        String email = authorizationUtils.getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!authorizationUtils.isAdminOrResourceOwner(currentUser.getId())) {
            throw new UnauthorizedOperationException("Only the owner can delete this post");
        }
        Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        User user = post.getUser();
        user.getPosts().remove(post);
        userRepository.save(user);
        postRepository.delete(post);
    }

    @Transactional
    public void likePost(Long id) {
        String email = authorizationUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getLikedBy().contains(user)) {
            post.getLikedBy().add(user);
            user.getLikedPosts().add(post);
            post.setLikes(post.getLikes() + 1);
            postRepository.save(post);
            userRepository.save(user);
        }
    }
}




