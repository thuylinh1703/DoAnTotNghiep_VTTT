package com.tuixach.lvt.service;

import com.tuixach.lvt.dto.ReportDTO;
import com.tuixach.lvt.dto.ProductPerformanceDTO;
import com.tuixach.lvt.dto.RevenueBreakdownDTO;
import com.tuixach.lvt.dto.RevenuePointDTO;
import com.tuixach.lvt.dto.RevenueSummaryDTO;
import com.tuixach.lvt.entity.DailyRevenueSummary;
import com.tuixach.lvt.entity.Order;
import com.tuixach.lvt.entity.Order.OrderStatus;
import com.tuixach.lvt.repository.DailyRevenueSummaryRepository;
import com.tuixach.lvt.repository.OrderItemRepository;
import com.tuixach.lvt.repository.OrderRepository;
import com.tuixach.lvt.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final DailyRevenueSummaryRepository summaryRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    private static final List<OrderStatus> REVENUE_STATUSES = List.of(
            OrderStatus.DELIVERED,
            OrderStatus.COMPLETED);

    @Transactional(readOnly = true)
    public ReportDTO getRevenueReport(LocalDate start, LocalDate end) {
        List<DailyRevenueSummary> summaries = summaryRepository.findAllBetweenDates(start, end);
        
        BigDecimal totalRevenue = summaries.stream()
                .map(DailyRevenueSummary::getNetRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalOrders = summaries.stream()
                .mapToInt(DailyRevenueSummary::getOrderCount)
                .sum();
                
        int totalProducts = summaries.stream()
                .mapToInt(DailyRevenueSummary::getProductCount)
                .sum();

        List<ReportDTO.DailyRevenueDTO> trends = summaries.stream()
                .map(s -> new ReportDTO.DailyRevenueDTO(s.getDate(), s.getNetRevenue(), s.getOrderCount()))
                .collect(Collectors.toList());

        return ReportDTO.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .totalProductsSold(totalProducts)
                .dailyTrends(trends)
                .topSellingProducts(getTopSellers(10))
                .inventoryWarnings(getLowStockItems(10))
                .build();
    }

        @Transactional(readOnly = true)
        public List<RevenuePointDTO> getRevenueSeries(LocalDate from, LocalDate to, String groupBy) {
                LocalDate start = from == null ? LocalDate.now().minusDays(29) : from;
                LocalDate end = to == null ? LocalDate.now() : to;

                List<Order> orders = orderRepository.findByStatusInAndCreatedAtBetween(
                                REVENUE_STATUSES,
                                start.atStartOfDay(),
                                end.plusDays(1).atStartOfDay());

                Map<String, BigDecimal> revenueMap = new LinkedHashMap<>();
                Map<String, Long> countMap = new LinkedHashMap<>();

                String normalized = (groupBy == null || groupBy.isBlank()) ? "day" : groupBy.toLowerCase(Locale.ROOT);
                if ("month".equals(normalized)) {
                        YearMonth cursor = YearMonth.from(start);
                        YearMonth endMonth = YearMonth.from(end);
                        while (!cursor.isAfter(endMonth)) {
                                revenueMap.put(cursor.toString(), BigDecimal.ZERO);
                                countMap.put(cursor.toString(), 0L);
                                cursor = cursor.plusMonths(1);
                        }
                } else if ("week".equals(normalized)) {
                        LocalDate cursor = start;
                        WeekFields wf = WeekFields.of(DayOfWeek.MONDAY, 4);
                        while (!cursor.isAfter(end)) {
                                String key = cursor.get(wf.weekBasedYear()) + "-W" + cursor.get(wf.weekOfWeekBasedYear());
                                revenueMap.putIfAbsent(key, BigDecimal.ZERO);
                                countMap.putIfAbsent(key, 0L);
                                cursor = cursor.plusDays(1);
                        }
                } else {
                        LocalDate cursor = start;
                        while (!cursor.isAfter(end)) {
                                String key = cursor.toString();
                                revenueMap.put(key, BigDecimal.ZERO);
                                countMap.put(key, 0L);
                                cursor = cursor.plusDays(1);
                        }
                }

                for (Order order : orders) {
                        String key = bucketKey(order.getCreatedAt().toLocalDate(), normalized);
                        revenueMap.computeIfPresent(key, (k, v) -> v.add(order.getTotalAmount()));
                        countMap.computeIfPresent(key, (k, v) -> v + 1);
                }

                List<RevenuePointDTO> points = new ArrayList<>();
                for (Map.Entry<String, BigDecimal> entry : revenueMap.entrySet()) {
                        long count = countMap.getOrDefault(entry.getKey(), 0L);
                        BigDecimal avg = count == 0 ? BigDecimal.ZERO
                                        : entry.getValue().divide(BigDecimal.valueOf(count), 0, RoundingMode.HALF_UP);
                        points.add(RevenuePointDTO.builder()
                                        .period(entry.getKey())
                                        .revenue(entry.getValue())
                                        .orderCount(count)
                                        .avgOrderValue(avg)
                                        .build());
                }
                return points;
        }

        @Transactional(readOnly = true)
        public RevenueSummaryDTO getRevenueSummary() {
                LocalDate today = LocalDate.now();
                LocalDate yesterday = today.minusDays(1);

                LocalDate weekStart = today.with(DayOfWeek.MONDAY);
                LocalDate lastWeekStart = weekStart.minusWeeks(1);
                LocalDate lastWeekEnd = weekStart.minusDays(1);

                LocalDate monthStart = today.withDayOfMonth(1);
                LocalDate lastMonthStart = monthStart.minusMonths(1);
                LocalDate lastMonthEnd = monthStart.minusDays(1);

                LocalDate thisYearStart = today.withDayOfYear(1);
                LocalDate lastYearStart = thisYearStart.minusYears(1);
                LocalDate lastYearSameDay = today.minusYears(1);

                BigDecimal todayRevenue = sumRevenue(today, today);
                BigDecimal yesterdayRevenue = sumRevenue(yesterday, yesterday);
                BigDecimal thisWeek = sumRevenue(weekStart, today);
                BigDecimal lastWeek = sumRevenue(lastWeekStart, lastWeekEnd);
                BigDecimal thisMonth = sumRevenue(monthStart, today);
                BigDecimal lastMonth = sumRevenue(lastMonthStart, lastMonthEnd);
                BigDecimal thisYear = sumRevenue(thisYearStart, today);
                BigDecimal lastYear = sumRevenue(lastYearStart, lastYearSameDay);

                BigDecimal yoy = BigDecimal.ZERO;
                if (lastYear.compareTo(BigDecimal.ZERO) > 0) {
                        yoy = thisYear.subtract(lastYear)
                                        .multiply(BigDecimal.valueOf(100))
                                        .divide(lastYear, 2, RoundingMode.HALF_UP);
                }

                return RevenueSummaryDTO.builder()
                                .today(todayRevenue)
                                .yesterday(yesterdayRevenue)
                                .thisWeek(thisWeek)
                                .lastWeek(lastWeek)
                                .thisMonth(thisMonth)
                                .lastMonth(lastMonth)
                                .yoy(yoy)
                                .build();
        }

        @Transactional(readOnly = true)
        public List<RevenueBreakdownDTO> getRevenueByPaymentMethod(LocalDate from, LocalDate to) {
                LocalDate start = from == null ? LocalDate.now().minusDays(29) : from;
                LocalDate end = to == null ? LocalDate.now() : to;

                return orderRepository.summarizeRevenueByPaymentMethod(
                                                REVENUE_STATUSES,
                                                start.atStartOfDay(),
                                                end.plusDays(1).atStartOfDay())
                                .stream()
                                .map(row -> RevenueBreakdownDTO.builder()
                                                .key(String.valueOf(row[0]))
                                                .revenue((BigDecimal) row[1])
                                                .orderCount(((Number) row[2]).longValue())
                                                .build())
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<RevenueBreakdownDTO> getRevenueByCategory(LocalDate from, LocalDate to) {
                LocalDate start = from == null ? LocalDate.now().minusDays(29) : from;
                LocalDate end = to == null ? LocalDate.now() : to;

                return orderRepository.summarizeRevenueByCategory(
                                                REVENUE_STATUSES,
                                                start.atStartOfDay(),
                                                end.plusDays(1).atStartOfDay())
                                .stream()
                                .map(row -> RevenueBreakdownDTO.builder()
                                                .key(row[0] == null ? "Chưa phân loại" : String.valueOf(row[0]))
                                                .revenue((BigDecimal) row[1])
                                                .orderCount(((Number) row[2]).longValue())
                                                .build())
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<ProductPerformanceDTO> getTopSellers(String period, int limit) {
                LocalDateTime end = LocalDateTime.now();
                LocalDateTime start = startByPeriod(period, end);
                return orderItemRepository.findTopSellersInRange(REVENUE_STATUSES, start, end, PageRequest.of(0, limit))
                                .stream()
                                .map(row -> ProductPerformanceDTO.builder()
                                                .productId(((Number) row[0]).longValue())
                                                .productName((String) row[1])
                                                .categoryName((String) row[2])
                                                .unitsSold(((Number) row[3]).longValue())
                                                .revenue((BigDecimal) row[4])
                                                .stock(((Number) row[5]).intValue())
                                                .imageUrl((String) row[6])
                                                .build())
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<ProductPerformanceDTO> getSlowMovers(int limit, int threshold) {
                LocalDateTime end = LocalDateTime.now();
                LocalDateTime start = end.minusDays(30);
                LocalDateTime createdBefore = end.minusDays(30);

                return orderItemRepository.findSlowMovers(REVENUE_STATUSES, start, end, createdBefore, threshold,
                                                PageRequest.of(0, limit))
                                .stream()
                                .map(row -> ProductPerformanceDTO.builder()
                                                .productId(((Number) row[0]).longValue())
                                                .productName((String) row[1])
                                                .categoryName((String) row[2])
                                                .stock(((Number) row[3]).intValue())
                                                .unitsSold(((Number) row[4]).longValue())
                                                .revenue((BigDecimal) row[5])
                                                .imageUrl((String) row[6])
                                                .build())
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<ProductPerformanceDTO> getZeroSales(int days, int limit) {
                LocalDateTime end = LocalDateTime.now();
                LocalDateTime start = end.minusDays(Math.max(days, 1));

                return orderItemRepository.findZeroSalesProducts(REVENUE_STATUSES, start, end, PageRequest.of(0, limit))
                                .stream()
                                .map(row -> ProductPerformanceDTO.builder()
                                                .productId(((Number) row[0]).longValue())
                                                .productName((String) row[1])
                                                .categoryName((String) row[2])
                                                .stock(((Number) row[3]).intValue())
                                                .unitsSold(0)
                                                .revenue(BigDecimal.ZERO)
                                                .imageUrl((String) row[4])
                                                .build())
                                .collect(Collectors.toList());
        }

    @Transactional
    public void updateDailySummary(LocalDate date) {
        List<Order> orders = orderRepository.findByStatusInAndCreatedAtBetween(
            List.of(Order.OrderStatus.DELIVERED, Order.OrderStatus.COMPLETED),
            date.atStartOfDay(),
            date.plusDays(1).atStartOfDay()
        );

        BigDecimal revenue = orders.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = orders.stream().map(o -> o.getDiscountAmount() != null ? o.getDiscountAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int productCount = orders.stream().flatMap(o -> o.getItems().stream()).mapToInt(item -> item.getQuantity()).sum();

        DailyRevenueSummary summary = summaryRepository.findByDate(date)
                .orElse(DailyRevenueSummary.builder().date(date).build());

        summary.setRevenue(revenue.add(discount));
        summary.setOrderCount(orders.size());
        summary.setProductCount(productCount);
        summary.setDiscountAmount(discount);
        summary.setNetRevenue(revenue);

        summaryRepository.save(summary);
    }

    private List<ReportDTO.TopProductDTO> getTopSellers(int limit) {
                return productRepository.findTopSellers(org.springframework.data.domain.PageRequest.of(0, limit));
    }

    private List<ReportDTO.TopProductDTO> getLowStockItems(int limit) {
        return productRepository.findByQuantityLessThanOrderByQuantityAsc(limit + 5)
                .stream()
                .limit(limit)
                .map(p -> new ReportDTO.TopProductDTO(p.getId(), p.getName(), 0, BigDecimal.ZERO, p.getQuantity()))
                .collect(Collectors.toList());
    }

        private String bucketKey(LocalDate date, String groupBy) {
                if ("month".equals(groupBy)) {
                        return YearMonth.from(date).toString();
                }
                if ("week".equals(groupBy)) {
                        WeekFields wf = WeekFields.of(DayOfWeek.MONDAY, 4);
                        return date.get(wf.weekBasedYear()) + "-W" + date.get(wf.weekOfWeekBasedYear());
                }
                return date.toString();
        }

        private BigDecimal sumRevenue(LocalDate start, LocalDate end) {
                if (end.isBefore(start)) {
                        return BigDecimal.ZERO;
                }
                return orderRepository.findByStatusInAndCreatedAtBetween(
                                                REVENUE_STATUSES,
                                                start.atStartOfDay(),
                                                end.plusDays(1).atStartOfDay())
                                .stream()
                                .map(Order::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private LocalDateTime startByPeriod(String period, LocalDateTime end) {
                if (period == null || period.isBlank()) {
                        return end.minusDays(30);
                }
                return switch (period.toLowerCase(Locale.ROOT)) {
                        case "week" -> end.minusDays(7);
                        case "month" -> end.minusDays(30);
                        case "year" -> end.minusDays(365);
                        default -> end.minusDays(30);
                };
        }
}
