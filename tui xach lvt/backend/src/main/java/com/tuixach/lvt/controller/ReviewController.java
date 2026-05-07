package com.tuixach.lvt.controller;

import com.tuixach.lvt.dto.ApiResponse;
import com.tuixach.lvt.dto.ReviewDTO;
import com.tuixach.lvt.dto.ReviewEligibilityDTO;
import com.tuixach.lvt.dto.ReviewRequest;
import com.tuixach.lvt.dto.ReviewSummaryDTO;
import com.tuixach.lvt.entity.User;
import com.tuixach.lvt.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        Page<ReviewDTO> reviews = reviewService.getProductReviews(productId, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    // Backward-compatibility endpoint for existing clients.
    @GetMapping("/reviews/product/{productId}")
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> getProductReviewsLegacy(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        Page<ReviewDTO> reviews = reviewService.getProductReviews(productId, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @GetMapping("/products/{productId}/reviews/summary")
    public ResponseEntity<ApiResponse<ReviewSummaryDTO>> getReviewSummary(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getSummary(productId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/reviews/eligibility/{productId}")
    public ResponseEntity<ApiResponse<ReviewEligibilityDTO>> getReviewEligibility(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.checkEligibility(user, productId)));
    }

    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewDTO>> createReview(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReviewRequest request) {
        ReviewDTO review = reviewService.createReview(user, request);
        return ResponseEntity.ok(ApiResponse.success("Đánh giá thành công", review));
    }
}
