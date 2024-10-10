package dbp.exploreconnet.promotion.application;

import dbp.exploreconnet.promotion.domain.PromotionService;
import dbp.exploreconnet.promotion.dto.NewPromotionDto;
import dbp.exploreconnet.promotion.dto.PromotionResponseDto;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/promotions")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @PreAuthorize("hasAuthority('OWNER')")
    @PostMapping
    public ResponseEntity<PromotionResponseDto> createPromotion(@ModelAttribute NewPromotionDto newPromotionDto) throws FileUploadException {
        return ResponseEntity.ok(promotionService.createPromotion(newPromotionDto));
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponseDto> getPromotionById(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getPromotionById(id));
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponseDto> updatePromotion(@PathVariable Long id, @ModelAttribute NewPromotionDto newPromotionDto) throws FileUploadException{
        return ResponseEntity.ok(promotionService.updatePromotion(id, newPromotionDto));
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @GetMapping
    public ResponseEntity<List<PromotionResponseDto>> getAllPromotions() {
        return ResponseEntity.ok(promotionService.getAllPromotions());
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/place/{placeId}")
    public ResponseEntity<List<PromotionResponseDto>> getPromotionsByPlace(@PathVariable Long placeId) {
        return ResponseEntity.ok(promotionService.getPromotionsByPlace(placeId));
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @GetMapping("/myplaces")
    public ResponseEntity<List<PromotionResponseDto>> getPromotionsByMyPlaces() {
        return ResponseEntity.ok(promotionService.getPromotionsByMyPlaces());
    }
}
