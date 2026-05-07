package com.tuixach.lvt.repository;

import com.tuixach.lvt.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    @Query("SELECT c FROM Coupon c WHERE " +
            "(:keyword IS NULL OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:active IS NULL OR c.active = :active)")
    Page<Coupon> search(@Param("keyword") String keyword,
                        @Param("active") Boolean active,
                        Pageable pageable);

    @Query("SELECT c FROM Coupon c WHERE c.active = true " +
            "AND (c.startDate IS NULL OR c.startDate <= CURRENT_TIMESTAMP) " +
            "AND (c.endDate IS NULL OR c.endDate >= CURRENT_TIMESTAMP) " +
            "AND (c.usageLimit IS NULL OR c.usedCount < c.usageLimit) " +
            "ORDER BY c.id DESC")
    java.util.List<Coupon> findAllAvailable();

    /**
     * Atomic increment guarded by usage_limit. Returns 1 if incremented, 0 if the
     * coupon is exhausted (or no longer exists). Callers MUST rollback when 0.
     */
    @Modifying(clearAutomatically = false, flushAutomatically = true)
    @Query("UPDATE Coupon c SET c.usedCount = c.usedCount + 1 " +
            "WHERE c.id = :couponId " +
            "AND (c.usageLimit IS NULL OR c.usedCount < c.usageLimit)")
    int incrementUsedCountIfAvailable(@Param("couponId") Long couponId);
}
