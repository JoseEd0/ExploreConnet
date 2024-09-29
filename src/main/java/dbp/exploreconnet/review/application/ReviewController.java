package dbp.exploreconnet.review.application;

import dbp.exploreconnet.review.domain.Review;
import dbp.exploreconnet.review.domain.ReviewService;
import dbp.exploreconnet.review.dto.NewReviewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/new")
    public ResponseEntity<String> createNewReview(@RequestBody NewReviewDto newReview) {
        reviewService.createNewReview(newReview);
        return ResponseEntity.ok("Review created");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Review deleted");
    }
}
