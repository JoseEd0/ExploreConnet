package dbp.exploreconnet.promotion.application;

import dbp.exploreconnet.promotion.domain.Promotion;
import dbp.exploreconnet.promotion.domain.PromotionService;
import dbp.exploreconnet.promotion.dto.NewPromotionDto;
import dbp.exploreconnet.promotion.dto.PromotionResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/promotions")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @PostMapping
    public ResponseEntity<PromotionResponseDto> createPromotion(@RequestBody NewPromotionDto newPromotionDto) {
        return ResponseEntity.ok(promotionService.createPromotion(newPromotionDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponseDto> getPromotionById(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getPromotionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponseDto> updatePromotion(@PathVariable Long id, @RequestBody NewPromotionDto newPromotionDto) {
        return ResponseEntity.ok(promotionService.updatePromotion(id, newPromotionDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PromotionResponseDto>> getAllPromotions() {
        return ResponseEntity.ok(promotionService.getAllPromotions());
    }

    @GetMapping("/place/{placeId}")
    public ResponseEntity<List<PromotionResponseDto>> getPromotionsByPlace(@PathVariable Long placeId) {
        return ResponseEntity.ok(promotionService.getPromotionsByPlace(placeId));
    }
}
