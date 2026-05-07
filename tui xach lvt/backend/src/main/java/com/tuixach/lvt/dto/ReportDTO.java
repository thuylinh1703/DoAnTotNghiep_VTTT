package com.tuixach.lvt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    private BigDecimal totalRevenue;
    private int totalOrders;
    private int totalProductsSold;
    private List<DailyRevenueDTO> dailyTrends;
    private List<TopProductDTO> topSellingProducts;
    private List<TopProductDTO> inventoryWarnings;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyRevenueDTO {
        private LocalDate date;
        private BigDecimal revenue;
        private int orders;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopProductDTO {
        private Long id;
        private String name;
        private int quantity;
        private BigDecimal revenue;
        private int stockRemaining;
    }
}
