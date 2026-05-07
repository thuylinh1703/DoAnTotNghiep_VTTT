package com.tuixach.lvt.repository;

import com.tuixach.lvt.entity.CouponUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    long countByCouponIdAndUserId(Long couponId, Long userId);

    Page<CouponUsage> findByCouponIdOrderByUsedAtDesc(Long couponId, Pageable pageable);
}
