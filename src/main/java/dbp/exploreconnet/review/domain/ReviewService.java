package dbp.exploreconnet.review.domain;


import dbp.exploreconnet.auth.utils.AuthorizationUtils;
import dbp.exploreconnet.exceptions.ResourceNotFoundException;
import dbp.exploreconnet.place.domain.Place;
import dbp.exploreconnet.place.infrastructure.PlaceRepository;
import dbp.exploreconnet.review.dto.NewReviewDto;
import dbp.exploreconnet.review.dto.ReviewResponseDto;
import dbp.exploreconnet.review.infrastructure.ReviewRepository;
import dbp.exploreconnet.user.domain.User;
import dbp.exploreconnet.user.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private PlaceRepository placeRepository;
    @Autowired
    private UserRepository<User> userRepository;
    @Autowired
    private AuthorizationUtils authorizationUtils;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, UserRepository<User> userRepository, PlaceRepository placeRepository, AuthorizationUtils authorizationUtils) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.placeRepository = placeRepository;
        this.authorizationUtils = authorizationUtils;
    }

    public void createNewReview(NewReviewDto newReview) {
        String authorEmail = authorizationUtils.getCurrentUserEmail();

        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Place place = placeRepository.findById(newReview.getPlaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Place not found"));

        Review review = new Review();
        review.setUser(author);
        review.setPlace(place);
        review.setComment(newReview.getComment());
        review.setRating(newReview.getRating());

        reviewRepository.save(review);
    }

    public void deleteReview(Long id) {
        Review reviewToDelete = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        Long authorId = reviewToDelete.getUser().getId();

        if (!authorizationUtils.isAdminOrResourceOwner(authorId))
            throw new AccessDeniedException("User not authorized to modify this resource");

        reviewRepository.delete(reviewToDelete);
    }

    public ReviewResponseDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        return mapToResponseDto(review);
    }

    public List<ReviewResponseDto> getMyReviews() {
        String currentUserEmail = authorizationUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return reviewRepository.findByUser(user).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<ReviewResponseDto> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<ReviewResponseDto> getReviewsByMyPlace() {
        String currentUserEmail = authorizationUtils.getCurrentUserEmail();
        List<Place> places = placeRepository.findByOwnerEmail(currentUserEmail);
        return places.stream()
                .flatMap(place -> reviewRepository.findByPlace(place).stream())
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private ReviewResponseDto mapToResponseDto(Review review) {
        ReviewResponseDto responseDto = new ReviewResponseDto();
        responseDto.setId(review.getId());
        responseDto.setComment(review.getComment());
        responseDto.setRating(review.getRating());
        responseDto.setPlaceId(review.getPlace().getId());
        responseDto.setPlaceName(review.getPlace().getName());
        return responseDto;
    }
}
