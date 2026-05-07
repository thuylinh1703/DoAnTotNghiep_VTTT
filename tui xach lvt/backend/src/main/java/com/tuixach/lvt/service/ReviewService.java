package com.tuixach.lvt.service;

import com.tuixach.lvt.dto.ReviewDTO;
import com.tuixach.lvt.dto.ReviewEligibilityDTO;
import com.tuixach.lvt.dto.ReviewRequest;
import com.tuixach.lvt.dto.ReviewSummaryDTO;
import com.tuixach.lvt.entity.Order;
import com.tuixach.lvt.entity.OrderItem;
import com.tuixach.lvt.entity.Product;
import com.tuixach.lvt.entity.Review;
import com.tuixach.lvt.entity.User;
import com.tuixach.lvt.exception.ResourceNotFoundException;
import com.tuixach.lvt.exception.ReviewException;
import com.tuixach.lvt.repository.OrderItemRepository;
import com.tuixach.lvt.repository.ProductRepository;
import com.tuixach.lvt.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    private static final EnumSet<Order.OrderStatus> REVIEW_ELIGIBLE_STATUSES = EnumSet.of(
            Order.OrderStatus.DELIVERED,
            Order.OrderStatus.COMPLETED);

    public Page<ReviewDTO> getProductReviews(Long productId, int page, int size, String sort) {
        Sort.Order order = switch (sort == null ? "createdAt,desc" : sort.toLowerCase()) {
            case "rating,asc" -> Sort.Order.asc("rating");
            case "rating,desc" -> Sort.Order.desc("rating");
            default -> Sort.Order.desc("createdAt");
        };
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));
        return reviewRepository.findByProductIdAndActiveTrue(productId, pageable).map(this::mapToDTO);
    }

    public ReviewSummaryDTO getSummary(Long productId) {
        Map<String, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(String.valueOf(i), 0L);
        }

        for (Object[] row : reviewRepository.getRatingDistributionByProductId(productId)) {
            int rating = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            distribution.put(String.valueOf(rating), count);
        }

        int reviewCount = reviewRepository.getReviewCountByProductId(productId);
        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);

        return ReviewSummaryDTO.builder()
                .averageRating(avgRating == null ? BigDecimal.ZERO : BigDecimal.valueOf(avgRating)
                        .setScale(1, RoundingMode.HALF_UP))
                .totalCount(reviewCount)
                .distribution(distribution)
                .build();
    }

    public ReviewEligibilityDTO checkEligibility(User user, Long productId) {
        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            return ReviewEligibilityDTO.builder()
                    .eligible(false)
                    .reason(ReviewException.Code.ALREADY_REVIEWED.name())
                    .build();
        }

        boolean hasAnyPurchase = orderItemRepository.existsByOrderUserIdAndProductId(user.getId(), productId);
        if (!hasAnyPurchase) {
            return ReviewEligibilityDTO.builder()
                    .eligible(false)
                    .reason(ReviewException.Code.NOT_PURCHASED.name())
                    .build();
        }

        OrderItem orderItem = orderItemRepository.findLatestEligibleOrderItem(user.getId(), productId,
                REVIEW_ELIGIBLE_STATUSES).orElse(null);
        if (orderItem == null) {
            return ReviewEligibilityDTO.builder()
                    .eligible(false)
                    .reason(ReviewException.Code.NOT_DELIVERED.name())
                    .build();
        }

        return ReviewEligibilityDTO.builder()
                .eligible(true)
                .orderItemId(orderItem.getId())
                .build();
    }

    @Transactional
    public ReviewDTO createReview(User user, ReviewRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new ReviewException(ReviewException.Code.ALREADY_REVIEWED, "Bạn đã đánh giá sản phẩm này rồi");
        }

        OrderItem eligibleOrderItem = orderItemRepository.findLatestEligibleOrderItem(user.getId(), product.getId(),
                REVIEW_ELIGIBLE_STATUSES)
                .orElseThrow(() -> {
                    boolean hasAnyPurchase = orderItemRepository.existsByOrderUserIdAndProductId(user.getId(),
                            product.getId());
                    if (!hasAnyPurchase) {
                        return new ReviewException(ReviewException.Code.NOT_PURCHASED,
                                "Bạn cần mua sản phẩm trước khi đánh giá");
                    }
                    return new ReviewException(ReviewException.Code.NOT_DELIVERED,
                            "Bạn chỉ có thể đánh giá sau khi đơn hàng đã giao thành công");
                });

        Review review = Review.builder()
                .user(user)
                .product(product)
                .orderItem(eligibleOrderItem)
                .verifiedPurchase(true)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            java.util.List<com.tuixach.lvt.entity.ReviewImage> reviewImages = request.getImages().stream()
                    .map(url -> com.tuixach.lvt.entity.ReviewImage.builder()
                            .review(review)
                            .imageUrl(url)
                            .build())
                    .toList();
            review.setImages(reviewImages);
        }

        reviewRepository.save(review);

        // Update product rating
        updateProductRating(product.getId());

        return mapToDTO(review);
    }

    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá"));

        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);

        // Update product rating
        updateProductRating(productId);
    }

    @Transactional
    public void toggleReviewStatus(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá"));

        review.setActive(!review.isActive());
        reviewRepository.save(review);

        // Update product rating because changing active status affects average rating
        updateProductRating(review.getProduct().getId());
    }

    // Admin: get all reviews
    public Page<ReviewDTO> getAllReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return reviewRepository.findAll(pageable).map(this::mapToDTO);
    }

    private void updateProductRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);
        int reviewCount = reviewRepository.getReviewCountByProductId(productId);

        product.setAverageRating(avgRating != null
                ? BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP)
                : null);
        product.setReviewCount(reviewCount);
        productRepository.save(product);
    }

    private ReviewDTO mapToDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .orderItemId(review.getOrderItem() != null ? review.getOrderItem().getId() : null)
                .verifiedPurchase(review.isVerifiedPurchase())
                .rating(review.getRating())
                .comment(review.getComment())
                .images(review.getImages() != null ? review.getImages().stream().map(com.tuixach.lvt.entity.ReviewImage::getImageUrl).toList() : null)
                .active(review.isActive())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
