package com.tuixach.lvt.service;

import com.tuixach.lvt.dto.DashboardDTO;
import com.tuixach.lvt.entity.Order;
import com.tuixach.lvt.entity.User;
import com.tuixach.lvt.repository.OrderRepository;
import com.tuixach.lvt.repository.ProductRepository;
import com.tuixach.lvt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final com.tuixach.lvt.repository.ReviewRepository reviewRepository;
    private final OrderService orderService;

    public DashboardDTO getDashboardData(int year) {
        long totalOrders = orderRepository.count();
        long totalProducts = productRepository.count();
        BigDecimal totalRevenue = orderRepository.getTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        long totalUsers = userRepository.countByRole(User.Role.USER);
        long totalReviews = reviewRepository.count();

        long pendingOrders = orderRepository.countByStatus(Order.OrderStatus.PENDING);
        long shippingOrders = orderRepository.countByStatus(Order.OrderStatus.SHIPPING);
        long deliveredOrders = orderRepository.countByStatus(Order.OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByStatus(Order.OrderStatus.CANCELLED);

        // Monthly revenue
        Map<Integer, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) {
            monthlyRevenue.put(i, BigDecimal.ZERO);
        }
        List<Object[]> revenueData = orderRepository.getMonthlyRevenue(year);
        for (Object[] row : revenueData) {
            int month = ((Number) row[0]).intValue();
            BigDecimal revenue = (BigDecimal) row[1];
            monthlyRevenue.put(month, revenue);
        }

        // Daily orders (last 30 days)
        Map<String, Long> dailyOrders = new LinkedHashMap<>();
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);
        List<Object[]> dailyData = orderRepository.getDailyOrderCount(startDate, endDate);
        for (Object[] row : dailyData) {
            String date = row[0].toString();
            long count = ((Number) row[1]).longValue();
            dailyOrders.put(date, count);
        }

        // Recent orders
        List<com.tuixach.lvt.dto.OrderDTO> recentOrders = orderRepository.findAll(
                org.springframework.data.domain.PageRequest.of(0, 5, org.springframework.data.domain.Sort.by("createdAt").descending())
        ).stream().map(orderService::mapToDTO).collect(java.util.stream.Collectors.toList());

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd 'Tháng' M, yyyy", new java.util.Locale("vi", "VN"));

        // Calculate growth rates
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);

        BigDecimal revenueThisMonth = orderRepository.getRevenueBetween(startOfThisMonth, now);
        BigDecimal revenueLastMonth = orderRepository.getRevenueBetween(startOfLastMonth, startOfThisMonth);
        double revenueGrowth = calculateGrowth(revenueLastMonth, revenueThisMonth);

        long ordersThisMonth = orderRepository.countByCreatedAtBetween(startOfThisMonth, now);
        long ordersLastMonth = orderRepository.countByCreatedAtBetween(startOfLastMonth, startOfThisMonth);
        double ordersGrowth = calculateGrowth(ordersLastMonth, ordersThisMonth);

        long usersThisMonth = userRepository.countByRoleAndCreatedAtBetween(User.Role.USER, startOfThisMonth, now);
        long usersLastMonth = userRepository.countByRoleAndCreatedAtBetween(User.Role.USER, startOfLastMonth, startOfThisMonth);
        double usersGrowth = calculateGrowth(usersLastMonth, usersThisMonth);

        long reviewsThisMonth = reviewRepository.countByCreatedAtBetween(startOfThisMonth, now);
        long reviewsLastMonth = reviewRepository.countByCreatedAtBetween(startOfLastMonth, startOfThisMonth);
        double reviewsGrowth = calculateGrowth(reviewsLastMonth, reviewsThisMonth);

        return DashboardDTO.builder()
                .totalOrders(totalOrders)
                .totalProducts(totalProducts)
                .totalRevenue(totalRevenue)
                .totalUsers(totalUsers)
                .totalReviews(totalReviews)
                .pendingOrders(pendingOrders)
                .shippingOrders(shippingOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .year(year)
                .currentDate(LocalDateTime.now().format(formatter))
                .monthlyRevenue(monthlyRevenue)
                .dailyOrders(dailyOrders)
                .recentOrders(recentOrders)
                .revenueGrowth(revenueGrowth)
                .ordersGrowth(ordersGrowth)
                .usersGrowth(usersGrowth)
                .reviewsGrowth(reviewsGrowth)
                .build();
    }

    private double calculateGrowth(long last, long current) {
        if (last == 0) return current > 0 ? 100.0 : 0.0;
        return ((double) (current - last) / last) * 100.0;
    }

    private double calculateGrowth(BigDecimal last, BigDecimal current) {
        if (last == null || last.compareTo(BigDecimal.ZERO) == 0) {
            return (current != null && current.compareTo(BigDecimal.ZERO) > 0) ? 100.0 : 0.0;
        }
        if (current == null) current = BigDecimal.ZERO;
        return current.subtract(last)
                .divide(last, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}
