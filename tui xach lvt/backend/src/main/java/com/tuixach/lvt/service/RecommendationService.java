package com.tuixach.lvt.service;

import com.tuixach.lvt.dto.ProductDTO;
import com.tuixach.lvt.dto.RelatedProductsDTO;
import com.tuixach.lvt.entity.Product;
import com.tuixach.lvt.exception.ResourceNotFoundException;
import com.tuixach.lvt.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ProductRepository productRepository;
    private final ProductService productService;

    @Value("${recommendation.accessory-category-ids:}")
    private List<Long> accessoryCategoryIds;

    /**
     * Finds related products for a given product ID.
     * Split into "similar" (same category, similar price) and "accessories" (top-selling items from accessory categories).
     *
     * @param productId The source product ID
     * @param limit     Max number of similar products to return.
     * @return RelatedProductsDTO containing both similar products and accessories.
     */
    @Cacheable(value = "relatedProducts", key = "#productId")
    public RelatedProductsDTO getRelatedProducts(Long productId, Integer limit) {
        Product source = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        int maxSimilar = limit != null ? limit : 6;
        int maxAccessories = 4;

        // 1. Find similar products
        List<ProductDTO> similar = findSimilarProducts(source, maxSimilar);

        // 2. Find accessories
        List<ProductDTO> accessories = findAccessories(source, maxAccessories);

        return RelatedProductsDTO.builder()
                .similar(similar)
                .accessories(accessories)
                .build();
    }

    private List<ProductDTO> findSimilarProducts(Product source, int limit) {
        BigDecimal sourcePrice = source.getDiscountPrice() != null ? source.getDiscountPrice() : source.getPrice();
        BigDecimal minPrice = sourcePrice.multiply(new BigDecimal("0.7"));
        BigDecimal maxPrice = sourcePrice.multiply(new BigDecimal("1.3"));

        // Pull candidates in price range + same category
        List<Product> candidates = productRepository.findCandidatesForRelated(
                source.getId(),
                source.getCategory() != null ? source.getCategory().getId() : null,
                minPrice,
                maxPrice,
                PageRequest.of(0, limit * 2)
        );

        // Score and rank
        List<ProductDTO> scoredSimilar = candidates.stream()
                .map(p -> {
                    double score = calculateSimilarityScore(source, sourcePrice, p);
                    return new ScoredProduct(p, score);
                })
                .sorted(Comparator.comparingDouble(ScoredProduct::score).reversed())
                .limit(limit)
                .map(sp -> productService.mapToDTO(sp.product()))
                .collect(Collectors.toCollection(ArrayList::new));

        // Fallback if not enough similar products from the price band
        if (scoredSimilar.size() < limit) {
            List<Product> fallbacks = productRepository.findSameCategoryFallback(
                    source.getId(),
                    source.getCategory() != null ? source.getCategory().getId() : null,
                    PageRequest.of(0, limit)
            );
            
            for (Product f : fallbacks) {
                if (scoredSimilar.size() >= limit) break;
                
                boolean alreadyInList = scoredSimilar.stream()
                        .anyMatch(p -> p.getId().equals(f.getId()));
                
                if (!alreadyInList) {
                    scoredSimilar.add(productService.mapToDTO(f));
                }
            }
        }

        return scoredSimilar;
    }

    private double calculateSimilarityScore(Product source, BigDecimal sourcePrice, Product target) {
        double score = 0.5; // Base score for same category (implicit from query)

        // Price similarity: +0.3 * (1 - |priceDiff| / sourcePrice)
        BigDecimal targetPrice = target.getDiscountPrice() != null ? target.getDiscountPrice() : target.getPrice();
        BigDecimal priceDiff = sourcePrice.subtract(targetPrice).abs();
        
        if (sourcePrice.compareTo(BigDecimal.ZERO) > 0) {
            double priceSimilarity = 1.0 - priceDiff.divide(sourcePrice, 4, RoundingMode.HALF_UP).doubleValue();
            score += 0.3 * Math.max(0, priceSimilarity);
        }

        // Brand similarity: +0.2 if same brand
        if (source.getBrand() != null && source.getBrand().equalsIgnoreCase(target.getBrand())) {
            score += 0.2;
        }

        return score;
    }

    private List<ProductDTO> findAccessories(Product source, int limit) {
        if (accessoryCategoryIds == null || accessoryCategoryIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Only recommend accessories if the source product is NOT an accessory itself
        boolean isAccessory = source.getCategory() != null && 
                             accessoryCategoryIds.contains(source.getCategory().getId());
        
        if (isAccessory) {
            return Collections.emptyList();
        }

        return productRepository.findTopSellingInCategories(
                source.getId(),
                accessoryCategoryIds,
                PageRequest.of(0, limit)
        ).stream()
                .map(productService::mapToDTO)
                .collect(Collectors.toList());
    }

    private record ScoredProduct(Product product, double score) {}
}
