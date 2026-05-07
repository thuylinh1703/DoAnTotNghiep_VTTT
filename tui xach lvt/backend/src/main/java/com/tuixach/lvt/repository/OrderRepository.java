package com.tuixach.lvt.repository;

import com.tuixach.lvt.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Order> findByOrderCode(String orderCode);
    
    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:search IS NULL OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(o.receiverName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Order> searchOrders(@Param("status") Order.OrderStatus status, @Param("search") String search, Pageable pageable);

    long countByStatus(Order.OrderStatus status);

        @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status IN ('DELIVERED', 'COMPLETED')")
    BigDecimal getTotalRevenue();

    @Query("SELECT MONTH(o.createdAt) as month, SUM(o.totalAmount) as revenue " +
            "FROM Order o WHERE o.status IN ('PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'COMPLETED') AND YEAR(o.createdAt) = :year " +
            "GROUP BY MONTH(o.createdAt) ORDER BY MONTH(o.createdAt)")
    List<Object[]> getMonthlyRevenue(@Param("year") int year);

    @Query("SELECT CAST(o.createdAt AS DATE) as date, COUNT(o) as count " +
            "FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY CAST(o.createdAt AS DATE) ORDER BY CAST(o.createdAt AS DATE)")
    List<Object[]> getDailyOrderCount(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    List<Order> findByStatusAndPaymentMethodAndStockRestoredFalseAndCreatedAtBefore(
            Order.OrderStatus status,
            Order.PaymentMethod paymentMethod,
            LocalDateTime cutoff);

    default List<Order> findAbandonedVnpayOrders(LocalDateTime cutoff) {
        return findByStatusAndPaymentMethodAndStockRestoredFalseAndCreatedAtBefore(
                Order.OrderStatus.PENDING,
                Order.PaymentMethod.VNPAY,
                cutoff);
    }

    List<Order> findByStatusInAndCreatedAtBetween(List<Order.OrderStatus> statuses,
            LocalDateTime start,
            LocalDateTime end);

    @Query("SELECT o.paymentMethod, COALESCE(SUM(o.totalAmount), 0), COUNT(o) " +
            "FROM Order o " +
            "WHERE o.status IN :statuses AND o.createdAt BETWEEN :start AND :end " +
            "GROUP BY o.paymentMethod")
    List<Object[]> summarizeRevenueByPaymentMethod(@Param("statuses") List<Order.OrderStatus> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT c.name, COALESCE(SUM(oi.subtotal), 0), COUNT(DISTINCT o.id) " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "JOIN oi.product p " +
            "LEFT JOIN p.category c " +
            "WHERE o.status IN :statuses AND o.createdAt BETWEEN :start AND :end " +
            "GROUP BY c.name " +
            "ORDER BY COALESCE(SUM(oi.subtotal), 0) DESC")
    List<Object[]> summarizeRevenueByCategory(@Param("statuses") List<Order.OrderStatus> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status IN ('DELIVERED', 'COMPLETED') AND o.createdAt BETWEEN :start AND :end")
    BigDecimal getRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
