package dbp.exploreconnet.review.domain;


import dbp.exploreconnet.auth.utils.AuthorizationUtils;
import dbp.exploreconnet.exceptions.ResourceNotFoundException;
import dbp.exploreconnet.place.domain.Place;
import dbp.exploreconnet.place.infrastructure.PlaceRepository;
import dbp.exploreconnet.review.dto.NewReviewDto;
import dbp.exploreconnet.review.infrastructure.ReviewRepository;
import dbp.exploreconnet.user.domain.User;
import dbp.exploreconnet.user.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
}
