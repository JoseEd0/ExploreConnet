package dbp.exploreconnet.promotion.domain;

import dbp.exploreconnet.promotion.application.PromotionController;
import dbp.exploreconnet.promotion.domain.PromotionService;
import dbp.exploreconnet.promotion.dto.NewPromotionDto;
import dbp.exploreconnet.promotion.dto.PromotionResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PromotionControllerIntegrationTest {

    @InjectMocks
    private PromotionController promotionController;

    @Mock
    private PromotionService promotionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreatePromotion() {
        // Arrange
        NewPromotionDto newPromotion = new NewPromotionDto();
        newPromotion.setDescription("Summer Sale");
        newPromotion.setDiscount(20.0);
        newPromotion.setStartDate(LocalDateTime.now());
        newPromotion.setEndDate(LocalDateTime.now().plusDays(10));
        newPromotion.setPlaceId(1L);

        PromotionResponseDto promotionResponse = new PromotionResponseDto();
        promotionResponse.setId(1L);
        promotionResponse.setDescription("Summer Sale");
        promotionResponse.setDiscount(20.0);

        when(promotionService.createPromotion(newPromotion)).thenReturn(promotionResponse);

        // Act
        ResponseEntity<PromotionResponseDto> response = promotionController.createPromotion(newPromotion);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(promotionResponse, response.getBody());
        verify(promotionService, times(1)).createPromotion(newPromotion);
    }

    @Test
    public void testGetPromotionById() {
        // Arrange
        Long promotionId = 1L;
        PromotionResponseDto promotionResponse = new PromotionResponseDto();
        promotionResponse.setId(promotionId);
        promotionResponse.setDescription("Winter Sale");
        promotionResponse.setDiscount(15.0);

        when(promotionService.getPromotionById(promotionId)).thenReturn(promotionResponse);

        // Act
        ResponseEntity<PromotionResponseDto> response = promotionController.getPromotionById(promotionId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(promotionResponse, response.getBody());
        verify(promotionService, times(1)).getPromotionById(promotionId);
    }

    @Test
    public void testUpdatePromotion() {
        // Arrange
        Long promotionId = 1L;
        NewPromotionDto newPromotion = new NewPromotionDto();
        newPromotion.setDescription("Updated Sale");
        newPromotion.setDiscount(30.0);
        newPromotion.setStartDate(LocalDateTime.now());
        newPromotion.setEndDate(LocalDateTime.now().plusDays(5));
        newPromotion.setPlaceId(1L);

        PromotionResponseDto updatedPromotion = new PromotionResponseDto();
        updatedPromotion.setId(promotionId);
        updatedPromotion.setDescription("Updated Sale");
        updatedPromotion.setDiscount(30.0);

        when(promotionService.updatePromotion(promotionId, newPromotion)).thenReturn(updatedPromotion);

        // Act
        ResponseEntity<PromotionResponseDto> response = promotionController.updatePromotion(promotionId, newPromotion);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedPromotion, response.getBody());
        verify(promotionService, times(1)).updatePromotion(promotionId, newPromotion);
    }

    @Test
    public void testDeletePromotion() {
        // Arrange
        Long promotionId = 1L;
        doNothing().when(promotionService).deletePromotion(promotionId);

        // Act
        ResponseEntity<Void> response = promotionController.deletePromotion(promotionId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(promotionService, times(1)).deletePromotion(promotionId);
    }

    @Test
    public void testGetAllPromotions() {
        // Arrange
        PromotionResponseDto promo1 = new PromotionResponseDto();
        promo1.setId(1L);
        promo1.setDescription("Promo 1");

        PromotionResponseDto promo2 = new PromotionResponseDto();
        promo2.setId(2L);
        promo2.setDescription("Promo 2");

        List<PromotionResponseDto> promotions = Arrays.asList(promo1, promo2);

        when(promotionService.getAllPromotions()).thenReturn(promotions);

        // Act
        ResponseEntity<List<PromotionResponseDto>> response = promotionController.getAllPromotions();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(promotions, response.getBody());
        verify(promotionService, times(1)).getAllPromotions();
    }

    @Test
    public void testGetPromotionsByPlace() {
        // Arrange
        Long placeId = 1L;
        PromotionResponseDto promo1 = new PromotionResponseDto();
        promo1.setId(1L);
        promo1.setDescription("Promo 1");

        PromotionResponseDto promo2 = new PromotionResponseDto();
        promo2.setId(2L);
        promo2.setDescription("Promo 2");

        List<PromotionResponseDto> promotions = Arrays.asList(promo1, promo2);

        when(promotionService.getPromotionsByPlace(placeId)).thenReturn(promotions);

        // Act
        ResponseEntity<List<PromotionResponseDto>> response = promotionController.getPromotionsByPlace(placeId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(promotions, response.getBody());
        verify(promotionService, times(1)).getPromotionsByPlace(placeId);
    }
}
