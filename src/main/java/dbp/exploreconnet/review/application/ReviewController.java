package dbp.exploreconnet.review.application;

import dbp.exploreconnet.review.domain.Review;
import dbp.exploreconnet.review.domain.ReviewService;
import dbp.exploreconnet.review.dto.NewReviewDto;
import dbp.exploreconnet.review.dto.ReviewResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @PostMapping("/new")
    public ResponseEntity<String> createNewReview(@RequestBody NewReviewDto newReview) {
        reviewService.createNewReview(newReview);
        return ResponseEntity.ok("Review created");
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Review deleted");
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @GetMapping("/myreviews")
    public ResponseEntity<List<ReviewResponseDto>> getMyReviews() {
        return ResponseEntity.ok(reviewService.getMyReviews());
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @GetMapping("/all")
    public ResponseEntity<List<ReviewResponseDto>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @GetMapping("/myplaces")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByMyPlace() {
        return ResponseEntity.ok(reviewService.getReviewsByMyPlace());
    }
}
