package com.tuixach.lvt.repository;

import com.tuixach.lvt.entity.OrderItem;
import com.tuixach.lvt.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi FROM OrderItem oi JOIN oi.order o " +
	    "WHERE o.user.id = :userId AND oi.product.id = :productId AND o.status IN :statuses " +
	    "ORDER BY o.createdAt DESC")
    Optional<OrderItem> findLatestEligibleOrderItem(@Param("userId") Long userId,
	    @Param("productId") Long productId,
	    @Param("statuses") Collection<Order.OrderStatus> statuses);

    boolean existsByOrderUserIdAndProductId(Long userId, Long productId);

    boolean existsByOrderUserIdAndProductIdAndOrderStatusIn(Long userId,
	    Long productId,
	    Collection<Order.OrderStatus> statuses);

	    @Query("SELECT p.id, p.name, c.name, SUM(oi.quantity), COALESCE(SUM(oi.subtotal), 0), p.quantity, pi.imageUrl " +
		    "FROM OrderItem oi " +
		    "JOIN oi.order o " +
		    "JOIN oi.product p " +
		    "LEFT JOIN p.category c " +
		    "LEFT JOIN p.images pi ON pi.isPrimary = true " +
		    "WHERE o.status IN :statuses AND o.createdAt BETWEEN :start AND :end " +
		    "GROUP BY p.id, p.name, c.name, p.quantity, pi.imageUrl " +
		    "ORDER BY SUM(oi.quantity) DESC")
	    List<Object[]> findTopSellersInRange(@Param("statuses") Collection<Order.OrderStatus> statuses,
		    @Param("start") LocalDateTime start,
		    @Param("end") LocalDateTime end,
		    org.springframework.data.domain.Pageable pageable);

	    @Query("SELECT p.id, p.name, c.name, p.quantity, " +
		    "COALESCE(SUM(CASE WHEN o.status IN :statuses AND o.createdAt BETWEEN :start AND :end THEN oi.quantity ELSE 0 END), 0), " +
		    "COALESCE(SUM(CASE WHEN o.status IN :statuses AND o.createdAt BETWEEN :start AND :end THEN oi.subtotal ELSE 0 END), 0), " +
		    "pi.imageUrl " +
		    "FROM Product p " +
		    "LEFT JOIN p.images pi ON pi.isPrimary = true " +
		    "LEFT JOIN OrderItem oi ON oi.product = p " +
		    "LEFT JOIN oi.order o " +
		    "LEFT JOIN p.category c " +
		    "WHERE p.active = true AND p.createdAt < :createdBefore " +
		    "GROUP BY p.id, p.name, c.name, p.quantity, pi.imageUrl " +
		    "HAVING COALESCE(SUM(CASE WHEN o.status IN :statuses AND o.createdAt BETWEEN :start AND :end THEN oi.quantity ELSE 0 END), 0) < :threshold " +
		    "ORDER BY COALESCE(SUM(CASE WHEN o.status IN :statuses AND o.createdAt BETWEEN :start AND :end THEN oi.quantity ELSE 0 END), 0) ASC, p.quantity DESC")
	    List<Object[]> findSlowMovers(@Param("statuses") Collection<Order.OrderStatus> statuses,
		    @Param("start") LocalDateTime start,
		    @Param("end") LocalDateTime end,
		    @Param("createdBefore") LocalDateTime createdBefore,
		    @Param("threshold") long threshold,
		    org.springframework.data.domain.Pageable pageable);

	    @Query("SELECT p.id, p.name, c.name, p.quantity, pi.imageUrl " +
		    "FROM Product p " +
		    "LEFT JOIN p.images pi ON pi.isPrimary = true " +
		    "LEFT JOIN OrderItem oi ON oi.product = p " +
		    "LEFT JOIN oi.order o ON o.status IN :statuses AND o.createdAt BETWEEN :start AND :end " +
		    "LEFT JOIN p.category c " +
		    "WHERE p.active = true " +
		    "GROUP BY p.id, p.name, c.name, p.quantity, pi.imageUrl " +
		    "HAVING COALESCE(SUM(CASE WHEN o.id IS NOT NULL THEN oi.quantity ELSE 0 END), 0) = 0 " +
		    "ORDER BY p.createdAt DESC")
	    List<Object[]> findZeroSalesProducts(@Param("statuses") Collection<Order.OrderStatus> statuses,
		    @Param("start") LocalDateTime start,
		    @Param("end") LocalDateTime end,
		    org.springframework.data.domain.Pageable pageable);
}
