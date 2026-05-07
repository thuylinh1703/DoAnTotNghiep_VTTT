package com.tuixach.lvt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private long totalOrders;
    private long totalProducts;
    private BigDecimal totalRevenue;
    private long totalUsers;
    private long totalReviews;
    private long pendingOrders;
    private long shippingOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private int year;
    private String currentDate;
    private Map<Integer, BigDecimal> monthlyRevenue;
    private Map<String, Long> dailyOrders;
    private java.util.List<OrderDTO> recentOrders;

    // Growth rates
    private double revenueGrowth;
    private double ordersGrowth;
    private double usersGrowth;
    private double reviewsGrowth;
}
