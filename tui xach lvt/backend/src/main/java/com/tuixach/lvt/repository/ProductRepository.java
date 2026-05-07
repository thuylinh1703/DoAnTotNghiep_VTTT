package com.tuixach.lvt.repository;

import com.tuixach.lvt.entity.Order;
import com.tuixach.lvt.entity.Product;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByActiveTrue(Pageable pageable);

    List<Product> findByFeaturedTrueAndActiveTrue();

    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.createdAt DESC")
    List<Product> findNewProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<Product> findNewProductsSince(@Param("since") LocalDateTime since, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.discountPrice IS NOT NULL AND p.discountPrice < p.price")
    List<Product> findDiscountedProducts();

    @Query("SELECT p FROM Product p WHERE p.active = true " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:brand IS NULL OR p.brand = :brand) " +
            "AND (:minPrice IS NULL OR COALESCE(p.discountPrice, p.price) >= :minPrice) " +
            "AND (:maxPrice IS NULL OR COALESCE(p.discountPrice, p.price) <= :maxPrice) " +
            "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:createdSince IS NULL OR p.createdAt >= :createdSince)")
    Page<Product> findWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("brand") String brand,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("keyword") String keyword,
            @Param("createdSince") LocalDateTime createdSince,
            Pageable pageable);

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.brand IS NOT NULL AND p.active = true")
    List<String> findAllBrands();

    /**
     * Candidate pool for "similar products" — same category, price within
     * [minPrice, maxPrice], excluding the source product. Scoring happens in
     * application code so we can pull a slightly wider pool and re-rank.
     */
    @Query("SELECT p FROM Product p WHERE p.active = true " +
            "AND p.id <> :excludeId " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findCandidatesForRelated(
            @Param("excludeId") Long excludeId,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    /**
     * Fallback pool when the price-banded candidates are too few — same
     * category, same filters without price constraint.
     */
    @Query("SELECT p FROM Product p WHERE p.active = true " +
            "AND p.id <> :excludeId " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "ORDER BY p.featured DESC, p.createdAt DESC")
    List<Product> findSameCategoryFallback(
            @Param("excludeId") Long excludeId,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    /**
     * Top-selling products in specific categories for accessory recommendations.
     * Joins with order_items to count volume.
     */
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN OrderItem oi ON p.id = oi.product.id " +
            "WHERE p.active = true " +
            "AND p.id <> :excludeId " +
            "AND p.category.id IN :categoryIds " +
            "GROUP BY p.id " +
            "ORDER BY SUM(COALESCE(oi.quantity, 0)) DESC, p.featured DESC, p.createdAt DESC")
    List<Product> findTopSellingInCategories(
            @Param("excludeId") Long excludeId,
            @Param("categoryIds") List<Long> categoryIds,
            Pageable pageable);

    List<Product> findByQuantityLessThanOrderByQuantityAsc(int threshold);

    @Query("SELECT new com.tuixach.lvt.dto.ReportDTO$TopProductDTO(p.id, p.name, CAST(SUM(oi.quantity) AS int), SUM(oi.subtotal), p.quantity) " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "JOIN oi.order o " +
            "WHERE o.status IN :statuses " +
            "GROUP BY p.id, p.name, p.quantity " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<com.tuixach.lvt.dto.ReportDTO.TopProductDTO> findTopSellersByStatuses(
            @Param("statuses") List<Order.OrderStatus> statuses,
            Pageable pageable);

    default List<com.tuixach.lvt.dto.ReportDTO.TopProductDTO> findTopSellers(Pageable pageable) {
        return findTopSellersByStatuses(List.of(Order.OrderStatus.DELIVERED, Order.OrderStatus.COMPLETED), pageable);
    }
}
