package dbp.exploreconnet.review.domain;

import dbp.exploreconnet.review.application.ReviewController;
import dbp.exploreconnet.review.domain.ReviewService;
import dbp.exploreconnet.review.dto.NewReviewDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ReviewControllerIntegrationTest {

    @InjectMocks
    private ReviewController reviewController;

    @Mock
    private ReviewService reviewService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateNewReview() {
        NewReviewDto newReview = new NewReviewDto();
        newReview.setComment("Great place!");
        newReview.setRating(5);
        newReview.setPlaceId(1L);

        ResponseEntity<String> response = reviewController.createNewReview(newReview);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Review created", response.getBody());
        verify(reviewService, times(1)).createNewReview(newReview);
    }

    @Test
    public void testDeleteReview() {
        Long reviewId = 1L;

        ResponseEntity<String> response = reviewController.deleteReview(reviewId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Review deleted", response.getBody());
        verify(reviewService, times(1)).deleteReview(reviewId);
    }
}
